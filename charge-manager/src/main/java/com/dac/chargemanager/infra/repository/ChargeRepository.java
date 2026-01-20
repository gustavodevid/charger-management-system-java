package com.dac.chargemanager.infra.repository;

import com.dac.chargemanager.infra.entity.Charge;
import com.dac.chargemanager.infra.entity.ChargeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Charge entity using pure JDBC.
 */
public class ChargeRepository {

    private static final Logger logger = LoggerFactory.getLogger(ChargeRepository.class);
    private final DataSource dataSource;

    public ChargeRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Creates a new charge in the database.
     */
    public Charge save(Charge charge) {
        String sql = """
            INSERT INTO charge (customer_id, external_id, value, due_date, billing_type, 
                               status, description, pix_code, boleto_code, invoice_url, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        LocalDateTime now = LocalDateTime.now();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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
     * Updates an existing charge.
     */
    public Charge update(Charge charge) {
        String sql = """
            UPDATE charge SET customer_id = ?, external_id = ?, value = ?, due_date = ?, 
                             billing_type = ?, status = ?, description = ?, pix_code = ?, 
                             boleto_code = ?, invoice_url = ?, updated_at = ?
            WHERE id = ?
            """;
        LocalDateTime now = LocalDateTime.now();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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
     * Updates only the status of a charge.
     */
    public boolean updateStatus(Long id, ChargeStatus status) {
        String sql = "UPDATE charge SET status = ?, updated_at = ? WHERE id = ?";
        LocalDateTime now = LocalDateTime.now();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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
     * Updates status by external ID.
     */
    public boolean updateStatusByExternalId(String externalId, ChargeStatus status) {
        String sql = "UPDATE charge SET status = ?, updated_at = ? WHERE external_id = ?";
        LocalDateTime now = LocalDateTime.now();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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
     * Finds a charge by ID.
     */
    public Optional<Charge> findById(Long id) {
        String sql = "SELECT * FROM charge WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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
     * Finds a charge by external ID.
     */
    public Optional<Charge> findByExternalId(String externalId) {
        String sql = "SELECT * FROM charge WHERE external_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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
     * Finds all charges for a customer.
     */
    public List<Charge> findByCustomerId(Long customerId) {
        String sql = "SELECT * FROM charge WHERE customer_id = ? ORDER BY created_at DESC";
        List<Charge> charges = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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
     * Finds all charges with a specific status.
     */
    public List<Charge> findByStatus(ChargeStatus status) {
        String sql = "SELECT * FROM charge WHERE status = ? ORDER BY created_at DESC";
        List<Charge> charges = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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
     * Returns all charges.
     */
    public List<Charge> findAll() {
        String sql = "SELECT * FROM charge ORDER BY created_at DESC";
        List<Charge> charges = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
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
     * Deletes a charge by ID.
     */
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM charge WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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
     * Checks if a charge exists by ID.
     */
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM charge WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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
