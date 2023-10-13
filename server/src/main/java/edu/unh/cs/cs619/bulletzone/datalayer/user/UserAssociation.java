package edu.unh.cs.cs619.bulletzone.datalayer.user;

import edu.unh.cs.cs619.bulletzone.datalayer.core.Entity;
import edu.unh.cs.cs619.bulletzone.datalayer.core.EntityRepository;

public class UserAssociation {
    public final GameUser user;
    public final Entity entity;
    public final double value;
    public final String info;

    public UserAssociation(GameUserRepository userRepo, EntityRepository entityRepo,
                           UserAssociationRecord rec) {
        user = userRepo.getUser(rec.user_entityID);
        entity = entityRepo.getTarget(rec.entityID);
        value = rec.value;
        info = rec.info;
    }
}
