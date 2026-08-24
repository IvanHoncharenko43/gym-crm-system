package org.example.crm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Set;

@ConfigurationProperties(prefix = "management.health.db-privileges")
public record DbPrivilegesConfigurationProperties (
        Set<String> requiredTables,
        Map<String, Set<String>> requiredPrivilegesByTable
){
}
