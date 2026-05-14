package com.benchmark.insurance.agent;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @PostMapping
    public ResponseEntity<Agent> create(@Valid @RequestBody Agent agent) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agentService.createAgent(agent));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Agent> getById(@PathVariable Long id) {
        return ResponseEntity.ok(agentService.getAgentById(id));
    }

    @GetMapping
    public ResponseEntity<List<Agent>> getAll() {
        return ResponseEntity.ok(agentService.getAllAgents());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Agent>> getActive() {
        return ResponseEntity.ok(agentService.getActiveAgents());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Agent> update(@PathVariable Long id, @Valid @RequestBody Agent agent) {
        return ResponseEntity.ok(agentService.updateAgent(id, agent));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        agentService.deleteAgent(id);
        return ResponseEntity.noContent().build();
    }
}
