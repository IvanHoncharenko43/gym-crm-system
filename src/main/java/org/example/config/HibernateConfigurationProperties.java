package org.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record HibernateConfigurationProperties(
        @Value("${hibernate.dialect}") String dialect,
        @Value("${hibernate.show_sql}") String showSql,
        @Value("${hibernate.format_sql}") String formatSql,
        @Value("${hibernate.generate_statistics}") String generateStatistics,
        @Value("${hibernate.hbm2ddl.auto}") String hbm2ddlAuto
) {
}
