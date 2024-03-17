package com.shop.exception;

// 상품 주문 수량 보다 현재 재고의 수가 적을 때 발생 시킬 Exception 정의
public class OutOfStockException extends RuntimeException{

    public OutOfStockException(String message) {
        super(message);
    }

}
