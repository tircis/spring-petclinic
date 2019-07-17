package org.springframework.samples.petclinic.visit;

import java.util.List;

import org.gama.stalactite.persistence.engine.ColumnOptions.IdentifierPolicy;
import org.gama.stalactite.persistence.engine.PersistenceContext;
import org.gama.stalactite.persistence.engine.cascade.JoinedTablesPersister;
import org.gama.stalactite.persistence.structure.Table;
import org.gama.stalactite.query.model.Operators;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import static org.gama.stalactite.persistence.engine.MappingEase.entityBuilder;

/**
 * @author Guillaume Mary
 */
@Repository
public class VisitRepositoryStit implements VisitRepository {
    
    private final JoinedTablesPersister<Visit, Integer, Table> persister;
    
    public VisitRepositoryStit(PersistenceContext persistenceContext) {
        persister = entityBuilder(Visit.class, Integer.class)
            .add(Visit::getId).identifier(IdentifierPolicy.AFTER_INSERT)
            .add(Visit::getPetId, "pet_id")
            .add(Visit::getDescription)
            .add(Visit::getDate, "visit_date")
            .build(persistenceContext, new Table("visits"));
    }
    
    @Override
    public void save(Visit visit) throws DataAccessException {
        persister.persist(visit);
    }
    
    @Override
    public List<Visit> findByPetId(Integer petId) {
        return persister.selectWhere(Visit::getPetId, Operators.eq(petId)).execute();
    }
}
