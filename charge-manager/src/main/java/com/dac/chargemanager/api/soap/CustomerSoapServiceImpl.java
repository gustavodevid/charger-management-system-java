package com.dac.chargemanager.api.soap;

import com.dac.chargemanager.api.soap.model.CustomerListResponse;
import com.dac.chargemanager.api.soap.model.CustomerRequest;
import com.dac.chargemanager.api.soap.model.CustomerResponse;
import com.dac.chargemanager.business.dto.CustomerDTO;
import com.dac.chargemanager.business.exception.BusinessException;
import com.dac.chargemanager.business.exception.ResourceNotFoundException;
import com.dac.chargemanager.business.service.CustomerService;
import com.dac.chargemanager.infra.config.ServiceLocator;
import jakarta.jws.WebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the Customer SOAP service.
 * API Layer - handles SOAP requests and delegates to business layer.
 */
@WebService(
        serviceName = "CustomerService",
        portName = "CustomerPort",
        targetNamespace = "http://chargemanager.dac.com/soap",
        endpointInterface = "com.dac.chargemanager.api.soap.CustomerSoapService"
)
public class CustomerSoapServiceImpl implements CustomerSoapService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerSoapServiceImpl.class);

    private CustomerService customerService;
    private DataSource dataSource;

    /**
     * Default constructor required by JAX-WS.
     * Services are obtained from ServiceLocator.
     */
    public CustomerSoapServiceImpl() {
        // Services will be lazily initialized from ServiceLocator
    }

    /**
     * Constructor for manual dependency injection.
     */
    public CustomerSoapServiceImpl(CustomerService customerService, DataSource dataSource) {
        this.customerService = customerService;
        this.dataSource = dataSource;
    }

    private CustomerService getCustomerService() {
        if (customerService == null) {
            customerService = ServiceLocator.getCustomerService();
        }
        return customerService;
    }

    private DataSource getDataSource() {
        if (dataSource == null) {
            dataSource = ServiceLocator.getDataSource();
        }
        return dataSource;
    }

    @Override
    public CustomerResponse createCustomer(CustomerRequest request) {
        logger.info("SOAP createCustomer - name: {}, email: {}", request.getName(), request.getEmail());

        try {
            CustomerDTO dto = new CustomerDTO();
            dto.setName(request.getName());
            dto.setEmail(request.getEmail());
            dto.setCpfCnpj(request.getCpfCnpj());
            dto.setPhone(request.getPhone());

            CustomerDTO created = getCustomerService().createCustomer(dto);
            logger.info("Customer created with ID: {}", created.getId());

            return toResponse(created);

        } catch (BusinessException e) {
            logger.warn("Business error creating customer: {}", e.getMessage());
            return CustomerResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating customer: ", e);
            return CustomerResponse.error("Failed to create customer: " + e.getMessage());
        }
    }

    @Override
    public CustomerResponse getCustomer(Long customerId) {
        logger.info("SOAP getCustomer - id: {}", customerId);

        try {
            CustomerDTO customer = getCustomerService().getCustomerById(customerId);
            return toResponse(customer);

        } catch (ResourceNotFoundException e) {
            logger.warn("Customer not found: {}", customerId);
            return CustomerResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Error getting customer: ", e);
            return CustomerResponse.error("Failed to get customer: " + e.getMessage());
        }
    }

    @Override
    public CustomerListResponse getAllCustomers() {
        logger.info("SOAP getAllCustomers");

        try {
            List<CustomerDTO> customers = getCustomerService().getAllCustomers();
            List<CustomerResponse> responses = customers.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());

            logger.info("Found {} customers", responses.size());
            return CustomerListResponse.success(responses);

        } catch (Exception e) {
            logger.error("Error getting customers: ", e);
            return CustomerListResponse.error("Failed to get customers: " + e.getMessage());
        }
    }

    @Override
    public CustomerResponse updateCustomer(Long customerId, CustomerRequest request) {
        logger.info("SOAP updateCustomer - id: {}", customerId);

        try {
            CustomerDTO dto = new CustomerDTO();
            dto.setName(request.getName());
            dto.setEmail(request.getEmail());
            dto.setCpfCnpj(request.getCpfCnpj());
            dto.setPhone(request.getPhone());

            CustomerDTO updated = getCustomerService().updateCustomer(customerId, dto);
            logger.info("Customer updated with ID: {}", updated.getId());

            return toResponse(updated);

        } catch (ResourceNotFoundException e) {
            logger.warn("Customer not found for update: {}", customerId);
            return CustomerResponse.error(e.getMessage());
        } catch (BusinessException e) {
            logger.warn("Business error updating customer: {}", e.getMessage());
            return CustomerResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating customer: ", e);
            return CustomerResponse.error("Failed to update customer: " + e.getMessage());
        }
    }

    @Override
    public CustomerResponse deleteCustomer(Long customerId) {
        logger.info("SOAP deleteCustomer - id: {}", customerId);

        try {
            getCustomerService().deleteCustomer(customerId);
            logger.info("Customer deleted with ID: {}", customerId);

            CustomerResponse response = new CustomerResponse();
            response.setSuccess(true);
            response.setId(customerId);
            return response;

        } catch (ResourceNotFoundException e) {
            logger.warn("Customer not found for deletion: {}", customerId);
            return CustomerResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting customer: ", e);
            return CustomerResponse.error("Failed to delete customer: " + e.getMessage());
        }
    }

    @Override
    public String healthCheck() {
        logger.debug("SOAP healthCheck");

        try (Connection conn = getDataSource().getConnection()) {
            conn.createStatement().execute("SELECT 1");
            return "Charge Manager SOAP Service is UP - Database: OK - " + java.time.LocalDateTime.now();
        } catch (Exception e) {
            return "Charge Manager SOAP Service is UP - Database: DOWN - " + e.getMessage();
        }
    }

    /**
     * Converts CustomerDTO to CustomerResponse.
     */
    private CustomerResponse toResponse(CustomerDTO dto) {
        return CustomerResponse.success(
                dto.getId(),
                dto.getName(),
                dto.getEmail(),
                dto.getCpfCnpj(),
                dto.getPhone(),
                dto.getCreatedAt() != null ? dto.getCreatedAt().toString() : null,
                dto.getUpdatedAt() != null ? dto.getUpdatedAt().toString() : null
        );
    }
}
