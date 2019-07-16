package org.springframework.samples.petclinic.owner;

import java.util.Collection;

import org.gama.stalactite.persistence.engine.ColumnOptions.IdentifierPolicy;
import org.gama.stalactite.persistence.engine.PersistenceContext;
import org.gama.stalactite.persistence.engine.cascade.JoinedTablesPersister;
import org.gama.stalactite.persistence.structure.Table;
import org.gama.stalactite.query.model.Operators;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static org.gama.stalactite.persistence.engine.MappingEase.entityBuilder;

/**
 * @author Guillaume Mary
 */
@Repository
@Primary
public class OwnerRepositoryStit implements OwnerRepository {
    
    private final JoinedTablesPersister<Owner, Integer, Table> persister;
    
    @Autowired
    public OwnerRepositoryStit(PersistenceContext persistenceContext) {
        persister = entityBuilder(Owner.class, Integer.class)
            .add(Owner::getId).identifier(IdentifierPolicy.AFTER_INSERT)
            .add(Owner::getLastName, "last_name")
            .add(Owner::getFirstName, "first_name")
            .add(Owner::getTelephone)
            .add(Owner::getAddress)
            .add(Owner::getCity)
            .build(persistenceContext, new Table("owners"));
    }
    
    /**
     * Retrieve {@link Owner}s from the data store by last name, returning all owners
     * whose last name <i>starts</i> with the given name.
     * @param lastName Value to search for
     * @return a Collection of matching {@link Owner}s (or an empty Collection if none
     * found)
     */
    @Transactional(readOnly = true)
    @Override
    public Collection<Owner> findByLastName(@Param("lastName") String lastName) {
        return persister.selectWhere(Owner::getLastName, Operators.contains(lastName))
            .execute();
    }
    
    /**
     * Retrieve an {@link Owner} from the data store by id.
     * @param id the id to search for
     * @return the {@link Owner} if found
     */
    @Transactional(readOnly = true)
    @Override
    public Owner findById(@Param("id") Integer id) {
        return persister.select(id);
    }
    
    /**
     * Save an {@link Owner} to the data store, either inserting or updating it.
     * @param owner the {@link Owner} to save
     */
    @Override
    @Transactional
    public void save(Owner owner) {
        persister.persist(owner);
    }
}
