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

    private final DatabaseProperties databaseProperties;
    private final HibernateProperties hibernateProperties;

    public HibernateConfig(DatabaseProperties databaseProperties, HibernateProperties hibernateProperties) {
        this.databaseProperties = databaseProperties;
        this.hibernateProperties = hibernateProperties;
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
        dataSource.setDriverClassName(databaseProperties.driver());
        dataSource.setUrl(databaseProperties.url());
        dataSource.setUsername(databaseProperties.username());
        dataSource.setPassword(databaseProperties.password());
        return dataSource;
    }

    private Properties hibernateProperties() {
        Properties properties = new Properties();
        properties.put("hibernate.dialect", hibernateProperties.dialect());
        properties.put("hibernate.show_sql", hibernateProperties.showSql());
        properties.put("hibernate.format_sql", hibernateProperties.formatSql());
        properties.put("hibernate.generate_statistics", hibernateProperties.generateStatistics());
        properties.put("hibernate.hbm2ddl.auto", hibernateProperties.hbm2ddlAuto());
        return properties;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        HibernateTransactionManager txManager = new HibernateTransactionManager();
        txManager.setSessionFactory(sessionFactory().getObject());
        return txManager;
    }
}
