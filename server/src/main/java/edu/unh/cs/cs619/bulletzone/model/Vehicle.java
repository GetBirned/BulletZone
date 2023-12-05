package edu.unh.cs.cs619.bulletzone.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Vehicle {

    public void takeDamage(int othersArmor);
    public void takeDamagefromSoldier(int othersArmor);

    public long getLastMoveTime();

    public void setLastMoveTime(long lastMoveTime);

    public long getAllowedMoveInterval();

    public void setAllowedMoveInterval(int allowedMoveInterval);

    public long getLastFireTime();

    public void setLastFireTime(long lastFireTime);

    public long getAllowedFireInterval();

    public void setAllowedFireInterval(int allowedFireInterval);

    public int getNumberOfBullets();

    public void setNumberOfBullets(int numberOfBullets);

    public int getAllowedNumberOfBullets();

    public void setAllowedNumberOfBullets(int allowedNumberOfBullets);

    public Direction getDirection();

    public void setDirection(Direction direction);
    

    public int getLife();

    public void setLife(int life);

    public abstract String getIp();

    public int getIsActive();

    public void setIsActive(int isActive);

    FieldHolder getParent();

    public int getPowerUpType();

    void setParent(FieldHolder nextField);

    long getId();

    boolean reenterTank(Tank tank);
}
