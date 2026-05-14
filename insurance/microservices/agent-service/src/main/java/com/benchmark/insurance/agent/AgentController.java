package com.benchmark.insurance.agent;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agents")
@RequiredArgsConstructor
public class AgentController {
    private final AgentService agentService;

    @PostMapping
    public ResponseEntity<Agent> create(@RequestBody Agent agent) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agentService.createAgent(agent));
    }

    @GetMapping("/{agentId}")
    public ResponseEntity<Agent> getById(@PathVariable Long agentId) {
        return ResponseEntity.ok(agentService.getAgentById(agentId));
    }

    @PutMapping("/{agentId}")
    public ResponseEntity<Agent> update(@PathVariable Long agentId, @RequestBody Agent agent) {
        return ResponseEntity.ok(agentService.updateAgent(agentId, agent));
    }

    @DeleteMapping("/{agentId}")
    public ResponseEntity<Void> delete(@PathVariable Long agentId) {
        agentService.deleteAgent(agentId);
        return ResponseEntity.noContent().build();
    }
}