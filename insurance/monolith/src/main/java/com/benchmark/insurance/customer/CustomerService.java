package com.benchmark.insurance.customer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Transactional
    public Customer updateCustomer(Long id, Customer details) {
        Customer customer = getCustomerById(id);
        customer.setFirstName(details.getFirstName());
        customer.setLastName(details.getLastName());
        customer.setEmail(details.getEmail());
        customer.setPhone(details.getPhone());
        customer.setAddress(details.getAddress());
        return customerRepository.save(customer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        customerRepository.delete(getCustomerById(id));
    }

    // Called by PolicyService and ClaimService — validates customer exists and is active
    @Transactional(readOnly = true)
    public void validateActiveCustomer(Long customerId) {
        Customer customer = getCustomerById(customerId);
        if (!customer.isActive()) {
            throw new IllegalStateException("Customer " + customerId + " is not active");
        }
    }
}
