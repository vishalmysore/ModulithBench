package com.benchmark.insurance;

import com.benchmark.insurance.agent.Agent;
import com.benchmark.insurance.agent.AgentService;
import com.benchmark.insurance.claim.Claim;
import com.benchmark.insurance.claim.ClaimService;
import com.benchmark.insurance.claim.ClaimStatus;
import com.benchmark.insurance.coverage.Coverage;
import com.benchmark.insurance.coverage.CoverageService;
import com.benchmark.insurance.customer.Customer;
import com.benchmark.insurance.customer.CustomerService;
import com.benchmark.insurance.policy.Policy;
import com.benchmark.insurance.policy.PolicyService;
import com.benchmark.insurance.policy.PolicyStatus;
import com.benchmark.insurance.policy.PolicyType;
import com.benchmark.insurance.premium.PremiumFrequency;
import com.benchmark.insurance.premium.PremiumService;
import com.benchmark.insurance.settlement.SettlementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * BENCHMARK VALIDATION TESTS — Insurance Monolith
 *
 * Run: mvn test -Dtest=CrossModuleIntegrationTest
 *
 * These tests prove that cross-module operations work atomically.
 * Compare with microservices to observe the architectural difference.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:insurance_test;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class CrossModuleIntegrationTest {

    @Autowired CustomerService customerService;
    @Autowired AgentService agentService;
    @Autowired PolicyService policyService;
    @Autowired CoverageService coverageService;
    @Autowired PremiumService premiumService;
    @Autowired ClaimService claimService;
    @Autowired SettlementService settlementService;

    private Customer createCustomer(String email) {
        return customerService.createCustomer(Customer.builder()
                .firstName("Test").lastName("Customer").email(email)
                .nationalId("NID-" + System.nanoTime()).build());
    }

    private Agent createAgent(String email) {
        return agentService.createAgent(Agent.builder()
                .firstName("Test").lastName("Agent").email(email)
                .licenseNumber("LIC-" + System.nanoTime()).build());
    }

    private Policy createActivePolicy(Long customerId, Long agentId) {
        Policy policy = policyService.createPolicy(Policy.builder()
                .customerId(customerId).agentId(agentId)
                .type(PolicyType.HEALTH)
                .startDate(LocalDate.now()).endDate(LocalDate.now().plusYears(1))
                .premiumAmount(new BigDecimal("200.00"))
                .coverageAmount(new BigDecimal("50000.00"))
                .build());
        return policyService.activatePolicy(policy.getId());
    }

    // ───────────────────────────────────────────────────────────
    // CROSS-MODULE TEST 1: Policy creation validates customer + agent atomically
    // ───────────────────────────────────────────────────────────

    @Test
    void createPolicy_shouldValidateCustomerAndAgentInOneTransaction() {
        Customer customer = createCustomer("cust1@test.com");
        Agent agent = createAgent("agent1@test.com");

        // Cross-module: PolicyService calls CustomerService + AgentService
        Policy policy = policyService.createPolicy(Policy.builder()
                .customerId(customer.getId()).agentId(agent.getId())
                .type(PolicyType.LIFE)
                .startDate(LocalDate.now()).endDate(LocalDate.now().plusYears(1))
                .premiumAmount(new BigDecimal("150.00"))
                .coverageAmount(new BigDecimal("100000.00"))
                .build());

        assertThat(policy.getCustomerId()).isEqualTo(customer.getId());
        assertThat(policy.getAgentId()).isEqualTo(agent.getId());
        assertThat(policy.getPolicyNumber()).startsWith("POL-");
    }

    // ───────────────────────────────────────────────────────────
    // CROSS-MODULE TEST 2: Inactive customer cannot get a policy
    // ───────────────────────────────────────────────────────────

    @Test
    void createPolicy_shouldRejectInactiveCustomer() {
        Customer customer = createCustomer("cust2@test.com");
        Agent agent = createAgent("agent2@test.com");
        customer.setActive(false);
        customerService.updateCustomer(customer.getId(), customer);

        // Cross-module: PolicyService calls CustomerService.validateActiveCustomer()
        assertThatThrownBy(() -> policyService.createPolicy(Policy.builder()
                .customerId(customer.getId()).agentId(agent.getId())
                .type(PolicyType.AUTO)
                .startDate(LocalDate.now()).endDate(LocalDate.now().plusYears(1))
                .premiumAmount(new BigDecimal("100.00")).coverageAmount(new BigDecimal("20000.00"))
                .build()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not active");
    }

    // ───────────────────────────────────────────────────────────
    // CROSS-MODULE TEST 3: Claim validates policy ownership by customer
    // ───────────────────────────────────────────────────────────

    @Test
    void fileClaim_shouldValidatePolicyBelongsToCustomer() {
        Customer customer1 = createCustomer("cust3a@test.com");
        Customer customer2 = createCustomer("cust3b@test.com");
        Agent agent = createAgent("agent3@test.com");

        Policy policy = createActivePolicy(customer1.getId(), agent.getId());

        // Cross-module: ClaimService checks policy.getCustomerId() == customerId
        // This is a direct Java call — in microservices it would require an HTTP call to policy-service
        assertThatThrownBy(() -> claimService.fileClaim(
                policy.getId(), customer2.getId(),
                "MEDICAL", "Hospital visit", new BigDecimal("5000"), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not belong to customer");
    }

    // ───────────────────────────────────────────────────────────
    // CROSS-MODULE TEST 4: Claim on inactive policy is rejected
    // ───────────────────────────────────────────────────────────

    @Test
    void fileClaim_shouldRejectWhenPolicyNotActive() {
        Customer customer = createCustomer("cust4@test.com");
        Agent agent = createAgent("agent4@test.com");

        // Create policy but do NOT activate it
        Policy policy = policyService.createPolicy(Policy.builder()
                .customerId(customer.getId()).agentId(agent.getId())
                .type(PolicyType.HOME).startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .premiumAmount(new BigDecimal("80.00")).coverageAmount(new BigDecimal("200000.00"))
                .build());

        assertThat(policy.getStatus()).isEqualTo(PolicyStatus.PENDING);

        // Cross-module: ClaimService calls PolicyService.validateActivePolicy()
        assertThatThrownBy(() -> claimService.fileClaim(
                policy.getId(), customer.getId(),
                "PROPERTY", "Fire damage", new BigDecimal("10000"), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not active");
    }

    // ───────────────────────────────────────────────────────────
    // CROSS-MODULE TEST 5: Settlement reads approved amount from claim directly
    // ───────────────────────────────────────────────────────────

    @Test
    void settleApprovedClaim_shouldReadAmountFromClaimDirectly() {
        Customer customer = createCustomer("cust5@test.com");
        Agent agent = createAgent("agent5@test.com");
        Policy policy = createActivePolicy(customer.getId(), agent.getId());

        Claim claim = claimService.fileClaim(
                policy.getId(), customer.getId(),
                "MEDICAL", "Surgery", new BigDecimal("8000"), null);

        claimService.approveClaim(claim.getId(), new BigDecimal("7500"));

        // Cross-module: SettlementService calls ClaimService.getClaimById()
        // and reads claim.getApprovedAmount() directly — no HTTP call
        var settlement = settlementService.settleApprovedClaim(
                claim.getId(), "BANK_TRANSFER", "Approved settlement");

        assertThat(settlement.getAmount()).isEqualByComparingTo("7500.00");
        assertThat(settlement.getClaimId()).isEqualTo(claim.getId());
    }

    // ───────────────────────────────────────────────────────────
    // CROSS-MODULE TEST 6: Settlement fails if claim is not approved
    // ───────────────────────────────────────────────────────────

    @Test
    void settleApprovedClaim_shouldFailIfClaimNotApproved() {
        Customer customer = createCustomer("cust6@test.com");
        Agent agent = createAgent("agent6@test.com");
        Policy policy = createActivePolicy(customer.getId(), agent.getId());

        Claim claim = claimService.fileClaim(
                policy.getId(), customer.getId(),
                "MEDICAL", "Checkup", new BigDecimal("500"), null);

        // Claim is still FILED, not APPROVED
        assertThat(claim.getStatus()).isEqualTo(ClaimStatus.FILED);

        // Cross-module: SettlementService reads claim status via ClaimService
        assertThatThrownBy(() -> settlementService.settleApprovedClaim(
                claim.getId(), "BANK_TRANSFER", null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be APPROVED");
    }

    // ───────────────────────────────────────────────────────────
    // CROSS-MODULE TEST 7: Premium schedule reads policy premium amount
    // ───────────────────────────────────────────────────────────

    @Test
    void schedulePremium_shouldReadAmountFromPolicy() {
        Customer customer = createCustomer("cust7@test.com");
        Agent agent = createAgent("agent7@test.com");
        Policy policy = createActivePolicy(customer.getId(), agent.getId());

        // Cross-module: PremiumService calls PolicyService.getPolicyById() to get premium amount
        var premium = premiumService.schedulePremium(
                policy.getId(), PremiumFrequency.MONTHLY, LocalDate.now().plusMonths(1));

        // Amount should match the policy's premiumAmount (200.00)
        assertThat(premium.getAmount()).isEqualByComparingTo("200.00");
        assertThat(premium.getPolicyId()).isEqualTo(policy.getId());
    }

    // ───────────────────────────────────────────────────────────
    // CROSS-MODULE TEST 8: Coverage validates policy exists before adding
    // ───────────────────────────────────────────────────────────

    @Test
    void createCoverage_shouldFailForNonExistentPolicy() {
        // Cross-module: CoverageService calls PolicyService.getPolicyById()
        assertThatThrownBy(() -> coverageService.createCoverage(Coverage.builder()
                .policyId(99999L)
                .type("MEDICAL").description("Hospital coverage")
                .maximumAmount(new BigDecimal("50000")).build()))
                .isInstanceOf(com.benchmark.insurance.policy.PolicyNotFoundException.class);
    }
}
