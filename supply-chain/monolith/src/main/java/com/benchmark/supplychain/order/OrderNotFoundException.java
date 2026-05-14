package com.benchmark.supplychain.order;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long id) { super("Order not found with id: " + id); }
    public OrderNotFoundException(String msg) { super(msg); }
}
