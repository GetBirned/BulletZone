package edu.unh.cs.cs619.bulletzone.model;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class Soldier extends FieldEntity implements Vehicle{

    private static final String TAG = "Soldier";
    public Queue<Integer> pQ = new LinkedList<>();
    private static final int REPAIR_KIT_EFFECT_DURATION = 120;
    private static final int DEFLECTOR_SHIELD_DAMAGE_REDUCTION = 1;

    private final long id;
    Timer sTimer2 = new Timer();
    Timer sTimer = new Timer();
    int mockTimer;

    private String ip;
    private long lastMoveTime;
    private int allowedMoveInterval;

    private boolean isInTank;
    private long lastEjectionTime;

    private long lastFireTime;
    private int allowedFireInterval;

    private int numberOfBullets;
    private int allowedNumberOfBullets;


    private int life;

    private Direction direction;
    private int powerUpType;
    private TankLocation tankLocation;
    public int ind;
    public int numShield;
    public boolean hasShield;

    public Soldier(long id, Direction direction, String ip) {
        this.id = id;
        this.direction = direction;
        this.ip = ip;
        numberOfBullets = 0;
        allowedNumberOfBullets = 6;
        lastFireTime = 0;
        allowedFireInterval = 250; // Shoot 250ms
        lastMoveTime = 0;
        allowedMoveInterval = 1000; // 1 second between move
        hasShield = false;
        mockTimer = 0;
    }


    public void setPowerUpType(int powerupValue) {
        this.powerUpType = powerupValue;
    }

    public int getPowerUpType(){
        return this.powerUpType;
    }

    public long getId() {
        return id;
    }

    @Override
    public int getIntValue() {
        return (int) (40000000 + 10000 * id + 10 * life + Direction
                .toByte(direction));
    }

    public boolean reenterTank(Tank tank) {
        if (!isInTank) {
            setIsInTank(true);
            // Soldier re-enters the tank
            tank.setIsActive(1);
            isInTank = true;
            // Reset soldier's health to full
            setLife(25);
            // Start or reset the ejection cooldown timer
            startEjectionCooldown();
            return true;
        }
        return false; // Soldier is already in a tank
    }

    public void setIsInTank(boolean isInTank) {
        this.isInTank = isInTank;
    }

    public boolean getIsInTank() {
        return this.isInTank;
    }

    public void startEjectionCooldown() {
        lastEjectionTime = System.currentTimeMillis();
    }

    public boolean canEject() {
        // Check if the ejection cooldown period has elapsed
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastEjectionTime) >= 3000;
    }

    public void resetSoldierLife() {
        life = 25;
    }

    @Override
    public String toString() {
        return "S";
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

    @Override
    public int getIsActive() {
        return 0;
    }

    @Override
    public void setIsActive(int isActive) {

    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public void takeDamage(int othersArmor) {

    }

    @Override
    public void takeDamagefromSoldier(int othersArmor) {

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

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void hit(int damage) {
        life = life - damage;
        System.out.println("Soldier life: " + id + " : " + life);
//		Log.d(TAG, "TankId: " + id + " hit -> life: " + life);

        if (life <= 0) {
//			Log.d(TAG, "Tank event");
            //eventBus.post(Tank.this);
            //eventBus.post(new Object());
        }
    }

    @Override
    public FieldEntity copy() {
        return new Soldier(id, direction, ip);
    }

    public void setAllowedNumberOfBullets(int allowedNumberOfBullets) {
        this.allowedNumberOfBullets = allowedNumberOfBullets;
    }
    public void setTankLocation(TankLocation tl) {this.tankLocation = tl;}
    public TankLocation getTankLocation() {return this.tankLocation;}

    public void revertBuffs(int type){
        if (type == 2) {
            this.setAllowedFireInterval((int) (this.getAllowedFireInterval() * 2));
            this.setAllowedNumberOfBullets(this.getAllowedNumberOfBullets() / 2);
            this.setAllowedMoveInterval((int) (this.getAllowedMoveInterval() / 1.25));
        } else if (type == 3){
            this.setAllowedMoveInterval((int) this.getAllowedMoveInterval() * 2);
            this.setAllowedFireInterval((int) this.getAllowedFireInterval() - 100);
        }


        //TODO: revert buffs for the new powerups
    }
    public void applyRepairKitEffect(long tankId) {
        final int[] elapsedTime = {0};
        Soldier curr = this;
        sTimer2.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (elapsedTime[0] < REPAIR_KIT_EFFECT_DURATION && curr.getLife() < 25) {
                    curr.setLife(curr.getLife() + 1); // Heal by 1 point
                    elapsedTime[0]++;
                    mockTimer++;
                }  if(mockTimer == REPAIR_KIT_EFFECT_DURATION) {
                    sTimer2.cancel();
                    sTimer2.purge();
                }
            }
        }, 0, 1000);
        mockTimer = 0;
    }
    public void deflectorShield(long tankId) {
        final int[] remainingAbsorption = {50};
        Soldier curr = this;
        curr.setAllowedFireInterval((int) (curr.getAllowedFireInterval() * 1.5));
        int origLife = curr.getLife();
        curr.setLife(curr.getLife() + 50);
        sTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (remainingAbsorption[0] > 0 && curr.getLife() > origLife) {
                    curr.setLife(curr.getLife() - DEFLECTOR_SHIELD_DAMAGE_REDUCTION);
                    remainingAbsorption[0]--;
                } else {
                    sTimer.cancel();
                    sTimer.purge();
                }
            }
        }, 1000, 1000);
    }

}
