package com.benchmark.supplychain.order;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PICKING,
    PACKED,
    DISPATCHED,
    IN_TRANSIT,
    DELIVERED,
    CANCELLED,
    FAILED
}
