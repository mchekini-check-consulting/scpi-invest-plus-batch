package fr.formationacademy.scpiinvestplusbatch.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class FlywayConfig {

    @Bean
    public Flyway flyway(@Qualifier("batchDataSource") DataSource batchDataSource) {
        return Flyway.configure()
                .dataSource(batchDataSource)
                .locations("classpath:db/migration")
                .load();
    }
}
