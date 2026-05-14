package com.benchmark.insurance.agent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

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
                .orElseThrow(() -> new AgentNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Agent> getAllAgents() {
        return agentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Agent> getActiveAgents() {
        return agentRepository.findByActive(true);
    }

    @Transactional
    public Agent updateAgent(Long id, Agent details) {
        Agent agent = getAgentById(id);
        agent.setFirstName(details.getFirstName());
        agent.setLastName(details.getLastName());
        agent.setEmail(details.getEmail());
        agent.setPhone(details.getPhone());
        agent.setCommissionRate(details.getCommissionRate());
        agent.setActive(details.isActive());
        return agentRepository.save(agent);
    }

    @Transactional
    public void deleteAgent(Long id) {
        agentRepository.delete(getAgentById(id));
    }

    // Called by PolicyService — validates agent is active before assigning
    @Transactional(readOnly = true)
    public void validateActiveAgent(Long agentId) {
        Agent agent = getAgentById(agentId);
        if (!agent.isActive()) {
            throw new IllegalStateException("Agent " + agentId + " is not active");
        }
    }
}
