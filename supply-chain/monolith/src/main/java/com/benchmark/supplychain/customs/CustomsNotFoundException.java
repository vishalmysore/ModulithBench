package com.benchmark.supplychain.customs;

public class CustomsNotFoundException extends RuntimeException {
    public CustomsNotFoundException(Long id) { super("Customs declaration not found with id: " + id); }
    public CustomsNotFoundException(String msg) { super(msg); }
}
