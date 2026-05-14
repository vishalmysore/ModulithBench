package com.benchmark.supplychain.route;

public class RouteNotFoundException extends RuntimeException {
    public RouteNotFoundException(Long id) { super("Route not found with id: " + id); }
    public RouteNotFoundException(String msg) { super(msg); }
}
