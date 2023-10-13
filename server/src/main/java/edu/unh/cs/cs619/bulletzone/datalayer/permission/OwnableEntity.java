package edu.unh.cs.cs619.bulletzone.datalayer.permission;

import edu.unh.cs.cs619.bulletzone.datalayer.core.Entity;
import edu.unh.cs.cs619.bulletzone.datalayer.core.EntityRecord;
import edu.unh.cs.cs619.bulletzone.datalayer.user.GameUser;

public class OwnableEntity extends Entity {
    protected GameUser owner;

    public OwnableEntity(EntityRecord rec) {
        super(rec);
    }

    public void setOwner(GameUser user)  { owner = user; }
    public GameUser getOwner() { return owner; }
}
