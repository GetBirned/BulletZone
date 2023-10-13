package edu.unh.cs.cs619.bulletzone.datalayer.core;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;

public class AggregateRepository implements EntityRepository {
    HashMap<Integer, EntityRepository> entityMap = new HashMap<>();
    HashMap<EntityType, EntityRepository> repoMap = new HashMap<>();

    public AggregateRepository(List<? extends EntityRepository> repos) {
        for (EntityRepository repo : repos) {
            repoMap.put(repo.getTargetType(), repo);
        }
    }

    @Override
    public Entity getTarget(int id) {
        if (entityMap.containsKey(id))
            return entityMap.get(id).getTarget(id);
        //wasn't there, so need to walk through our repos.
        for (EntityRepository repo : repoMap.values()) {
            Entity target = repo.getTarget(id);
            if (target != null) {
                entityMap.put(id, repo); //cache the repo
                return target;
            }
        }
        return null;
    }

    @Override
    public AbstractMap<Integer, ? extends Entity> getEntities() {
        HashMap<Integer, Entity> snapshot = new HashMap<>();
        for (EntityRepository repo : repoMap.values()) {
            snapshot.putAll(repo.getEntities());
        }
        return snapshot;
    }

    @Override
    public EntityType getTargetType() {
        return EntityType.Invalid;
    }
}
