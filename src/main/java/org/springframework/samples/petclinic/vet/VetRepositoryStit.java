package org.springframework.samples.petclinic.vet;

import javax.annotation.Nonnull;
import java.util.Collection;

import org.gama.reflection.MemberDefinition;
import org.gama.stalactite.persistence.engine.AssociationTableNamingStrategy;
import org.gama.stalactite.persistence.engine.CascadeOptions.RelationMode;
import org.gama.stalactite.persistence.engine.ColumnOptions.IdentifierPolicy;
import org.gama.stalactite.persistence.engine.PersistenceContext;
import org.gama.stalactite.persistence.engine.cascade.JoinedTablesPersister;
import org.gama.stalactite.persistence.structure.Column;
import org.gama.stalactite.persistence.structure.Table;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import static org.gama.stalactite.persistence.engine.MappingEase.entityBuilder;

/**
 * @author Guillaume Mary
 */
@Repository
public class VetRepositoryStit implements VetRepository {
    
    private final JoinedTablesPersister<Vet, Integer, Table> persister;
    
    public VetRepositoryStit(PersistenceContext persistenceContext) {
        Table specialtiesTable = new Table("specialties");
        persister = entityBuilder(Vet.class, Integer.class)
            .add(Vet::getId).identifier(IdentifierPolicy.AFTER_INSERT)
            .add(Vet::getLastName, "last_name")
            .add(Vet::getFirstName, "first_name")
            .addOneToManySet(Vet::setSpecialtiesInternal,
                entityBuilder(Specialty.class, Integer.class)
                .add(Specialty::getId).identifier(IdentifierPolicy.AFTER_INSERT)
                .add(Specialty::getName), specialtiesTable)
            .cascading(RelationMode.ASSOCIATION_ONLY)
            .associationTableNamingStrategy(new AssociationTableNamingStrategy() {
                @Override
                public String giveName(@Nonnull MemberDefinition memberDefinition, @Nonnull Column source, @Nonnull Column target) {
                    if (Vet.class.isAssignableFrom(memberDefinition.getDeclaringClass())) {
                        return "vet_specialties";
                    }
                    if (Specialty.class.isAssignableFrom(memberDefinition.getDeclaringClass())) {
                        return "specialty";
                    }
                    return null;
                }
    
                @Override
                public String giveOneSideColumnName(@Nonnull Column source) {
                    return "vet_id";
                }
    
                @Override
                public String giveManySideColumnName(@Nonnull Column source) {
                    return "specialty_id";
                }
            })
            .build(persistenceContext, new Table("vets"));
    }
    
    @Override
    public Collection<Vet> findAll() throws DataAccessException {
        return persister.selectAll();
    }
}
