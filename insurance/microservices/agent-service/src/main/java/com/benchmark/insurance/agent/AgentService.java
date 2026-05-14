package com.benchmark.insurance.agent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AgentService {
    private final AgentRepository agentRepository;

    @Transactional
    public Agent createAgent(Agent agent) {
        return agentRepository.save(agent);
    }

    @Transactional(readOnly = true)
    public Agent getAgentById(Long id) {
        return agentRepository.findById(id)
                .orElseThrow(() -> new AgentNotFoundException("Agent not found with id: " + id));
    }

    @Transactional
    public Agent updateAgent(Long id, Agent agentDetails) {
        Agent agent = getAgentById(id);
        return agentRepository.save(agent);
    }

    @Transactional
    public void deleteAgent(Long id) {
        Agent agent = getAgentById(id);
        agentRepository.delete(agent);
    }
}