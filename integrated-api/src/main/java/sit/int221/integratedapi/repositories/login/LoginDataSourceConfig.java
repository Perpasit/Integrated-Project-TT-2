package sit.int221.integratedapi.repositories.login;

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
        basePackages = "sit.int221.integratedapi.repositories.login",
        entityManagerFactoryRef = "loginEntityManagerFactory",
        transactionManagerRef = "loginTransactionManager"
)
public class LoginDataSourceConfig {

    private final DataSource loginDataSource;

    public LoginDataSourceConfig(@Qualifier("loginDataSource") DataSource loginDataSource) {
        this.loginDataSource = loginDataSource;
    }

    @Bean(name = "loginEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean loginEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(loginDataSource)
                .packages("sit.int221.integratedapi.entities")
                .persistenceUnit("login")
                .build();
    }

    @Bean(name = "loginTransactionManager")
    public PlatformTransactionManager loginTransactionManager(
            @Qualifier("loginEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
