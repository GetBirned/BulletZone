package edu.unh.cs.cs619.bulletzone.model;

import java.util.Timer;
import java.util.TimerTask;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import edu.unh.cs.cs619.bulletzone.util.LongWrapper;

public final class Game {
    /**
     * Field dimensions
     */
    private static final int FIELD_DIM = 16;
    private final long id;
    private long lastEjectionTime;
    private final ConcurrentMap<Long, Soldier> soldiers = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Tank> tanks = new ConcurrentHashMap<>();

    private final ConcurrentMap<Long, Builder> builders = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> playersIP = new ConcurrentHashMap<>();
    private final Object monitor = new Object();
    private GameBoardBuilder gbb = null;
    private GameBoard gb = null;

    public Game() {
        this.id = 0;
        this.initiialize();
    }

    public void initiialize() {
        if (gbb != null) {
            return;
        }
        gbb = new GameBoardBuilder(gb);
    }

    @JsonIgnore
    public long getId() {
        return id;
    }

    @JsonIgnore
    public ArrayList<FieldHolder> getHolderGrid() {
        return gbb.getBoard().getHolderGrid();
    }

    public void addTank(String ip, Tank tank) {
        synchronized (tanks) {
            tanks.put(tank.getId(), tank);
            playersIP.put(ip, tank.getId());
        }
    }

    public Tank getTank(long tankId) {
        return tanks.get(tankId);
    }

    public ConcurrentMap<Long, Tank> getTanks() {
        return tanks;
    }



    public Tank getTank(String ip){
        if (playersIP.containsKey(ip)){
            return tanks.get(playersIP.get(ip));
        }
        return null;
    }

    public void removeTank(long tankId){
        synchronized (tanks) {
            Tank t = tanks.remove(tankId);
            if (t != null) {
                playersIP.remove(t.getIp());
            }
        }
    }
    public List<Optional<FieldEntity>> getGrid() {
        synchronized (gbb.getBoard().getHolderGrid()) {
            List<Optional<FieldEntity>> entities = new ArrayList<Optional<FieldEntity>>();

            FieldEntity entity;
            for (FieldHolder holder : gbb.getBoard().getHolderGrid()) {
                if (holder.isPresent()) {
                    entity = holder.getEntity();
                    entity = entity.copy();

                    entities.add(Optional.<FieldEntity>of(entity));
                } else {
                    entities.add(Optional.<FieldEntity>empty());
                }
            }
            return entities;
        }
    }
    public int[][] getGrid2D() {
        int[][] grid = new int[FIELD_DIM][FIELD_DIM];

        synchronized (gbb.getBoard().getHolderGrid()) {
            FieldHolder holder;
            for (int i = 0; i < FIELD_DIM; i++) {
                for (int j = 0; j < FIELD_DIM; j++) {
                    holder = gbb.getBoard().getHolderGrid().get(i * FIELD_DIM + j);
                    if (holder.isPresent()) {
                        grid[i][j] = holder.getEntity().getIntValue();
                    } else {
                        grid[i][j] = 0;
                    }
                }
            }
        }

        return grid;
    }
    public TankLocation findTank(Tank tank, long tankID) {
        synchronized (gbb.getBoard().getHolderGrid()) {
            FieldHolder holder;
            for (int i = 0; i < FIELD_DIM; i++) {
                for (int j = 0; j < FIELD_DIM; j++) {
                    holder = gbb.getBoard().getHolderGrid().get(i * FIELD_DIM + j);
                    if (holder.isPresent() && holder.getEntity() instanceof Tank) {
                        Tank currentTank = (Tank) holder.getEntity();
                        if (currentTank.getId() == tankID) {
                            return new TankLocation(i, j);
                        }
                    }
                }
            }
        }
        return null;
    }
    public TankLocation findSoldier(Soldier soldier, long SoldierID) {
        synchronized (gbb.getBoard().getHolderGrid()) {
            FieldHolder holder;
            for (int i = 0; i < FIELD_DIM; i++) {
                for (int j = 0; j < FIELD_DIM; j++) {
                    holder = gbb.getBoard().getHolderGrid().get(i * FIELD_DIM + j);
                    if (holder.isPresent() && holder.getEntity() instanceof Soldier) {
                        Soldier currentSoldier = (Soldier) holder.getEntity();
                        if (currentSoldier.getId() == SoldierID) {
                            return new TankLocation(i, j);
                        }
                    }
                }
            }
        }
        return null;
    }

    public TankLocation findBuilder(Builder builder, long builderId) {
        synchronized (gbb.getBoard().getHolderGrid()) {
            FieldHolder holder;
            for (int i = 0; i < FIELD_DIM; i++) {
                for (int j = 0; j < FIELD_DIM; j++) {
                    holder = gbb.getBoard().getHolderGrid().get(i * FIELD_DIM + j);
                    if (holder.isPresent() && holder.getEntity() instanceof Builder) {
                        Builder currentSoldier = (Builder) holder.getEntity();
                        if (currentSoldier.getId() == builderId) {
                            return new TankLocation(i, j);
                        }
                    }
                }
            }
        }
        return null;
    }
    public GameBoard getGameBoard() {
        return this.gbb.getBoard();
    }

    public void startEjectionCooldown() {
        lastEjectionTime = System.currentTimeMillis();
    }

    public boolean canEject() {
        // Check if the ejection cooldown period has elapsed
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastEjectionTime) >= 3000;
    }

    private int[] getOffsetForDirection(int direction) {
        switch (direction) {
            case 0:
                return new int[]{1, 0}; // UP
            case 4:
                return new int[]{-1, 0}; // DOWN
            case 6:
                return new int[]{0, 1}; // LEFT
            case 2:
                return new int[]{0, -1}; // RIGHT
            default:
                return new int[]{0, 0}; // No movement for other directions
        }
    }

    public LongWrapper dismantleImprovement(long builderId) {
        Builder builder = getBuilders().get(builderId);
        if (builder != null) {
            TankLocation builderLocation = findBuilder(builder, builderId);
            int x = builderLocation.getRow();
            int y = builderLocation.getColumn();
            int direction = (builder.getIntValue() % 10);
            int[] offset = getOffsetForDirection(direction);

            int newX = x + offset[0];
            int newY = y + offset[1];
            FieldHolder fieldElement = getHolderGrid().get(newX * FIELD_DIM + newY); // find the FieldHolder of element behind builder
            if (fieldElement.getEntity() instanceof Wall) { // WALL - RETURN 100 CREDITS
                Wall wall = (Wall) fieldElement.getEntity();
                wall.getParent().clearField();
                wall.setParent(null);
                fieldElement.setFieldEntity(new Grass());
                return new LongWrapper(1);
            } else if (fieldElement.getEntity().getIntValue() == 70) { // ROAD - RETURN 40 CREDITS
                Road road = (Road) fieldElement.getEntity();
                road.getParent().clearField();
                road.setParent(null);
                fieldElement.setFieldEntity(new Grass());
                return new LongWrapper(2);
            } else if (fieldElement.getEntity().getIntValue() == 60) { // BRIDGE - RETURN 80 CREDITS
                Bridge bridge = (Bridge) fieldElement.getEntity();
                bridge.getParent().clearField();
                bridge.setParent(null);
                fieldElement.setFieldEntity(new Water());
                return new LongWrapper(3);
            } else {
                throw new IllegalArgumentException("Improper remove request. Spot behind builder is not an improvement.");
            }
        } else {
            throw new IllegalArgumentException("Builder associated with Id: " + builderId + " not found.");
        }
    }
    public LongWrapper buildImprovement(int choice, long builderId) {
        Builder builder = getBuilder(builderId);
        if (builder != null) {
            TankLocation builderLocation = findBuilder(builder, builderId);
            int x = builderLocation.getRow();
            int y = builderLocation.getColumn();
            int direction = (builder.getIntValue() % 10);
            int[] offset = getOffsetForDirection(direction);

            int newX = x + offset[0];
            int newY = y + offset[1];
            FieldHolder fieldElement = getHolderGrid().get(newX * FIELD_DIM + newY);

            /**
            long buildTime = System.currentTimeMillis(); // DEPLOYMENT TIMES

            long buildDuration = (fieldElement.getEntity() instanceof Hill || fieldElement.getEntity() instanceof Rocky
                    || fieldElement.getEntity() instanceof Forest) ? 2000 : 1000;

            while (System.currentTimeMillis() - buildTime <= buildDuration) {
                if
            }
             */

            if (choice == 1) { // WALL - COSTS 100 CREDITS
                Wall wall = new Wall();
                if (!fieldElement.isPresent()) {
                    fieldElement.setFieldEntity(wall);
                    wall.setParent(fieldElement);
                    return new LongWrapper(1);
                }
            } else if (choice == 2) { // ROAD - COSTS 40 CREDITS
                Road road = new Road();
                if (!fieldElement.isPresent()) {
                    fieldElement.setFieldEntity(road);
                    road.setParent(fieldElement);
                    return new LongWrapper(2);
                }
            } else if (choice == 3) { // BRIDGE - COSTS 80 CREDITS
                if (fieldElement.getEntity() instanceof Water) {
                    Bridge bridge = new Bridge();
                    fieldElement.setFieldEntity(bridge);
                    bridge.setParent(fieldElement);
                    return new LongWrapper(3);
                }
            } else { // improper input -- this should never happen with how I have buildChoice added.
                throw new IllegalArgumentException("Improper Build Request: " + choice);
            }
        } else {
            throw new IllegalArgumentException("Builder associated with Id: " + builderId + " not found.");
        }
        return new LongWrapper(4);
    }

    public LongWrapper deploySoldier(long tankId) {
        synchronized (tanks) {
            Tank tank = tanks.get(tankId);
            if (tank != null) {
                if (soldiers.get(tankId) == null) {
                    tank.setIsActive(0);
                    TankLocation currentTank = findTank(tank, tankId);
                    long soldierId = tankId;

                    // Create a new soldier

                    Soldier soldier = new Soldier(soldierId, tank.getDirection(), tank.getIp());
                    if (canEject()) {

                        int x = currentTank.getRow();
                        int y = currentTank.getColumn();

                        int maxIterations = FIELD_DIM * FIELD_DIM; // Set a maximum number of iterations
                        for (int i = 0; i < maxIterations; i++) {
                            // Attempt placing below, then above, then to the right, then to the left
                            int[][] offsets = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

                            for (int[] offset : offsets) {
                                int newX = x + offset[0];
                                int newY = y + offset[1];

                                if (isValidPosition(newX, newY) && !getHolderGrid().get(newX * FIELD_DIM + newY).isPresent()) {
                                    x = newX;
                                    y = newY;
                                    break;
                                }
                            }

                            FieldHolder fieldElement = getHolderGrid().get(x * FIELD_DIM + y);
                            if (!fieldElement.isPresent()) {
                                fieldElement.setFieldEntity(soldier);
                                soldier.setParent(fieldElement);
                                break;
                            }
                        }

                        if (soldier.getParent() == null) {
                            throw new IllegalStateException("No free space found for soldier.");
                        }

                        // Add the soldier to the game
                        addSoldier(soldier.getIp(), soldier);
                        soldier.setIsInTank(false);
                        startEjectionCooldown();
                        soldier.setLife(25);

                        return new LongWrapper(soldierId);
                    } else {
                        throw new IllegalArgumentException("Soldier cannot be deployed.");
                    }
                } else {
                    throw new IllegalArgumentException("A soldier already exists for tankId = " + tankId);
                }
            } else {
                // Handle the case where the tank is not found
                throw new IllegalArgumentException("Tank not found with ID: " + tankId);
            }
        }
    }
    private boolean isValidPosition(int x, int y) {
        return x >= 0 && x < FIELD_DIM && y >= 0 && y < FIELD_DIM;
    }

    public LongWrapper controlBuilder(long tankId) {
        Builder b = getBuilders().get(tankId);
        Tank t = getTanks().get(tankId);
        b.setIsActive(1); // Set builder as active
        t.setIsActive(0); // Set tank as NOT active
        return new LongWrapper(t.getId()); // returns the placeholder LongWrapper
    }

    public LongWrapper controlTank(long tankId) {
        Builder b = getBuilders().get(tankId);
        Tank t = getTanks().get(tankId);
        b.setIsActive(0); // Set builder as NOT active
        t.setIsActive(1); // Set tank as active
        if (soldiers.get(tankId) != null) { // Soldier is currently out, make sure controlling soldier instead of tank on return
            t.setIsActive(0);
        }
        return new LongWrapper(t.getId()); // returns the placeholder LongWrapper
    }

    public void addSoldier(String ip, Soldier soldier) {
        synchronized (soldiers) {
            soldiers.put(soldier.getId(), soldier);
            playersIP.put(ip, soldier.getId());
        }
    }

    public void addBuilder(String ip, Builder builder) {
        synchronized (builders) {
            builders.put(builder.getId(), builder);
            playersIP.put(ip, builder.getId());
        }
    }

    public Soldier getSoldiers(String ip){
        if (playersIP.containsKey(ip)){
            return soldiers.get(playersIP.get(ip));
        }
        return null;
    }

    public Soldier getSoldier(long soldierID) {
        return soldiers.get((long) soldierID);
    }

    public ConcurrentMap<Long, Soldier> getSoldiers() {
        return soldiers;
    }

    public Builder getBuilder(long builderID) {
        return builders.get((long) builderID);
    }

    public ConcurrentMap<Long, Builder> getBuilders() {
        return builders;
    }

    public void removeSoldier(long soldierId){
        synchronized (tanks) {
            Tank tank = tanks.get(soldierId);
            tank.setIsActive(1);
            Soldier s = soldiers.remove(soldierId);
            if (s != null) {
                playersIP.remove(s.getIp());
            }
        }
    }

    public void removeBuilder(long builderId){
        synchronized (tanks) {
            Tank tank = tanks.get(builderId);
            tank.setIsActive(1);
            Builder b = builders.remove(builderId);
            if (b != null) {
                playersIP.remove(b.getIp());
            }
        }
    }
    public ArrayList<Integer> getTankPowerups(long tankId) {
        return getTank(tankId).powerupList;
    }

    public ArrayList<Integer> getSoldierPowerups(long soldierId) {

        return getSoldier(soldierId).powerupList;
    }

    public int getTankPowerup(long tankId) {
        Tank curr = tanks.get(tankId);
        if (curr == null || curr.pQ.peek() == null) {
            return -1;
        }
        curr.revertBuffs(curr.pQ.peek());
        return curr.pQ.poll();
    }


    public int getSoldierPowerup(long tankId) {
        Soldier curr = soldiers.get(tankId);
        if (curr == null || curr.pQ.peek() == null) {
            return -1;
        }
        curr.revertBuffs(curr.pQ.peek());
        return curr.pQ.poll();
    }
    public void setSoldierPowerup(long tankId, int powerupValue){
        getSoldier((int) tankId).setPowerUpType(powerupValue);
        Soldier curr = getSoldier((int) tankId);
        curr.pQ.add(powerupValue);
        if (powerupValue == 2) {
            curr.setAllowedMoveInterval((int) (curr.getAllowedMoveInterval() * 1.25));
            curr.setAllowedNumberOfBullets(curr.getAllowedNumberOfBullets() * 2);
        }
        //ANTIGRAV
        if (powerupValue == 3) {
            curr.setAllowedMoveInterval((int) curr.getAllowedMoveInterval() / 2);
            curr.setAllowedFireInterval((int) curr.getAllowedFireInterval() + 100);
        }


    }
    public void setTankPowerup(long tankId, int powerupValue) {
        getTank(tankId).setPowerUpType(powerupValue);
        Tank curr = getTank(tankId);
        curr.pQ.add(powerupValue);
        //FUSION
        if (powerupValue == 2) {
            curr.setAllowedMoveInterval((int) (curr.getAllowedMoveInterval() * 1.25));
            curr.setAllowedNumberOfBullets(curr.getAllowedNumberOfBullets() * 2);
            curr.setAllowedFireInterval((int)curr.getAllowedFireInterval() / 2);
        }
        //ANTIGRAV
        if (powerupValue == 3) {
            curr.setAllowedMoveInterval((int) curr.getAllowedMoveInterval() / 2);
            curr.setAllowedFireInterval((int) curr.getAllowedFireInterval() + 100);
        }


    }


}