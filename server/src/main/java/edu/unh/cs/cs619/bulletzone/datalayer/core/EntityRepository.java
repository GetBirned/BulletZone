package edu.unh.cs.cs619.bulletzone.datalayer.core;

import java.util.AbstractMap;

public interface EntityRepository {
    public Entity getTarget(int id);
    public EntityType getTargetType();

    public AbstractMap<Integer, ? extends Entity> getEntities();
}
