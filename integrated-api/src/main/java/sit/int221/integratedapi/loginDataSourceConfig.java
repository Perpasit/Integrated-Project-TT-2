package sit.int221.integratedapi;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackages = "sit.int221.integratedapi.repositories",
        entityManagerFactoryRef = "loginEntityManagerFactory",
        transactionManagerRef = "loginTransactionManager"
)
public class loginDataSourceConfig {

    private final DataSource loginDataSource;

    public loginDataSourceConfig(@Qualifier("loginDataSource") DataSource loginDataSource) {
        this.loginDataSource = loginDataSource;
    }

    @Bean
    @Qualifier("loginEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean loginEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(loginDataSource)
                .packages("sit.int221.integratedapi.entities")
                .persistenceUnit("login")
                .build();
    }

    @Bean
    @Qualifier("loginTransactionManager")
    public PlatformTransactionManager loginTransactionManager(@Qualifier("loginEntityManagerFactory") EntityManagerFactory loginEntityManagerFactory) {
        return new JpaTransactionManager(loginEntityManagerFactory);
    }
}
