package org.springframework.samples.petclinic.owner;

import java.util.Collection;

import org.gama.stalactite.persistence.engine.CascadeOptions.RelationMode;
import org.gama.stalactite.persistence.engine.ColumnOptions.IdentifierPolicy;
import org.gama.stalactite.persistence.engine.PersistenceContext;
import org.gama.stalactite.persistence.engine.PersistenceContext.ExecutableUpdate;
import org.gama.stalactite.persistence.engine.cascade.JoinedTablesPersister;
import org.gama.stalactite.persistence.structure.Column;
import org.gama.stalactite.persistence.structure.Table;
import org.gama.stalactite.query.model.Operators;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.petclinic.visit.Visit;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static org.gama.stalactite.persistence.engine.MappingEase.entityBuilder;
import static org.gama.stalactite.query.model.Operators.eq;

/**
 * @author Guillaume Mary
 */
@Repository
@Primary
public class OwnerRepositoryStit implements OwnerRepository {
    
    private final JoinedTablesPersister<Owner, Integer, Table> persister;
    private final PersistenceContext persistenceContext;
    
    @Autowired
    public OwnerRepositoryStit(PersistenceContext persistenceContext) {
        this.persistenceContext = persistenceContext;
        
        Table visits = new Table("visits");
        Column<Table, Integer> pet_id = visits.addColumn("pet_id", Integer.class);
        persister = entityBuilder(Owner.class, Integer.class)
            .joinColumnNamingStrategy(memberDefinition -> memberDefinition.getName() + "_id")
            .add(Owner::getId).identifier(IdentifierPolicy.AFTER_INSERT)
            .add(Owner::getLastName, "last_name")
            .add(Owner::getFirstName, "first_name")
            .add(Owner::getTelephone)
            .add(Owner::getAddress)
            .add(Owner::getCity)
            .addOneToManySet(Owner::getPetsInternal,
                entityBuilder(Pet.class, Integer.class)
                .joinColumnNamingStrategy(memberDefinition -> memberDefinition.getName() + "_id")
                .add(Pet::getId).identifier(IdentifierPolicy.AFTER_INSERT)
                .add(Pet::getName)
                .add(Pet::getBirthDate, "birth_date")
                .addOneToManySet(Pet::getVisitsInternal,
                    entityBuilder(Visit.class, Integer.class)
                    .add(Visit::getId).identifier(IdentifierPolicy.AFTER_INSERT)
                    .add(Visit::getDate, "visit_date")
                    .add(Visit::getDescription), visits)
                .mappedBy(pet_id)
                .addOneToOne(Pet::getType,
                    entityBuilder(PetType.class, Integer.class)
                    .add(PetType::getId).identifier(IdentifierPolicy.AFTER_INSERT)
                    .add(PetType::getName), new Table("types")).cascading(RelationMode.ALL)
                , new Table("pets")).cascading(RelationMode.ALL)
            .mappedBy(Pet::getOwner)
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
    
    @Override
    @Transactional
    public void save(RawOwner owner) {
        // all of this could have been done with a Persister, this is done as such only for demo purpose : we use PersistenceContext "row access"
        Table owners = new Table("owners");
        Column<Table, Integer> id = owners.addColumn("id", Integer.class);
        Column<Table, String> first_name = owners.addColumn("first_name", String.class);
        Column<Table, String> last_name = owners.addColumn("last_name", String.class);
        Column<Table, String> address = owners.addColumn("address", String.class);
        Column<Table, String> city = owners.addColumn("city", String.class);
        Column<Table, String> telephone = owners.addColumn("telephone", String.class);
        
        if (owner.isNew()) {
            persistenceContext.insert(owners)
                .set(first_name, owner.getFirstName())
                .set(last_name, owner.getLastName())
                .set(address, owner.getAddress())
                .set(city, owner.getCity())
                .set(telephone, owner.getTelephone())
                .execute();
        } else {
            ExecutableUpdate<Table> executableUpdate = persistenceContext.update(owners)
                .set(first_name, owner.getFirstName())
                .set(last_name, owner.getLastName())
                .set(address, owner.getAddress())
                .set(city, owner.getCity())
                .set(telephone, owner.getTelephone());
            executableUpdate
                .where(id, eq(owner.getId()));
            executableUpdate
                .execute();
        }
    }
    
}
