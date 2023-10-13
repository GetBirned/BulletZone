package edu.unh.cs.cs619.bulletzone.datalayer.permission;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;

import edu.unh.cs.cs619.bulletzone.datalayer.core.EntityType;

public class AggregateOwnableRepository implements OwnableEntityRepository {
    HashMap<Integer, OwnableEntityRepository> entityMap = new HashMap<>();
    HashMap<EntityType, OwnableEntityRepository> repoMap = new HashMap<>();

    public AggregateOwnableRepository(List<? extends OwnableEntityRepository> repos) {
        for (OwnableEntityRepository repo : repos) {
            repoMap.put(repo.getTargetType(), repo);
        }
    }

    @Override
    public OwnableEntity getTarget(int id) {
        if (entityMap.containsKey(id))
            return entityMap.get(id).getTarget(id);
        //wasn't there, so need to walk through our repos.
        for (OwnableEntityRepository repo : repoMap.values()) {
            OwnableEntity target = repo.getTarget(id);
            if (target != null) {
                entityMap.put(id, repo); //cache the repo
                return target;
            }
        }
        return null;
    }

    @Override
    public AbstractMap<Integer, ? extends OwnableEntity> getEntities() {
        HashMap<Integer, OwnableEntity> snapshot = new HashMap<>();
        for (OwnableEntityRepository repo : repoMap.values()) {
            snapshot.putAll(repo.getEntities());
        }
        return snapshot;
    }

    @Override
    public EntityType getTargetType() {
        return EntityType.Invalid;
    }

}
