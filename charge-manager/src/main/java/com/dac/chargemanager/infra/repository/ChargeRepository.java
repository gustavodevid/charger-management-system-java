package com.dac.chargemanager.infra.repository;

import com.dac.chargemanager.infra.entity.Charge;
import com.dac.chargemanager.infra.entity.ChargeStatus;
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
 * Repository for Charge entity using pure JDBC.
 * Supports explicit transaction management by accepting Connection parameter.
 */
@Repository
public class ChargeRepository {

    private static final Logger logger = LoggerFactory.getLogger(ChargeRepository.class);
    private final DataSource dataSource;

    @Autowired
    public ChargeRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // ==================== METHODS WITH CONNECTION PARAMETER ====================

    /**
     * Creates a new charge in the database using provided connection.
     *
     * @param charge the charge to create
     * @param conn the database connection (caller manages transaction)
     * @return the created charge with generated ID
     */
    public Charge save(Charge charge, Connection conn) {
        String sql = """
            INSERT INTO charge (customer_id, external_id, value, due_date, billing_type, 
                               status, description, pix_code, boleto_code, invoice_url, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        LocalDateTime now = LocalDateTime.now();

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, charge.getCustomerId());
            ps.setString(2, charge.getExternalId());
            ps.setBigDecimal(3, charge.getValue());
            ps.setDate(4, Date.valueOf(charge.getDueDate()));
            ps.setString(5, charge.getBillingType());
            ps.setString(6, charge.getStatus().name());
            ps.setString(7, charge.getDescription());
            ps.setString(8, charge.getPixCode());
            ps.setString(9, charge.getBoletoCode());
            ps.setString(10, charge.getInvoiceUrl());
            ps.setTimestamp(11, Timestamp.valueOf(now));

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating charge failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    charge.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating charge failed, no ID obtained.");
                }
            }

            charge.setCreatedAt(now);
            logger.debug("Charge saved with ID: {}", charge.getId());
            return charge;

        } catch (SQLException e) {
            logger.error("Error saving charge", e);
            throw new RuntimeException("Failed to save charge", e);
        }
    }

    /**
     * Updates an existing charge using provided connection.
     *
     * @param charge the charge to update
     * @param conn the database connection (caller manages transaction)
     * @return the updated charge
     */
    public Charge update(Charge charge, Connection conn) {
        String sql = """
            UPDATE charge SET customer_id = ?, external_id = ?, value = ?, due_date = ?, 
                             billing_type = ?, status = ?, description = ?, pix_code = ?, 
                             boleto_code = ?, invoice_url = ?, updated_at = ?
            WHERE id = ?
            """;
        LocalDateTime now = LocalDateTime.now();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, charge.getCustomerId());
            ps.setString(2, charge.getExternalId());
            ps.setBigDecimal(3, charge.getValue());
            ps.setDate(4, Date.valueOf(charge.getDueDate()));
            ps.setString(5, charge.getBillingType());
            ps.setString(6, charge.getStatus().name());
            ps.setString(7, charge.getDescription());
            ps.setString(8, charge.getPixCode());
            ps.setString(9, charge.getBoletoCode());
            ps.setString(10, charge.getInvoiceUrl());
            ps.setTimestamp(11, Timestamp.valueOf(now));
            ps.setLong(12, charge.getId());

            ps.executeUpdate();
            charge.setUpdatedAt(now);
            logger.debug("Charge updated with ID: {}", charge.getId());
            return charge;

        } catch (SQLException e) {
            logger.error("Error updating charge", e);
            throw new RuntimeException("Failed to update charge", e);
        }
    }

    /**
     * Updates only the status of a charge using provided connection.
     *
     * @param id the charge ID
     * @param status the new status
     * @param conn the database connection (caller manages transaction)
     * @return true if updated, false otherwise
     */
    public boolean updateStatus(Long id, ChargeStatus status, Connection conn) {
        String sql = "UPDATE charge SET status = ?, updated_at = ? WHERE id = ?";
        LocalDateTime now = LocalDateTime.now();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setTimestamp(2, Timestamp.valueOf(now));
            ps.setLong(3, id);

            int rowsAffected = ps.executeUpdate();
            logger.debug("Charge status updated, rows affected: {}", rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Error updating charge status", e);
            throw new RuntimeException("Failed to update charge status", e);
        }
    }

    /**
     * Updates status by external ID using provided connection.
     *
     * @param externalId the external ID
     * @param status the new status
     * @param conn the database connection (caller manages transaction)
     * @return true if updated, false otherwise
     */
    public boolean updateStatusByExternalId(String externalId, ChargeStatus status, Connection conn) {
        String sql = "UPDATE charge SET status = ?, updated_at = ? WHERE external_id = ?";
        LocalDateTime now = LocalDateTime.now();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setTimestamp(2, Timestamp.valueOf(now));
            ps.setString(3, externalId);

            int rowsAffected = ps.executeUpdate();
            logger.debug("Charge status updated by external ID, rows affected: {}", rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Error updating charge status by external ID", e);
            throw new RuntimeException("Failed to update charge status", e);
        }
    }

    /**
     * Finds a charge by ID using provided connection.
     *
     * @param id the charge ID
     * @param conn the database connection (caller manages transaction)
     * @return optional containing the charge if found
     */
    public Optional<Charge> findById(Long id, Connection conn) {
        String sql = "SELECT * FROM charge WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToCharge(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding charge by ID", e);
            throw new RuntimeException("Failed to find charge", e);
        }
    }

    /**
     * Finds a charge by external ID using provided connection.
     *
     * @param externalId the external ID
     * @param conn the database connection (caller manages transaction)
     * @return optional containing the charge if found
     */
    public Optional<Charge> findByExternalId(String externalId, Connection conn) {
        String sql = "SELECT * FROM charge WHERE external_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, externalId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToCharge(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding charge by external ID", e);
            throw new RuntimeException("Failed to find charge", e);
        }
    }

    /**
     * Finds all charges for a customer using provided connection.
     *
     * @param customerId the customer ID
     * @param conn the database connection (caller manages transaction)
     * @return list of charges
     */
    public List<Charge> findByCustomerId(Long customerId, Connection conn) {
        String sql = "SELECT * FROM charge WHERE customer_id = ? ORDER BY created_at DESC";
        List<Charge> charges = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    charges.add(mapRowToCharge(rs));
                }
            }

            return charges;

        } catch (SQLException e) {
            logger.error("Error finding charges by customer ID", e);
            throw new RuntimeException("Failed to find charges", e);
        }
    }

    /**
     * Finds all charges with a specific status using provided connection.
     *
     * @param status the charge status
     * @param conn the database connection (caller manages transaction)
     * @return list of charges
     */
    public List<Charge> findByStatus(ChargeStatus status, Connection conn) {
        String sql = "SELECT * FROM charge WHERE status = ? ORDER BY created_at DESC";
        List<Charge> charges = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    charges.add(mapRowToCharge(rs));
                }
            }

            return charges;

        } catch (SQLException e) {
            logger.error("Error finding charges by status", e);
            throw new RuntimeException("Failed to find charges", e);
        }
    }

    /**
     * Returns all charges using provided connection.
     *
     * @param conn the database connection (caller manages transaction)
     * @return list of all charges
     */
    public List<Charge> findAll(Connection conn) {
        String sql = "SELECT * FROM charge ORDER BY created_at DESC";
        List<Charge> charges = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                charges.add(mapRowToCharge(rs));
            }

            return charges;

        } catch (SQLException e) {
            logger.error("Error finding all charges", e);
            throw new RuntimeException("Failed to find charges", e);
        }
    }

    /**
     * Deletes a charge by ID using provided connection.
     *
     * @param id the charge ID
     * @param conn the database connection (caller manages transaction)
     * @return true if deleted, false otherwise
     */
    public boolean deleteById(Long id, Connection conn) {
        String sql = "DELETE FROM charge WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            int rowsAffected = ps.executeUpdate();
            logger.debug("Charge deleted, rows affected: {}", rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Error deleting charge", e);
            throw new RuntimeException("Failed to delete charge", e);
        }
    }

    /**
     * Checks if a charge exists by ID using provided connection.
     *
     * @param id the charge ID
     * @param conn the database connection (caller manages transaction)
     * @return true if exists, false otherwise
     */
    public boolean existsById(Long id, Connection conn) {
        String sql = "SELECT COUNT(*) FROM charge WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

            return false;

        } catch (SQLException e) {
            logger.error("Error checking charge existence", e);
            throw new RuntimeException("Failed to check charge existence", e);
        }
    }

    // ==================== CONVENIENCE METHODS (AUTO-MANAGED CONNECTION) ====================

    /**
     * Creates a new charge in the database.
     * Uses auto-managed connection (auto-commit enabled).
     */
    public Charge save(Charge charge) {
        try (Connection conn = dataSource.getConnection()) {
            return save(charge, conn);
        } catch (SQLException e) {
            logger.error("Error obtaining connection", e);
            throw new RuntimeException("Failed to obtain database connection", e);
        }
    }

    /**
     * Updates an existing charge.
     * Uses auto-managed connection (auto-commit enabled).
     */
    public Charge update(Charge charge) {
        try (Connection conn = dataSource.getConnection()) {
            return update(charge, conn);
        } catch (SQLException e) {
            logger.error("Error obtaining connection", e);
            throw new RuntimeException("Failed to obtain database connection", e);
        }
    }

    /**
     * Updates only the status of a charge.
     * Uses auto-managed connection (auto-commit enabled).
     */
    public boolean updateStatus(Long id, ChargeStatus status) {
        try (Connection conn = dataSource.getConnection()) {
            return updateStatus(id, status, conn);
        } catch (SQLException e) {
            logger.error("Error obtaining connection", e);
            throw new RuntimeException("Failed to obtain database connection", e);
        }
    }

    /**
     * Updates status by external ID.
     * Uses auto-managed connection (auto-commit enabled).
     */
    public boolean updateStatusByExternalId(String externalId, ChargeStatus status) {
        try (Connection conn = dataSource.getConnection()) {
            return updateStatusByExternalId(externalId, status, conn);
        } catch (SQLException e) {
            logger.error("Error obtaining connection", e);
            throw new RuntimeException("Failed to obtain database connection", e);
        }
    }

    /**
     * Finds a charge by ID.
     * Uses auto-managed connection (auto-commit enabled).
     */
    public Optional<Charge> findById(Long id) {
        try (Connection conn = dataSource.getConnection()) {
            return findById(id, conn);
        } catch (SQLException e) {
            logger.error("Error obtaining connection", e);
            throw new RuntimeException("Failed to obtain database connection", e);
        }
    }

    /**
     * Finds a charge by external ID.
     * Uses auto-managed connection (auto-commit enabled).
     */
    public Optional<Charge> findByExternalId(String externalId) {
        try (Connection conn = dataSource.getConnection()) {
            return findByExternalId(externalId, conn);
        } catch (SQLException e) {
            logger.error("Error obtaining connection", e);
            throw new RuntimeException("Failed to obtain database connection", e);
        }
    }

    /**
     * Finds all charges for a customer.
     * Uses auto-managed connection (auto-commit enabled).
     */
    public List<Charge> findByCustomerId(Long customerId) {
        try (Connection conn = dataSource.getConnection()) {
            return findByCustomerId(customerId, conn);
        } catch (SQLException e) {
            logger.error("Error obtaining connection", e);
            throw new RuntimeException("Failed to obtain database connection", e);
        }
    }

    /**
     * Finds all charges with a specific status.
     * Uses auto-managed connection (auto-commit enabled).
     */
    public List<Charge> findByStatus(ChargeStatus status) {
        try (Connection conn = dataSource.getConnection()) {
            return findByStatus(status, conn);
        } catch (SQLException e) {
            logger.error("Error obtaining connection", e);
            throw new RuntimeException("Failed to obtain database connection", e);
        }
    }

    /**
     * Returns all charges.
     * Uses auto-managed connection (auto-commit enabled).
     */
    public List<Charge> findAll() {
        try (Connection conn = dataSource.getConnection()) {
            return findAll(conn);
        } catch (SQLException e) {
            logger.error("Error obtaining connection", e);
            throw new RuntimeException("Failed to obtain database connection", e);
        }
    }

    /**
     * Deletes a charge by ID.
     * Uses auto-managed connection (auto-commit enabled).
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
     * Checks if a charge exists by ID.
     * Uses auto-managed connection (auto-commit enabled).
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
     * Maps a ResultSet row to a Charge entity.
     */
    private Charge mapRowToCharge(ResultSet rs) throws SQLException {
        Charge charge = new Charge();
        charge.setId(rs.getLong("id"));
        charge.setCustomerId(rs.getLong("customer_id"));
        charge.setExternalId(rs.getString("external_id"));
        charge.setValue(rs.getBigDecimal("value"));
        charge.setDueDate(rs.getDate("due_date").toLocalDate());
        charge.setBillingType(rs.getString("billing_type"));
        charge.setStatus(ChargeStatus.fromString(rs.getString("status")));
        charge.setDescription(rs.getString("description"));
        charge.setPixCode(rs.getString("pix_code"));
        charge.setBoletoCode(rs.getString("boleto_code"));
        charge.setInvoiceUrl(rs.getString("invoice_url"));
        charge.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        charge.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : null);
        return charge;
    }
}
