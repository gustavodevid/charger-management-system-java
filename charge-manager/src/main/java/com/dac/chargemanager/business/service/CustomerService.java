package com.dac.chargemanager.business.service;

import com.dac.chargemanager.business.dto.CustomerDTO;
import com.dac.chargemanager.business.exception.BusinessException;
import com.dac.chargemanager.business.exception.ResourceNotFoundException;
import com.dac.chargemanager.infra.entity.Customer;
import com.dac.chargemanager.infra.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Customer business logic.
 * Implements business rules and validation with explicit transaction management.
 */
@Service
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final DataSource dataSource;

    @Autowired
    public CustomerService(CustomerRepository customerRepository, DataSource dataSource) {
        this.customerRepository = customerRepository;
        this.dataSource = dataSource;
    }

    // ==================== METHODS WITH CONNECTION PARAMETER ====================

    /**
     * Retrieves a customer by ID using provided connection.
     * Used when caller manages the transaction.
     *
     * @param id the customer ID
     * @param conn the database connection (caller manages transaction)
     * @return the customer
     * @throws ResourceNotFoundException if customer not found
     */
    public CustomerDTO getCustomerById(Long id, Connection conn) {
        logger.debug("Fetching customer with id: {}", id);

        Customer customer = customerRepository.findById(id, conn)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));

        return toDTO(customer);
    }

    // ==================== TRANSACTIONAL METHODS ====================

    /**
     * Creates a new customer with business rule validation.
     * Manages transaction explicitly: disables autocommit, commits on success, rollbacks on failure.
     *
     * @param dto the customer data
     * @return the created customer
     * @throws BusinessException if validation fails
     */
    public CustomerDTO createCustomer(CustomerDTO dto) {
        logger.info("Creating customer with email: {}", dto.getEmail());

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            // Business rule: email must be unique
            if (customerRepository.existsByEmail(dto.getEmail(), conn)) {
                throw new BusinessException("Email already registered: " + dto.getEmail());
            }

            // Business rule: CPF/CNPJ must be unique
            if (customerRepository.existsByCpfCnpj(dto.getCpfCnpj(), conn)) {
                throw new BusinessException("CPF/CNPJ already registered: " + dto.getCpfCnpj());
            }

            Customer customer = toEntity(dto);
            Customer savedCustomer = customerRepository.save(customer, conn);

            conn.commit();
            logger.info("Customer created with id: {}", savedCustomer.getId());
            return toDTO(savedCustomer);

        } catch (SQLException e) {
            rollbackQuietly(conn);
            logger.error("Database error creating customer", e);
            throw new RuntimeException("Database error creating customer", e);
        } catch (BusinessException e) {
            rollbackQuietly(conn);
            throw e;
        } catch (Exception e) {
            rollbackQuietly(conn);
            logger.error("Error creating customer", e);
            throw e;
        } finally {
            closeQuietly(conn);
        }
    }

    /**
     * Updates an existing customer.
     * Manages transaction explicitly: disables autocommit, commits on success, rollbacks on failure.
     *
     * @param id  the customer ID
     * @param dto the updated customer data
     * @return the updated customer
     * @throws ResourceNotFoundException if customer not found
     * @throws BusinessException         if validation fails
     */
    public CustomerDTO updateCustomer(Long id, CustomerDTO dto) {
        logger.info("Updating customer with id: {}", id);

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            Customer existingCustomer = customerRepository.findById(id, conn)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", id));

            // Business rule: if email changed, check uniqueness
            if (!existingCustomer.getEmail().equals(dto.getEmail()) &&
                    customerRepository.existsByEmail(dto.getEmail(), conn)) {
                throw new BusinessException("Email already registered: " + dto.getEmail());
            }

            // Business rule: if CPF/CNPJ changed, check uniqueness
            if (!existingCustomer.getCpfCnpj().equals(dto.getCpfCnpj()) &&
                    customerRepository.existsByCpfCnpj(dto.getCpfCnpj(), conn)) {
                throw new BusinessException("CPF/CNPJ already registered: " + dto.getCpfCnpj());
            }

            existingCustomer.setName(dto.getName());
            existingCustomer.setEmail(dto.getEmail());
            existingCustomer.setCpfCnpj(dto.getCpfCnpj());
            existingCustomer.setPhone(dto.getPhone());

            Customer updatedCustomer = customerRepository.update(existingCustomer, conn);

            conn.commit();
            logger.info("Customer updated with id: {}", updatedCustomer.getId());
            return toDTO(updatedCustomer);

        } catch (SQLException e) {
            rollbackQuietly(conn);
            logger.error("Database error updating customer", e);
            throw new RuntimeException("Database error updating customer", e);
        } catch (BusinessException | ResourceNotFoundException e) {
            rollbackQuietly(conn);
            throw e;
        } catch (Exception e) {
            rollbackQuietly(conn);
            logger.error("Error updating customer", e);
            throw e;
        } finally {
            closeQuietly(conn);
        }
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
     * Manages transaction explicitly: disables autocommit, commits on success, rollbacks on failure.
     *
     * @param id the customer ID
     * @throws ResourceNotFoundException if customer not found
     */
    public void deleteCustomer(Long id) {
        logger.info("Deleting customer with id: {}", id);

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            if (!customerRepository.existsById(id, conn)) {
                throw new ResourceNotFoundException("Customer", id);
            }

            customerRepository.deleteById(id, conn);

            conn.commit();
            logger.info("Customer deleted with id: {}", id);

        } catch (SQLException e) {
            rollbackQuietly(conn);
            logger.error("Database error deleting customer", e);
            throw new RuntimeException("Database error deleting customer", e);
        } catch (ResourceNotFoundException e) {
            rollbackQuietly(conn);
            throw e;
        } catch (Exception e) {
            rollbackQuietly(conn);
            logger.error("Error deleting customer", e);
            throw e;
        } finally {
            closeQuietly(conn);
        }
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Rolls back the connection silently, ignoring any errors.
     */
    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
                logger.debug("Transaction rolled back");
            } catch (SQLException e) {
                logger.warn("Error rolling back transaction", e);
            }
        }
    }

    /**
     * Closes the connection silently, ignoring any errors.
     */
    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.warn("Error closing connection", e);
            }
        }
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
