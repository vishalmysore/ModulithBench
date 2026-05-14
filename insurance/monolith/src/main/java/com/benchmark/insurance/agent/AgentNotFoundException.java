package com.benchmark.insurance.agent;

public class AgentNotFoundException extends RuntimeException {
    public AgentNotFoundException(Long id) { super("Agent not found with id: " + id); }
}
