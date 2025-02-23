package ivr.alarmregions.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Objects;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "ivr.alarmregions.entity",
        entityManagerFactoryRef = "regionEntityManagerFactory",
        transactionManagerRef = "regionTransactionManager")
public class RegionJpaConfig {

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean regionEntityManagerFactory(
            @Qualifier("regionDataSource") DataSource dataSource,
            EntityManagerFactoryBuilder builder
    ) {
        return builder
                .dataSource(dataSource)
                .packages("ivr.alarmregions.entity")
                .build();
    }

    @Bean
    @Primary
    public PlatformTransactionManager regionTransactionManager(
            @Qualifier("regionEntityManagerFactory") LocalContainerEntityManagerFactoryBean regionEntityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(regionEntityManagerFactory.getObject()));
    }
}
