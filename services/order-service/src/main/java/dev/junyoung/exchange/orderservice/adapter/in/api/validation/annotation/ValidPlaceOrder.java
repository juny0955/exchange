package dev.junyoung.exchange.orderservice.adapter.in.api.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.junyoung.exchange.orderservice.adapter.in.api.validation.PlaceOrderValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 주문 요청 Request를 검증한다
 *
 * <p>검증 항목은 다음과 같다</p>
 *
 * <h3>지정가 주문</h3>
 * <ul>
 *     <li>Price - 필수</li>
 *     <li>Quantity - 필수</li>
 *     <li>QuoteQty - 사용안함</li>
 * </ul>
 *
 * <h3>시장가 매수 주문</h3>
 * <ul>
 *     <li>TIF IOC 고정</li>
 *     <li>QuoteQty - 필수</li>
 *     <li>Quantity - 사용안함</li>
 *     <li>Price - 사용안함</li>
 * </ul>
 *
 * <h3>시장가 매도 주문</h3>
 * <ul>
 *     <li>TIF IOC 고정</li>
 *     <li>Quantity - 필수</li>
 *     <li>QuoteQty - 사용안함</li>
 *     <li>Price - 사용안함</li>
 * </ul>
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PlaceOrderValidator.class)
public @interface ValidPlaceOrder {
	String message() default "Invalid order";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
