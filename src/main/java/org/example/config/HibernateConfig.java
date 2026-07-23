package org.example.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.orm.jpa.hibernate.HibernateTransactionManager;
import org.springframework.orm.jpa.hibernate.LocalSessionFactoryBean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@PropertySource("classpath:application.properties")
public class HibernateConfig {

    private final DatabaseConfigurationProperties databaseConfigurationProperties;
    private final HibernateConfigurationProperties hibernateConfigurationProperties;

    public HibernateConfig(DatabaseConfigurationProperties databaseConfigurationProperties, HibernateConfigurationProperties hibernateConfigurationProperties) {
        this.databaseConfigurationProperties = databaseConfigurationProperties;
        this.hibernateConfigurationProperties = hibernateConfigurationProperties;
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource());
        sessionFactory.setPackagesToScan("org.example");
        sessionFactory.setHibernateProperties(hibernateProperties());
        return sessionFactory;
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(databaseConfigurationProperties.driver());
        dataSource.setUrl(databaseConfigurationProperties.url());
        dataSource.setUsername(databaseConfigurationProperties.username());
        dataSource.setPassword(databaseConfigurationProperties.password());
        return dataSource;
    }

    private Properties hibernateProperties() {
        Properties properties = new Properties();
        properties.put("hibernate.dialect", hibernateConfigurationProperties.dialect());
        properties.put("hibernate.show_sql", hibernateConfigurationProperties.showSql());
        properties.put("hibernate.format_sql", hibernateConfigurationProperties.formatSql());
        properties.put("hibernate.generate_statistics", hibernateConfigurationProperties.generateStatistics());
        properties.put("hibernate.hbm2ddl.auto", hibernateConfigurationProperties.hbm2ddlAuto());
        return properties;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        HibernateTransactionManager txManager = new HibernateTransactionManager();
        txManager.setSessionFactory(sessionFactory().getObject());
        return txManager;
    }
}
