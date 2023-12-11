package edu.unh.cs.cs619.bulletzone.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;


public class Tank extends FieldEntity implements Vehicle {
    private static final int REPAIR_KIT_EFFECT_DURATION = 120;
    private static final int DEFLECTOR_SHIELD_DAMAGE_REDUCTION = 1; // Damage reduction per second


    private static final String TAG = "Tank";

    private final long id;
    private int powerUpType;
    Timer tTimer2 = new Timer();
    Timer tTimer = new Timer();

    private final String ip;
    public Queue<Integer> pQ = new LinkedList<>();
    private long lastMoveTime;
    public int allowedMoveInterval;

    private long lastFireTime;
    public int allowedFireInterval;

    public int numberOfBullets;
    public int allowedNumberOfBullets;

    private int life;
    public int numShield;

    private Direction direction;
    private TankLocation tankLocation;
    private int isActive;
    public int ind;
    public Tank(long id, Direction direction, String ip, int isActive) {
        this.id = id;
        this.direction = direction;
        this.ip = ip;
        this.isActive = isActive;
        numberOfBullets = 0;
        allowedNumberOfBullets = 2;
        lastFireTime = 0;
        allowedFireInterval = 1500;
        lastMoveTime = 0;
        allowedMoveInterval = 500;
        numShield = 0;
        ind = 0;
    }

    public void setPowerUpType(int powerupValue) {
        this.powerUpType = powerupValue;
    }
    public int getPowerUpType(){
        return this.powerUpType;
    }

    @Override
    public FieldEntity copy() {
        return new Tank(id, direction, ip, isActive);
    }


    @Override
    public void hit(int damage) {
        life = life - damage;
        System.out.println("Tank life: " + id + " : " + life);
//		Log.d(TAG, "TankId: " + id + " hit -> life: " + life);

        if (life <= 0) {
//			Log.d(TAG, "Tank event");
            //eventBus.post(Tank.this);
            //eventBus.post(new Object());
        }
    }


    public void takeDamage(int othersArmor) {
        int damageTaken = (int) Math.floor(othersArmor * .1);
        this.setLife(this.getLife() - damageTaken);
    }
    public void takeDamagefromSoldier(int othersArmor) {
        int damage = (int) Math.ceil(othersArmor * .4);
        this.life = this.life - damage;
    }

    public long getLastMoveTime() {
        return lastMoveTime;
    }

    public void setLastMoveTime(long lastMoveTime) {
        this.lastMoveTime = lastMoveTime;
    }

    public long getAllowedMoveInterval() {
        return allowedMoveInterval;
    }

    public void setAllowedMoveInterval(int allowedMoveInterval) {
        this.allowedMoveInterval = allowedMoveInterval;
    }

    public long getLastFireTime() {
        return lastFireTime;
    }

    public void setLastFireTime(long lastFireTime) {
        this.lastFireTime = lastFireTime;
    }

    public long getAllowedFireInterval() {
        return allowedFireInterval;
    }

    public void setAllowedFireInterval(int allowedFireInterval) {
        this.allowedFireInterval = allowedFireInterval;
    }

    public int getNumberOfBullets() {
        return numberOfBullets;
    }

    public void setNumberOfBullets(int numberOfBullets) {
        this.numberOfBullets = numberOfBullets;
    }

    public int getAllowedNumberOfBullets() {
        return allowedNumberOfBullets;
    }

    public void setAllowedNumberOfBullets(int allowedNumberOfBullets) {
        this.allowedNumberOfBullets = allowedNumberOfBullets;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    @JsonIgnore
    public long getId() {
        return id;
    }

    @Override
    public boolean reenterTank(Tank tank) {
        return false;
    }

    @Override
    public int getIntValue() {
        return (int) (10000000 + 10000 * id + 10 * life + Direction
                .toByte(direction));
    }

    @Override
    public String toString() {
        return "T";
    }

    public int getLife() {
        return life;
    }

    public void setLife(int life) {
        this.life = life;
    }

    public String getIp(){
        return ip;
    }

    public int getIsActive() {
        return isActive;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    public void revertBuffs(int type){
        if (type == 2) {
            this.setAllowedFireInterval((int) (this.getAllowedFireInterval() * 2));
            this.setAllowedNumberOfBullets(this.getAllowedNumberOfBullets() / 2);
            this.setAllowedMoveInterval((int) (this.getAllowedMoveInterval() / 1.25));
        } else if (type == 3){
            this.setAllowedMoveInterval((int) this.getAllowedMoveInterval() * 2);
            this.setAllowedFireInterval((int) this.getAllowedFireInterval() - 100);
        } else if(type == 9){
            tTimer.cancel();
            tTimer.purge();
        } else {
            tTimer2.cancel();
            tTimer2.purge();
        }


        //TODO: revert buffs for the new powerups
    }

    public void applyRepairKitEffect(long tankId) {
        final int[] elapsedTime = {0};
        Tank curr = this;
        tTimer2.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (elapsedTime[0] < REPAIR_KIT_EFFECT_DURATION && curr.getLife() < 100) {
                    curr.setLife(curr.getLife() + 1); // Heal by 1 point
                    elapsedTime[0]++;
                } else {
                    tTimer2.cancel();
                    tTimer2.purge();
                }
            }
        }, 0, 1000);
    }

    public void deflectorShield(long tankId) {
        final int[] remainingAbsorption = {50};
        Tank curr = this;
        curr.setAllowedFireInterval((int) (curr.getAllowedFireInterval() * 1.5));
        int origLife = curr.getLife();
        //curr.setLife(curr.getLife() + 50);
        tTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (remainingAbsorption[0] > 0 && curr.getLife() < origLife) {
                    curr.setLife(curr.getLife() + DEFLECTOR_SHIELD_DAMAGE_REDUCTION);
                    remainingAbsorption[0]--;
                } else {
                    tTimer.cancel();
                    tTimer.purge();
                }
            }
        }, 1000, 1000);
    }


    public void setTankLocation(TankLocation tl) {this.tankLocation = tl;}
    public TankLocation getTankLocation() {return tankLocation;}

}

