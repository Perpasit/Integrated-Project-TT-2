package sit.int221.integratedapi;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource primaryDataSource() {
        return primaryDataSourceProperties().initializeDataSourceBuilder()
                .type(DriverManagerDataSource.class).build();
    }

    @Bean
    @Qualifier("loginDataSource")
    @ConfigurationProperties("spring.datasource.login")
    public DataSourceProperties loginDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Qualifier("loginDataSource")
    public DataSource loginDataSource() {
        return loginDataSourceProperties().initializeDataSourceBuilder()
                .type(DriverManagerDataSource.class).build();
    }
}
