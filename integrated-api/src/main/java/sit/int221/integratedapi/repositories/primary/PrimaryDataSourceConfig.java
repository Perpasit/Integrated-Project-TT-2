package sit.int221.integratedapi.repositories.primary;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackages = "sit.int221.integratedapi.repositories.primary",
        entityManagerFactoryRef = "primaryEntityManagerFactory",
        transactionManagerRef = "primaryTransactionManager"
)
public class PrimaryDataSourceConfig {

    private final DataSource primaryDataSource;

    public PrimaryDataSourceConfig(@Qualifier("primaryDataSource") DataSource primaryDataSource) {
        this.primaryDataSource = primaryDataSource;
    }

    @Bean(name = "primaryEntityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(primaryDataSource)
                .packages("sit.int221.integratedapi.entities")
                .persistenceUnit("primary")
                .build();
    }

    @Bean(name = "primaryTransactionManager")
    @Primary
    public PlatformTransactionManager primaryTransactionManager(
            @Qualifier("primaryEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
