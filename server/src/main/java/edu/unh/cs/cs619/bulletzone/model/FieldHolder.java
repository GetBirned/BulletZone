package edu.unh.cs.cs619.bulletzone.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FieldHolder {

    private final Map<Direction, FieldHolder> neighbors = new HashMap<Direction, FieldHolder>();
    private Optional<FieldEntity> entityHolder = Optional.empty();

    private FieldEntity entity;
    private FieldEntity originalEntity;

    public void addNeighbor(Direction direction, FieldHolder fieldHolder) {
        neighbors.put(checkNotNull(direction), checkNotNull(fieldHolder));
    }

    public FieldHolder getNeighbor(Direction direction) {
        return neighbors.get(checkNotNull(direction,
                "Direction cannot be null."));
    }

    public boolean isPresent() {
        return entityHolder.isPresent();
    }

    public FieldEntity getEntity() {
        return entityHolder.get();
    }

    public void setFieldEntity(FieldEntity entity) {

        this.entity = entity;
        if (originalEntity == null) {
            this.originalEntity = entity;  // Set the original entity only if it's not set already
        }
        entityHolder = Optional.of(checkNotNull(entity,
                "FieldEntity cannot be null."));
    }


    public FieldEntity getOriginalEntity() {
        return originalEntity;
    }

    public void clearField() {
        if (entityHolder.isPresent()) {
            entityHolder = Optional.empty();
        }
    }

}
