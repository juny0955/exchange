use crate::engine::event::EngineEvent;
use crate::engine::orderbook::OrderBook;
use crate::models::{AccountId, Order, OrderId, OrderType, Price, Quantity, QuoteQty, Side, TimeInForce, Trade};

struct MakerInfo {
    order_id: OrderId,
    account_id: AccountId,
    price: Price,
    remaining_qty: Quantity,
}

pub struct Matcher;
impl Matcher {
    pub fn match_order(order: Order, book: &mut OrderBook) -> EngineEvent {
        if order.order_type == OrderType::Market {
            match order.side {
                Side::Buy => Self::process_fok_quote(order, book),
                Side::Sell => Self::process_fok(order, book),
            }
        } else {
            match order.tif {
                TimeInForce::Gtc => Self::process_match(order, book, true),
                TimeInForce::Ioc => Self::process_match(order, book, false),
                TimeInForce::Fok => Self::process_fok(order, book),
            }
        }
    }

    /// GTC/IOC 공통 매칭 루프
    ///
    /// resting=true 일시 미체결 잔량 호가 등록
    fn process_match(order: Order, book: &mut OrderBook, resting: bool) -> EngineEvent {
        let mut taker = order;
        let mut trades = Vec::new();

        while let Some(maker) = Self::get_maker_info(taker.side.opposite(), book) {
            if !Self::can_match(&taker, maker.price) {
                break;
            }

            let trade = Self::execute_fill(&mut taker, maker, book);
            trades.push(trade);

            if taker.is_fully_filled() { return EngineEvent::Matched(trades) }
        }

        if resting && !taker.is_fully_filled(){
            book.add(taker);
        }

        EngineEvent::Matched(trades)
    }

    /// FOK 전용 Processor
    fn process_fok(order: Order, book: &mut OrderBook) -> EngineEvent {
        let price = order.price.unwrap().value();
        let required = order.quantity.unwrap().value();
        
        if !book.can_fully_fill(order.side.opposite(), price, required) {
            return EngineEvent::Canceled(order.order_id);
        }
        Self::process_match(order, book, false)
    }

    /// 시장가 매수(FOK) 전용
    ///
    /// 금액 기준 전량 체결 검증 후 처리
    fn process_fok_quote(order: Order, book: &mut OrderBook) -> EngineEvent {
        let required_quote = order.quote_qty.unwrap();
        if !book.can_fully_fill_quote(required_quote.value()) {
            return EngineEvent::Canceled(order.order_id); // TODO 잔량 부족으로 인한 취소 명시
        }

        let mut trades = Vec::new();
        let mut remaining_quote = required_quote;

        while remaining_quote > QuoteQty::zero() {
            let Some(maker) = Self::get_maker_info(Side::Sell, book) else { break };

            let max_qty = Quantity::new(remaining_quote.value() / maker.price.value());
            let fill_qty = maker.remaining_qty.min(max_qty);
            let fill_quote = QuoteQty::new(maker.price.value() * fill_qty.value());

            book.fill(&maker.order_id, fill_qty);
            remaining_quote = remaining_quote.sub(fill_quote);

            trades.push(Trade::new(
                order.symbol.clone(),
                order.account_id,
                order.order_id,
                maker.account_id,
                maker.order_id,
                maker.price,
                fill_qty,
                fill_quote
            ));
        }

        EngineEvent::Matched(trades)
    }

    // 반대편 최우선 호가 MakerInfo로 변환
    fn get_maker_info(side: Side, book: &mut OrderBook) -> Option<MakerInfo> {
        match side {
            Side::Buy => book.best_bid(),
            Side::Sell => book.best_ask(),
        }.map(|m| MakerInfo {
                order_id: m.order_id,
                account_id: m.account_id,
                price: m.price.unwrap(),
                remaining_qty: m.remaining_qty(),
            }
        )
    }

    // 체결 가능 여부 확인 - 시장가는 항상 true
    fn can_match(taker: &Order, maker_price: Price) -> bool {
        match taker.price {
            Some(taker_price) => match taker.side {
                Side::Buy => taker_price >= maker_price,
                Side::Sell => taker_price <= maker_price,
            }
            None => true
        }
    }

    // 단일 체결 실행 - taker/maker 수량 차감 및 Trade 생성
    fn execute_fill(taker: &mut Order, maker: MakerInfo, book: &mut OrderBook) -> Trade {
        let fill_qty = maker.remaining_qty.min(taker.remaining_qty());
        let quote_qty = QuoteQty::new(maker.price.value() * fill_qty.value());

        book.fill(&maker.order_id, fill_qty);
        taker.fill(fill_qty);

        let (buy_account_id, buy_order_id, sell_account_id, sell_order_id) = match taker.side {
            Side::Buy => (taker.account_id, taker.order_id, maker.account_id, maker.order_id),
            Side::Sell => (maker.account_id, maker.order_id, taker.account_id, taker.order_id),
        };

        Trade::new(taker.symbol.clone(), buy_account_id, buy_order_id, sell_account_id, sell_order_id, maker.price, fill_qty, quote_qty)
    }
}