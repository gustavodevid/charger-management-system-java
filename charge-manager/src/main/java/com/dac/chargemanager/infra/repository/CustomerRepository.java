package com.dac.chargemanager.infra.repository;

import com.dac.chargemanager.infra.entity.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Customer entity using pure JDBC.
 * Implements data access layer with explicit SQL queries.
 * Supports explicit transaction management by accepting Connection parameter.
 */
@Repository
public class CustomerRepository {

    private static final Logger logger = LoggerFactory.getLogger(CustomerRepository.class);
    private final DataSource dataSource;

    @Autowired
    public CustomerRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // ==================== METHODS WITH CONNECTION PARAMETER ====================

    /**
     * Creates a new customer in the database using provided connection.
     *
     * @param customer the customer to create
     * @param conn the database connection (caller manages transaction)
     * @return the created customer with generated ID
     */
    public Customer save(Customer customer, Connection conn) {
        String sql = "INSERT INTO customer (name, email, cpf_cnpj, phone, created_at) VALUES (?, ?, ?, ?, ?)";
        LocalDateTime now = LocalDateTime.now();

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, customer.getName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getCpfCnpj());
            ps.setString(4, customer.getPhone());
            ps.setTimestamp(5, Timestamp.valueOf(now));

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating customer failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    customer.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating customer failed, no ID obtained.");
                }
            }

            customer.setCreatedAt(now);
            logger.debug("Customer saved with ID: {}", customer.getId());
            return customer;

        } catch (SQLException e) {
            logger.error("Error saving customer", e);
            throw new RuntimeException("Failed to save customer", e);
        }
    }

    /**
     * Updates an existing customer using provided connection.
     *
     * @param customer the customer to update
     * @param conn the database connection (caller manages transaction)
     * @return the updated customer
     */
    public Customer update(Customer customer, Connection conn) {
        String sql = "UPDATE customer SET name = ?, email = ?, cpf_cnpj = ?, phone = ?, updated_at = ? WHERE id = ?";
        LocalDateTime now = LocalDateTime.now();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, customer.getName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getCpfCnpj());
            ps.setString(4, customer.getPhone());
            ps.setTimestamp(5, Timestamp.valueOf(now));
            ps.setLong(6, customer.getId());

            ps.executeUpdate();
            customer.setUpdatedAt(now);
            logger.debug("Customer updated with ID: {}", customer.getId());
            return customer;

        } catch (SQLException e) {
            logger.error("Error updating customer", e);
            throw new RuntimeException("Failed to update customer", e);
        }
    }

    /**
     * Finds a customer by ID using provided connection.
     *
     * @param id the customer ID
     * @param conn the database connection (caller manages transaction)
     * @return optional containing the customer if found
     */
    public Optional<Customer> findById(Long id, Connection conn) {
        String sql = "SELECT * FROM customer WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToCustomer(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding customer by ID", e);
            throw new RuntimeException("Failed to find customer", e);
        }
    }

    /**
     * Finds a customer by email using provided connection.
     *
     * @param email the customer email
     * @param conn the database connection (caller manages transaction)
     * @return optional containing the customer if found
     */
    public Optional<Customer> findByEmail(String email, Connection conn) {
        String sql = "SELECT * FROM customer WHERE email = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToCustomer(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding customer by email", e);
            throw new RuntimeException("Failed to find customer", e);
        }
    }

    /**
     * Finds a customer by CPF/CNPJ using provided connection.
     *
     * @param cpfCnpj the customer CPF or CNPJ
     * @param conn the database connection (caller manages transaction)
     * @return optional containing the customer if found
     */
    public Optional<Customer> findByCpfCnpj(String cpfCnpj, Connection conn) {
        String sql = "SELECT * FROM customer WHERE cpf_cnpj = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cpfCnpj);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToCustomer(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding customer by CPF/CNPJ", e);
            throw new RuntimeException("Failed to find customer", e);
        }
    }

    /**
     * Returns all customers using provided connection.
     *
     * @param conn the database connection (caller manages transaction)
     * @return list of all customers
     */
    public List<Customer> findAll(Connection conn) {
        String sql = "SELECT * FROM customer ORDER BY id";
        List<Customer> customers = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                customers.add(mapRowToCustomer(rs));
            }

            return customers;

        } catch (SQLException e) {
            logger.error("Error finding all customers", e);
            throw new RuntimeException("Failed to find customers", e);
        }
    }

    /**
     * Deletes a customer by ID using provided connection.
     *
     * @param id the customer ID
     * @param conn the database connection (caller manages transaction)
     * @return true if deleted, false otherwise
     */
    public boolean deleteById(Long id, Connection conn) {
        String sql = "DELETE FROM customer WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            int rowsAffected = ps.executeUpdate();
            logger.debug("Customer deleted, rows affected: {}", rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Error deleting customer", e);
            throw new RuntimeException("Failed to delete customer", e);
        }
    }

    /**
     * Checks if a customer exists by ID using provided connection.
     *
     * @param id the customer ID
     * @param conn the database connection (caller manages transaction)
     * @return true if exists, false otherwise
     */
    public boolean existsById(Long id, Connection conn) {
        String sql = "SELECT COUNT(*) FROM customer WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

            return false;

        } catch (SQLException e) {
            logger.error("Error checking customer existence by ID", e);
            throw new RuntimeException("Failed to check customer existence", e);
        }
    }

    /**
     * Checks if a customer exists by email using provided connection.
     *
     * @param email the customer email
     * @param conn the database connection (caller manages transaction)
     * @return true if exists, false otherwise
     */
    public boolean existsByEmail(String email, Connection conn) {
        String sql = "SELECT COUNT(*) FROM customer WHERE email = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

            return false;

        } catch (SQLException e) {
            logger.error("Error checking customer existence by email", e);
            throw new RuntimeException("Failed to check customer existence", e);
        }
    }

    /**
     * Checks if a customer exists by CPF/CNPJ using provided connection.
     *
     * @param cpfCnpj the customer CPF or CNPJ
     * @param conn the database connection (caller manages transaction)
     * @return true if exists, false otherwise
     */
    public boolean existsByCpfCnpj(String cpfCnpj, Connection conn) {
        String sql = "SELECT COUNT(*) FROM customer WHERE cpf_cnpj = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cpfCnpj);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

            return false;

        } catch (SQLException e) {
            logger.error("Error checking customer existence by CPF/CNPJ", e);
            throw new RuntimeException("Failed to check customer existence", e);
        }
    }

    // ==================== CONVENIENCE METHODS (AUTO-MANAGED CONNECTION) ====================

    /**
     * Creates a new customer in the database.
     * Uses auto-managed connection (auto-commit enabled).
     *
     * @param customer the customer to create
     * @return the created customer with generated ID
     */
    public Customer save(Customer customer) {
        try (Connection conn = dataSource.getConnection()) {
            return save(customer, conn);
        } catch (SQLException e) {
            logger.error("Error obtaining connection", e);
            throw new RuntimeException("Failed to obtain database connection", e);
        }
    }

    /**
     * Updates an existing customer.
     * Uses auto-managed connection (auto-commit enabled).
     *
     * @param customer the customer to update
     * @return the updated customer
     */
    public Customer update(Customer customer) {
        try (Connection conn = dataSource.getConnection()) {
            return update(customer, conn);
        } catch (SQLException e) {
            logger.error("Error obtaining connection", e);
            throw new RuntimeException("Failed to obtain database connection", e);
        }
    }

    /**
     * Finds a customer by ID.
     * Uses auto-managed connection (auto-commit enabled).
     *
     * @param id the customer ID
     * @return optional containing the customer if found
     */
    public Optional<Customer> findById(Long id) {
        try (Connection conn = dataSource.getConnection()) {
            return findById(id, conn);
        } catch (SQLException e) {
            logger.error("Error obtaining connection", e);
            throw new RuntimeException("Failed to obtain database connection", e);
        }
    }

    /**
     * Finds a customer by email.
     * Uses auto-managed connection (auto-commit enabled).
     *
     * @param email the customer email
     * @return optional containing the customer if found
     */
    public Optional<Customer> findByEmail(String email) {
        try (Connection conn = dataSource.getConnection()) {
            return findByEmail(email, conn);
        } catch (SQLException e) {
            logger.error("Error obtaining connection", e);
            throw new RuntimeException("Failed to obtain database connection", e);
        }
    }

    /**
     * Finds a customer by CPF/CNPJ.
     * Uses auto-managed connection (auto-commit enabled).
     *
     * @param cpfCnpj the customer CPF or CNPJ
     * @return optional containing the customer if found
     */
    public Optional<Customer> findByCpfCnpj(String cpfCnpj) {
        try (Connection conn = dataSource.getConnection()) {
            return findByCpfCnpj(cpfCnpj, conn);
        } catch (SQLException e) {
            logger.error("Error obtaining connection", e);
            throw new RuntimeException("Failed to obtain database connection", e);
        }
    }

    /**
     * Returns all customers.
     * Uses auto-managed connection (auto-commit enabled).
     *
     * @return list of all customers
     */
    public List<Customer> findAll() {
        try (Connection conn = dataSource.getConnection()) {
            return findAll(conn);
        } catch (SQLException e) {
            logger.error("Error obtaining connection", e);
            throw new RuntimeException("Failed to obtain database connection", e);
        }
    }

    /**
     * Deletes a customer by ID.
     * Uses auto-managed connection (auto-commit enabled).
     *
     * @param id the customer ID
     * @return true if deleted, false otherwise
     */
    public boolean deleteById(Long id) {
        try (Connection conn = dataSource.getConnection()) {
            return deleteById(id, conn);
        } catch (SQLException e) {
            logger.error("Error obtaining connection", e);
            throw new RuntimeException("Failed to obtain database connection", e);
        }
    }

    /**
     * Checks if a customer exists by ID.
     * Uses auto-managed connection (auto-commit enabled).
     *
     * @param id the customer ID
     * @return true if exists, false otherwise
     */
    public boolean existsById(Long id) {
        try (Connection conn = dataSource.getConnection()) {
            return existsById(id, conn);
        } catch (SQLException e) {
            logger.error("Error obtaining connection", e);
            throw new RuntimeException("Failed to obtain database connection", e);
        }
    }

    /**
     * Checks if a customer exists by email.
     * Uses auto-managed connection (auto-commit enabled).
     *
     * @param email the customer email
     * @return true if exists, false otherwise
     */
    public boolean existsByEmail(String email) {
        try (Connection conn = dataSource.getConnection()) {
            return existsByEmail(email, conn);
        } catch (SQLException e) {
            logger.error("Error obtaining connection", e);
            throw new RuntimeException("Failed to obtain database connection", e);
        }
    }

    /**
     * Checks if a customer exists by CPF/CNPJ.
     * Uses auto-managed connection (auto-commit enabled).
     *
     * @param cpfCnpj the customer CPF or CNPJ
     * @return true if exists, false otherwise
     */
    public boolean existsByCpfCnpj(String cpfCnpj) {
        try (Connection conn = dataSource.getConnection()) {
            return existsByCpfCnpj(cpfCnpj, conn);
        } catch (SQLException e) {
            logger.error("Error obtaining connection", e);
            throw new RuntimeException("Failed to obtain database connection", e);
        }
    }

    /**
     * Maps a ResultSet row to a Customer entity.
     */
    private Customer mapRowToCustomer(ResultSet rs) throws SQLException {
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
    }
}
