package com.wallet.ledger.adapter.persistence;

import com.wallet.ledger.application.port.FindProvisioningServicesPort;
import com.wallet.ledger.application.port.FindServiceBundlesPort;
import com.wallet.ledger.application.port.SaveProvisioningServicePort;
import com.wallet.ledger.application.port.SaveServiceBundlePort;
import com.wallet.ledger.domain.entity.ProvisioningService;
import com.wallet.ledger.domain.entity.ServiceBundle;
import com.wallet.ledger.domain.valueobject.AmountFilter;
import com.wallet.ledger.domain.valueobject.ProvisioningServiceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProvisioningPersistenceAdapter implements FindProvisioningServicesPort, FindServiceBundlesPort, SaveProvisioningServicePort, SaveServiceBundlePort {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<ProvisioningService> SERVICE_ROW_MAPPER = (rs, rowNum) -> ProvisioningService.builder()
            .id(UUID.fromString(rs.getString("id")))
            .type(ProvisioningServiceType.valueOf(rs.getString("type")))
            .name(rs.getString("name"))
            .code(rs.getString("code"))
            .subcategory(rs.getString("subcategory"))
            .description(rs.getString("description"))
            .status(rs.getString("status"))
            .createdAt(toInstant(rs.getTimestamp("created_at")))
            .build();

    private static final RowMapper<ServiceBundle> BUNDLE_ROW_MAPPER = (rs, rowNum) -> ServiceBundle.builder()
            .id(UUID.fromString(rs.getString("id")))
            .provisioningServiceId(UUID.fromString(rs.getString("provisioning_service_id")))
            .name(rs.getString("name"))
            .code(rs.getString("code"))
            .amountMin(rs.getBigDecimal("amount_min"))
            .amountMax(rs.getBigDecimal("amount_max"))
            .fixedAmount(rs.getBigDecimal("fixed_amount"))
            .subcategory(rs.getString("subcategory"))
            .validityDays(rs.getObject("validity_days", Integer.class))
            .description(rs.getString("description"))
            .status(rs.getString("status"))
            .createdAt(toInstant(rs.getTimestamp("created_at")))
            .build();

    private static Instant toInstant(Timestamp ts) {
        return ts == null ? null : ts.toInstant();
    }

    @Override
    public List<ProvisioningService> findAll(String serviceType, String subcategory) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, type, name, code, subcategory, description, status, created_at FROM provisioning_service WHERE status = 'ACTIVE'");
        List<Object> args = new ArrayList<>();
        if (serviceType != null && !serviceType.isBlank()) {
            sql.append(" AND type = ?");
            args.add(serviceType);
        }
        if (subcategory != null && !subcategory.isBlank()) {
            sql.append(" AND subcategory = ?");
            args.add(subcategory);
        }
        sql.append(" ORDER BY type, name");
        return jdbcTemplate.query(sql.toString(), SERVICE_ROW_MAPPER, args.toArray());
    }

    @Override
    public Optional<ProvisioningService> findById(UUID id) {
        List<ProvisioningService> list = jdbcTemplate.query(
                "SELECT id, type, name, code, subcategory, description, status, created_at FROM provisioning_service WHERE id = ?",
                SERVICE_ROW_MAPPER, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<ServiceBundle> findBundles(String serviceType, BigDecimal amount, AmountFilter amountFilter,
                                          String subcategory, UUID serviceId) {
        StringBuilder sql = new StringBuilder("""
                SELECT b.id, b.provisioning_service_id, b.name, b.code, b.amount_min, b.amount_max, b.fixed_amount,
                       b.subcategory, b.validity_days, b.description, b.status, b.created_at
                FROM service_bundle b
                JOIN provisioning_service s ON s.id = b.provisioning_service_id
                WHERE b.status = 'ACTIVE' AND s.status = 'ACTIVE'
                """);
        List<Object> args = new ArrayList<>();
        if (serviceType != null && !serviceType.isBlank()) {
            sql.append(" AND s.type = ?");
            args.add(serviceType);
        }
        if (serviceId != null) {
            sql.append(" AND b.provisioning_service_id = ?");
            args.add(serviceId);
        }
        if (subcategory != null && !subcategory.isBlank()) {
            sql.append(" AND b.subcategory = ?");
            args.add(subcategory);
        }
        // Bundles have fixed amount; filter by greater-than or less-than the given amount
        if (amount != null && amountFilter != null) {
            sql.append(" AND b.fixed_amount IS NOT NULL");
            if (amountFilter == AmountFilter.GREATER_THAN) {
                sql.append(" AND b.fixed_amount > ?");
                args.add(amount);
            } else if (amountFilter == AmountFilter.LESS_THAN) {
                sql.append(" AND b.fixed_amount < ?");
                args.add(amount);
            }
        }
        sql.append(" ORDER BY s.type, b.name");
        return jdbcTemplate.query(sql.toString(), BUNDLE_ROW_MAPPER, args.toArray());
    }

    @Override
    public Optional<ServiceBundle> findBundleById(UUID bundleId) {
        List<ServiceBundle> list = jdbcTemplate.query(
                "SELECT id, provisioning_service_id, name, code, amount_min, amount_max, fixed_amount, subcategory, validity_days, description, status, created_at FROM service_bundle WHERE id = ?",
                BUNDLE_ROW_MAPPER, bundleId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public ProvisioningService create(ProvisioningService service) {
        UUID id = service.getId() != null ? service.getId() : UUID.randomUUID();
        Instant now = service.getCreatedAt() != null ? service.getCreatedAt() : Instant.now();
        jdbcTemplate.update(
                "INSERT INTO provisioning_service (id, type, name, code, subcategory, description, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                id, service.getType().name(), service.getName(), service.getCode(),
                service.getSubcategory(), service.getDescription(),
                service.getStatus() != null ? service.getStatus() : "ACTIVE",
                Timestamp.from(now));
        log.debug("Created provisioning service id={} code={}", id, service.getCode());
        return ProvisioningService.builder()
                .id(id)
                .type(service.getType())
                .name(service.getName())
                .code(service.getCode())
                .subcategory(service.getSubcategory())
                .description(service.getDescription())
                .status(service.getStatus() != null ? service.getStatus() : "ACTIVE")
                .createdAt(now)
                .build();
    }

    @Override
    public ProvisioningService update(UUID id, ProvisioningService service) {
        int updated = jdbcTemplate.update(
                "UPDATE provisioning_service SET type = ?, name = ?, code = ?, subcategory = ?, description = ?, status = ? WHERE id = ?",
                service.getType().name(), service.getName(), service.getCode(),
                service.getSubcategory(), service.getDescription(), service.getStatus(), id);
        if (updated == 0) {
            throw new IllegalArgumentException("Provisioning service not found: " + id);
        }
        log.debug("Updated provisioning service id={}", id);
        return findById(id).orElseThrow(() -> new IllegalStateException("Provisioning service not found after update: " + id));
    }

    @Override
    public ServiceBundle create(ServiceBundle bundle) {
        UUID id = bundle.getId() != null ? bundle.getId() : UUID.randomUUID();
        Instant now = bundle.getCreatedAt() != null ? bundle.getCreatedAt() : Instant.now();
        String status = bundle.getStatus() != null ? bundle.getStatus() : "ACTIVE";
        jdbcTemplate.update(
                "INSERT INTO service_bundle (id, provisioning_service_id, name, code, amount_min, amount_max, fixed_amount, subcategory, validity_days, description, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, bundle.getProvisioningServiceId(), bundle.getName(), bundle.getCode(),
                bundle.getAmountMin(), bundle.getAmountMax(), bundle.getFixedAmount(),
                bundle.getSubcategory(), bundle.getValidityDays(), bundle.getDescription(),
                status, Timestamp.from(now));
        log.debug("Created service bundle id={} code={} serviceId={}", id, bundle.getCode(), bundle.getProvisioningServiceId());
        return ServiceBundle.builder()
                .id(id)
                .provisioningServiceId(bundle.getProvisioningServiceId())
                .name(bundle.getName())
                .code(bundle.getCode())
                .amountMin(bundle.getAmountMin())
                .amountMax(bundle.getAmountMax())
                .fixedAmount(bundle.getFixedAmount())
                .subcategory(bundle.getSubcategory())
                .validityDays(bundle.getValidityDays())
                .description(bundle.getDescription())
                .status(status)
                .createdAt(now)
                .build();
    }

    @Override
    public ServiceBundle update(UUID id, ServiceBundle bundle) {
        int updated = jdbcTemplate.update(
                "UPDATE service_bundle SET provisioning_service_id = ?, name = ?, code = ?, amount_min = ?, amount_max = ?, fixed_amount = ?, subcategory = ?, validity_days = ?, description = ?, status = ? WHERE id = ?",
                bundle.getProvisioningServiceId(), bundle.getName(), bundle.getCode(),
                bundle.getAmountMin(), bundle.getAmountMax(), bundle.getFixedAmount(),
                bundle.getSubcategory(), bundle.getValidityDays(), bundle.getDescription(),
                bundle.getStatus(), id);
        if (updated == 0) {
            throw new IllegalArgumentException("Service bundle not found: " + id);
        }
        log.debug("Updated service bundle id={}", id);
        return findBundleById(id).orElseThrow(() -> new IllegalStateException("Service bundle not found after update: " + id));
    }
}
