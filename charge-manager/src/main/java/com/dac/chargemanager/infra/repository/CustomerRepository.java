package com.dac.chargemanager.infra.repository;

import com.dac.chargemanager.infra.entity.Customer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Customer entity using JDBC Template.
 * Implements data access layer with explicit SQL queries.
 */
@Repository
public class CustomerRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<Customer> CUSTOMER_ROW_MAPPER = (rs, rowNum) -> {
        Customer customer = new Customer();
        customer.setId(rs.getLong("id"));
        customer.setName(rs.getString("name"));
        customer.setEmail(rs.getString("email"));
        customer.setCpfCnpj(rs.getString("cpf_cnpj"));
        customer.setPhone(rs.getString("phone"));
        customer.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        customer.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : null);
        return customer;
    };

    public CustomerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Creates a new customer in the database.
     *
     * @param customer the customer to create
     * @return the created customer with generated ID
     */
    public Customer save(Customer customer) {
        String sql = "INSERT INTO customer (name, email, cpf_cnpj, phone, created_at) VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getCpfCnpj());
            ps.setString(4, customer.getPhone());
            ps.setTimestamp(5, Timestamp.valueOf(now));
            return ps;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId != null) {
            customer.setId(generatedId.longValue());
        }
        customer.setCreatedAt(now);

        return customer;
    }

    /**
     * Updates an existing customer.
     *
     * @param customer the customer to update
     * @return the updated customer
     */
    public Customer update(Customer customer) {
        String sql = "UPDATE customer SET name = ?, email = ?, cpf_cnpj = ?, phone = ?, updated_at = ? WHERE id = ?";
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update(sql,
                customer.getName(),
                customer.getEmail(),
                customer.getCpfCnpj(),
                customer.getPhone(),
                Timestamp.valueOf(now),
                customer.getId());

        customer.setUpdatedAt(now);
        return customer;
    }

    /**
     * Finds a customer by ID.
     *
     * @param id the customer ID
     * @return optional containing the customer if found
     */
    public Optional<Customer> findById(Long id) {
        String sql = "SELECT * FROM customer WHERE id = ?";
        List<Customer> customers = jdbcTemplate.query(sql, CUSTOMER_ROW_MAPPER, id);
        return customers.isEmpty() ? Optional.empty() : Optional.of(customers.get(0));
    }

    /**
     * Finds a customer by email.
     *
     * @param email the customer email
     * @return optional containing the customer if found
     */
    public Optional<Customer> findByEmail(String email) {
        String sql = "SELECT * FROM customer WHERE email = ?";
        List<Customer> customers = jdbcTemplate.query(sql, CUSTOMER_ROW_MAPPER, email);
        return customers.isEmpty() ? Optional.empty() : Optional.of(customers.get(0));
    }

    /**
     * Finds a customer by CPF/CNPJ.
     *
     * @param cpfCnpj the customer CPF or CNPJ
     * @return optional containing the customer if found
     */
    public Optional<Customer> findByCpfCnpj(String cpfCnpj) {
        String sql = "SELECT * FROM customer WHERE cpf_cnpj = ?";
        List<Customer> customers = jdbcTemplate.query(sql, CUSTOMER_ROW_MAPPER, cpfCnpj);
        return customers.isEmpty() ? Optional.empty() : Optional.of(customers.get(0));
    }

    /**
     * Returns all customers.
     *
     * @return list of all customers
     */
    public List<Customer> findAll() {
        String sql = "SELECT * FROM customer ORDER BY id";
        return jdbcTemplate.query(sql, CUSTOMER_ROW_MAPPER);
    }

    /**
     * Deletes a customer by ID.
     *
     * @param id the customer ID
     * @return true if deleted, false otherwise
     */
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM customer WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        return rowsAffected > 0;
    }

    /**
     * Checks if a customer exists by ID.
     *
     * @param id the customer ID
     * @return true if exists, false otherwise
     */
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM customer WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    /**
     * Checks if a customer exists by email.
     *
     * @param email the customer email
     * @return true if exists, false otherwise
     */
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM customer WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    /**
     * Checks if a customer exists by CPF/CNPJ.
     *
     * @param cpfCnpj the customer CPF or CNPJ
     * @return true if exists, false otherwise
     */
    public boolean existsByCpfCnpj(String cpfCnpj) {
        String sql = "SELECT COUNT(*) FROM customer WHERE cpf_cnpj = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, cpfCnpj);
        return count != null && count > 0;
    }
}

