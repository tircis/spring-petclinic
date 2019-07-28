package org.springframework.samples.petclinic.system;

import org.gama.stalactite.sql.spring.PlatformTransactionManagerConnectionProvider;
import org.gama.stalactite.persistence.engine.PersistenceContext;
import org.gama.stalactite.persistence.sql.HSQLDBDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author Guillaume Mary
 */
@Configuration
public class StalactiteConfiguration {
    
    @Bean
    @Scope("prototype") // prototype because same entity is registered multiple times and this is not allowed by Stalactite
    public PersistenceContext buildPersistenceContext(PlatformTransactionManager transactionManager) {
        return new PersistenceContext(new PlatformTransactionManagerConnectionProvider(transactionManager), new HSQLDBDialect());
    }
}
