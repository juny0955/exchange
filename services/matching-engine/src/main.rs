use crossbeam::channel::{self, Sender};
use matching_engine::kafka::{KafkaConfig, KafkaConsumer, KafkaProducer};
use matching_engine::{
    engine::{EngineEvent, EngineManager},
    models::Symbol,
};
use opentelemetry::trace::TracerProvider;
use opentelemetry::{KeyValue, global};
use opentelemetry_appender_tracing::layer::OpenTelemetryTracingBridge;
use opentelemetry_otlp::{LogExporter, MetricExporter, SpanExporter};
use opentelemetry_sdk::Resource;
use opentelemetry_sdk::logs::SdkLoggerProvider;
use opentelemetry_sdk::metrics::SdkMeterProvider;
use opentelemetry_sdk::trace::SdkTracerProvider;
use std::io::{IsTerminal, stdout};
use std::process::exit;
use std::sync::Arc;
use std::sync::atomic::{AtomicBool, Ordering};
use std::thread;
use tracing::error;
use tracing_subscriber::layer::SubscriberExt;
use tracing_subscriber::util::SubscriberInitExt;
use tracing_subscriber::{EnvFilter, Layer};

struct OtelGuard {
    tracer_provider: SdkTracerProvider,
    logger_provider: SdkLoggerProvider,
    meter_provider: SdkMeterProvider,
}

impl Drop for OtelGuard {
    fn drop(&mut self) {
        let _ = self.meter_provider.shutdown();
        let _ = self.tracer_provider.shutdown();
        let _ = self.logger_provider.shutdown();
    }
}

fn main() {
    let _otel_guard = init_otel();
    let kafka_config = init_kafka_config();
    let (event_sender, event_receiver) = channel::unbounded::<EngineEvent>();
    let manager = init_engine_manager(event_sender);

    let shutdown = Arc::new(AtomicBool::new(false));
    let shutdown_signal = shutdown.clone();
    ctrlc::set_handler(move || {
        shutdown_signal.store(true, Ordering::Relaxed);
    })
    .expect("Ctrl-C 시그널 핸들러 등록 실패");

    let consumer = init_kafka_consumer(kafka_config.clone(), manager, shutdown.clone());
    let producer = init_kafka_producer(kafka_config);

    thread::scope(|scope| {
        let manager_handle = scope.spawn(|| {
            let manager = consumer.run();
            manager.shutdown();
        });

        producer.run(event_receiver);

        manager_handle.join().expect("스레드 panic!");
    });
}

fn init_otel() -> OtelGuard {
    let resource = Resource::builder()
        .with_attribute(KeyValue::new("service.name", "matching-engine"))
        .build();

    // trace 설정
    let span_exporter = SpanExporter::builder()
        .with_http()
        .build()
        .expect("SpanExporter 생성 실패");
    let tracer_provider = SdkTracerProvider::builder()
        .with_resource(resource.clone())
        .with_batch_exporter(span_exporter)
        .build();
    global::set_tracer_provider(tracer_provider.clone());

    // log 설정
    let log_exporter = LogExporter::builder()
        .with_http()
        .build()
        .expect("LogExporter 생성 실패");
    let logger_provider = SdkLoggerProvider::builder()
        .with_resource(resource.clone())
        .with_batch_exporter(log_exporter)
        .build();

    // metric 설정
    let meter_exporter = MetricExporter::builder()
        .with_http()
        .build()
        .expect("MetricExporter 생성 실패");
    let meter_provider = SdkMeterProvider::builder()
        .with_resource(resource.clone())
        .with_periodic_exporter(meter_exporter)
        .build();
    global::set_meter_provider(meter_provider.clone());

    let filter = EnvFilter::try_from_default_env().unwrap_or_else(|_| EnvFilter::new("info"));
    let json = std::env::var("LOG_FORMAT")
        .map(|v| v.eq_ignore_ascii_case("json"))
        .unwrap_or(false);

    let tracer = tracer_provider.tracer("matching-engine");

    let fmt_layer = tracing_subscriber::fmt::layer();
    let fmt_layer = if json {
        fmt_layer.json().boxed()
    } else {
        fmt_layer
            .with_ansi(IsTerminal::is_terminal(&stdout()))
            .boxed()
    };

    tracing_subscriber::registry()
        .with(filter)
        .with(fmt_layer)
        .with(tracing_opentelemetry::layer().with_tracer(tracer))
        .with(OpenTelemetryTracingBridge::new(&logger_provider))
        .init();

    OtelGuard {
        tracer_provider,
        logger_provider,
        meter_provider,
    }
}

fn init_kafka_config() -> KafkaConfig {
    match KafkaConfig::from_env() {
        Ok(c) => c,
        Err(e) => {
            error!(error = %e, "KafkaConfig 로딩 실패");
            exit(1);
        }
    }
}

fn init_engine_manager(event_sender: Sender<EngineEvent>) -> EngineManager {
    let mut engine_manager = EngineManager::new(event_sender);
    engine_manager.register_symbol(Symbol {
        base_asset: "BTC".into(),
        quote_asset: "KRW".into(),
    });

    engine_manager
}

fn init_kafka_consumer(
    kafka_config: KafkaConfig,
    manager: EngineManager,
    shutdown: Arc<AtomicBool>,
) -> KafkaConsumer {
    match KafkaConsumer::new(kafka_config, manager, shutdown) {
        Ok(c) => c,
        Err(e) => {
            error!(error = %e, "KafkaConsumer 초기화 실패");
            exit(1);
        }
    }
}

fn init_kafka_producer(kafka_config: KafkaConfig) -> KafkaProducer {
    match KafkaProducer::new(kafka_config) {
        Ok(p) => p,
        Err(e) => {
            error!(error = %e, "KafkaProducer 초기화 실패");
            exit(1);
        }
    }
}
