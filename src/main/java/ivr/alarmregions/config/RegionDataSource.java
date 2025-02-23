package ivr.alarmregions.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class RegionDataSource {

    @Bean
    @ConfigurationProperties("spring.datasource.region")
    public DataSourceProperties regionDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource regionDataSource() {
        return regionDataSourceProperties().initializeDataSourceBuilder()
                .build();
    }

}
