package edu.unh.cs.cs619.bulletzone.datalayer.permission;

import java.util.AbstractMap;

import edu.unh.cs.cs619.bulletzone.datalayer.core.EntityRepository;
import edu.unh.cs.cs619.bulletzone.datalayer.core.EntityType;

public interface OwnableEntityRepository extends EntityRepository {
    public OwnableEntity getTarget(int id);
    public EntityType getTargetType();

    public AbstractMap<Integer, ? extends OwnableEntity> getEntities();
}
