package org.example.crm.monitoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.crm.config.DbPrivilegesConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@EnableConfigurationProperties(DbPrivilegesConfigurationProperties.class)
@RequiredArgsConstructor
public class DbPrivilegesHealthIndicator implements HealthIndicator {

    private static final Set<String> DEFAULT_REQUIRED_PRIVILEGES =
            Set.of("SELECT", "INSERT", "UPDATE");
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final DbPrivilegesConfigurationProperties dbPrivilegesProperties;

    @Override
    public Health health() {
        Set<String> configuredTables = dbPrivilegesProperties.requiredTables();
        if (configuredTables == null || configuredTables.isEmpty()) {
            return Health.up()
                    .withDetail("message", "No database tables configured for privilege verification")
                    .build();
        }
        try{
            Set<String> targetTables = configuredTables.stream()
                    .map(String::toLowerCase)
                    .filter(name -> !name.isBlank())
                    .collect(Collectors.toSet());
            if (targetTables.isEmpty()) {
                return Health.up()
                        .withDetail("message", "No database tables configured for privilege verification")
                        .build();
            }
            Map<String, Set<String>> tablePrivileges = fetchGrantedPrivileges(targetTables);
            Map<String, Set<String>> requiredPrivilegesByTable = dbPrivilegesProperties.requiredPrivilegesByTable();
            Map<String, Object> details = new HashMap<>();
            boolean allOk = true;
            for (String table : targetTables) {
                Set<String> granted = tablePrivileges.getOrDefault(table, Set.of());
                Set<String> requiredPrivileges = requiredPrivilegesByTable.getOrDefault(table, DEFAULT_REQUIRED_PRIVILEGES)
                        .stream()
                        .map(String::toUpperCase)
                        .collect(Collectors.toSet());
                if (granted.isEmpty()) {
                    details.put(table, "NO_PRIVILEGES (Required: " + requiredPrivileges + ")");
                    allOk = false;
                    continue;
                }
                if (granted.containsAll(requiredPrivileges)) {
                    details.put(table, "OK (Privileges: " + granted + ")");
                } else {
                    allOk = false;
                    details.put(table, "MISSING_REQUIRED_PRIVILEGES (Required: " + requiredPrivileges
                            + ", Current: " + granted + ")");
                }
            }
            if (allOk) {
                return Health.up()
                        .withDetail("message", "Database user has all required privileges for configured tables")
                        .withDetails(details)
                        .build();
            }
            log.warn("Database privilege health check failed. Details: {}", details);
            return Health.down()
                    .withDetail("error", "Missing database privileges for one or more critical tables")
                    .withDetails(details)
                    .build();
        } catch (Exception e) {
            log.error("Failed to execute database privileges health check", e);
            return Health.down(e)
                    .withDetail("error", "Failed to connect to database or query system catalog")
                    .build();
        }
    }

    private Map<String, Set<String>> fetchGrantedPrivileges(Set<String> targetTables) {
        String sql = "SELECT table_name, privilege_type " +
                "FROM information_schema.table_privileges " +
                "WHERE table_schema = current_schema() " +
                "AND table_name IN (:tables) " +
                "AND grantee = current_user";

        MapSqlParameterSource parameters = new MapSqlParameterSource("tables", targetTables);
        return jdbcTemplate.query(sql, parameters, (rs, rowNum) -> Map.entry(
                        rs.getString("table_name").toLowerCase(),
                        rs.getString("privilege_type").toUpperCase()
                ))
                .stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toSet())
                ));
    }
}
