package com.dac.chargemanager.business.service;

import com.dac.chargemanager.business.dto.CustomerDTO;
import com.dac.chargemanager.business.exception.BusinessException;
import com.dac.chargemanager.business.exception.ResourceNotFoundException;
import com.dac.chargemanager.infra.entity.Customer;
import com.dac.chargemanager.infra.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Customer business logic.
 * Implements business rules and validation.
 */
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Creates a new customer with business rule validation.
     *
     * @param dto the customer data
     * @return the created customer
     * @throws BusinessException if validation fails
     */
    public CustomerDTO createCustomer(CustomerDTO dto) {
        logger.info("Creating customer with email: {}", dto.getEmail());

        // Business rule: email must be unique
        if (customerRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Email already registered: " + dto.getEmail());
        }

        // Business rule: CPF/CNPJ must be unique
        if (customerRepository.existsByCpfCnpj(dto.getCpfCnpj())) {
            throw new BusinessException("CPF/CNPJ already registered: " + dto.getCpfCnpj());
        }

        Customer customer = toEntity(dto);
        Customer savedCustomer = customerRepository.save(customer);

        logger.info("Customer created with id: {}", savedCustomer.getId());
        return toDTO(savedCustomer);
    }

    /**
     * Updates an existing customer.
     *
     * @param id  the customer ID
     * @param dto the updated customer data
     * @return the updated customer
     * @throws ResourceNotFoundException if customer not found
     * @throws BusinessException         if validation fails
     */
    public CustomerDTO updateCustomer(Long id, CustomerDTO dto) {
        logger.info("Updating customer with id: {}", id);

        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));

        // Business rule: if email changed, check uniqueness
        if (!existingCustomer.getEmail().equals(dto.getEmail()) &&
                customerRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Email already registered: " + dto.getEmail());
        }

        // Business rule: if CPF/CNPJ changed, check uniqueness
        if (!existingCustomer.getCpfCnpj().equals(dto.getCpfCnpj()) &&
                customerRepository.existsByCpfCnpj(dto.getCpfCnpj())) {
            throw new BusinessException("CPF/CNPJ already registered: " + dto.getCpfCnpj());
        }

        existingCustomer.setName(dto.getName());
        existingCustomer.setEmail(dto.getEmail());
        existingCustomer.setCpfCnpj(dto.getCpfCnpj());
        existingCustomer.setPhone(dto.getPhone());

        Customer updatedCustomer = customerRepository.update(existingCustomer);

        logger.info("Customer updated with id: {}", updatedCustomer.getId());
        return toDTO(updatedCustomer);
    }

    /**
     * Retrieves a customer by ID.
     *
     * @param id the customer ID
     * @return the customer
     * @throws ResourceNotFoundException if customer not found
     */
    public CustomerDTO getCustomerById(Long id) {
        logger.debug("Fetching customer with id: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));

        return toDTO(customer);
    }

    /**
     * Retrieves all customers.
     *
     * @return list of all customers
     */
    public List<CustomerDTO> getAllCustomers() {
        logger.debug("Fetching all customers");

        return customerRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Deletes a customer by ID.
     *
     * @param id the customer ID
     * @throws ResourceNotFoundException if customer not found
     */
    public void deleteCustomer(Long id) {
        logger.info("Deleting customer with id: {}", id);

        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer", id);
        }

        customerRepository.deleteById(id);
        logger.info("Customer deleted with id: {}", id);
    }

    /**
     * Converts entity to DTO.
     */
    private CustomerDTO toDTO(Customer customer) {
        return new CustomerDTO(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getCpfCnpj(),
                customer.getPhone(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    /**
     * Converts DTO to entity.
     */
    private Customer toEntity(CustomerDTO dto) {
        Customer customer = new Customer();
        customer.setId(dto.getId());
        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setCpfCnpj(dto.getCpfCnpj());
        customer.setPhone(dto.getPhone());
        return customer;
    }
}
