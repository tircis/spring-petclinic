/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import java.util.List;

import org.gama.stalactite.persistence.engine.ColumnOptions.IdentifierPolicy;
import org.gama.stalactite.persistence.engine.PersistenceContext;
import org.gama.stalactite.persistence.engine.cascade.JoinedTablesPersister;
import org.gama.stalactite.persistence.structure.Column;
import org.gama.stalactite.persistence.structure.Table;
import org.springframework.samples.petclinic.model.Person;
import org.springframework.samples.petclinic.visit.Visit;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static org.gama.stalactite.persistence.engine.MappingEase.entityBuilder;

/**
 * Repository class for <code>Pet</code> domain objects All method names are compliant with Spring Data naming
 * conventions so this interface can easily be extended for Spring Data.
 * See: https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 */
@Repository
public class PetRepositoryStit implements PetRepository {
    
    private final JoinedTablesPersister<Pet, Integer, Table> persister;
    private final JoinedTablesPersister<PetType, Object, Table> petTypePersister;
    
    public PetRepositoryStit(PersistenceContext persistenceContext) {
        Table visitsTable = new Table("visits");
        Column<Table, Integer> petid = visitsTable.addColumn("pet_id", Integer.class);
        persister = entityBuilder(Pet.class, Integer.class)
            .joinColumnNamingStrategy(memberDefinition -> memberDefinition.getName() + "_id")
            .add(Pet::getId).identifier(IdentifierPolicy.AFTER_INSERT)
            .add(Pet::getName)
            .add(Pet::getBirthDate, "birth_date")
            .addOneToOne(Pet::getType,
                entityBuilder(PetType.class, Integer.class)
                .add(PetType::getId).identifier(IdentifierPolicy.AFTER_INSERT)
                .add(PetType::getName)
                , new Table("types"))
            .addOneToOne(Pet::getOwner,
                entityBuilder(Owner.class, Integer.class)
                .mapInheritance(
                    entityBuilder(Person.class, Integer.class)
                    .add(Person::getId).identifier(IdentifierPolicy.AFTER_INSERT)
                    .getConfiguration())
                .add(Owner::getFirstName, "first_name")
                .add(Owner::getLastName, "last_name"), new Table("owners"))
            .addOneToManySet(Pet::getVisitsInternal, entityBuilder(Visit.class, Integer.class)
                    .add(Visit::getId).identifier(IdentifierPolicy.AFTER_INSERT)
                    .add(Visit::getDescription)
                , visitsTable).mappedBy(petid)
            .build(persistenceContext, new Table("pets"));
        // PetType is available in the PersistenceContext because it was added by Pet mapping
        petTypePersister = (JoinedTablesPersister) persistenceContext.getPersister(PetType.class);
    }
    
    /**
     * Retrieve all {@link PetType}s from the data store.
     * @return a Collection of {@link PetType}s.
     */
    @Transactional(readOnly = true)
    public List<PetType> findPetTypes() {
        return petTypePersister.selectAll();
    }

    /**
     * Retrieve a {@link Pet} from the data store by id.
     * @param id the id to search for
     * @return the {@link Pet} if found
     */
    @Transactional(readOnly = true)
    public Pet findById(Integer id) {
        return persister.select(id);
    }

    /**
     * Save a {@link Pet} to the data store, either inserting or updating it.
     * @param pet the {@link Pet} to save
     */
    public void save(Pet pet) {
        persister.persist(pet);
    }

}

