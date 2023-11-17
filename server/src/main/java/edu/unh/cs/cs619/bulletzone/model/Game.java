package edu.unh.cs.cs619.bulletzone.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;

import java.util.ArrayList;
import java.util.List;
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
    private final ConcurrentMap<String, Long> playersIP = new ConcurrentHashMap<>();
    private final Object monitor = new Object();
    private GameBoardBuilder gbb = null;

    public Game() {
        this.id = 0;
        this.initiialize();
    }

    public void initiialize() {
        if (gbb != null) {
            return;
        }
        gbb = new GameBoardBuilder();
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
        synchronized (gbb.gb.holderGrid) {
            FieldHolder holder;
            for (int i = 0; i < FIELD_DIM; i++) {
                for (int j = 0; j < FIELD_DIM; j++) {
                    holder = gbb.gb.holderGrid.get(i * FIELD_DIM + j);
                    if (holder.isPresent() && holder.getEntity() instanceof Tank) {
                        Tank currentTank = (Tank) holder.getEntity();
                        if (currentTank.getId() == tankID) {
                            return new TankLocation(i, j);
                        }
                    }
                }
            }
        }
        // When the first grid is created, have each fieldentity know which coordinate it has in the grid, and then the tank will know who its parent is, then can ask the field entity for its coordinate.

        // Tank not found, return null or handle accordingly
        return null;
    }


    public void startEjectionCooldown() {
        lastEjectionTime = System.currentTimeMillis();
    }

    public boolean canEject() {
        // Check if the ejection cooldown period has elapsed
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastEjectionTime) >= 3000;
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


    public void addSoldier(String ip, Soldier soldier) {
        synchronized (soldiers) {
            soldiers.put(soldier.getId(), soldier);
            playersIP.put(ip, soldier.getId());
        }
    }

    public Soldier getSoldiers(String ip){
        if (playersIP.containsKey(ip)){
            return soldiers.get(playersIP.get(ip));
        }
        return null;
    }

    public Soldier getSoldier(int soldierID) {
        return soldiers.get(soldierID);
    }

    public ConcurrentMap<Long, Soldier> getSoldiers() {
        return soldiers;
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
    public void setTankPowerup(long tankId, int powerupValue, boolean isTank) {
        if (isTank) {
            getTank(tankId).setPowerUpType(powerupValue);
            Tank curr = getTank(tankId);
            if (powerupValue == 2) {
                curr.setAllowedMoveInterval((int) (curr.getAllowedMoveInterval() * 1.25));
                curr.setAllowedNumberOfBullets(curr.getAllowedNumberOfBullets() * 2);
            }
            //ANTIGRAV
            if (powerupValue == 3) {
                curr.setAllowedMoveInterval((int) curr.getAllowedMoveInterval() / 2);
                curr.setAllowedFireInterval((int) curr.getAllowedFireInterval() + 100);
            }

        } else {
            getSoldier((int) tankId).setPowerUpType(powerupValue);
            Soldier curr = getSoldier((int) tankId);
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
    }

}

//
//package edu.unh.cs.cs619.bulletzone.model;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import java.util.Optional;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;
//
//public final class Game {
//    /**
//     * Field dimensions
//     */
//    private static final int FIELD_DIM = 16;
//    private final long id;
//    private final ArrayList<FieldHolder> holderGrid = new ArrayList<>();
//
//    private final ConcurrentMap<Long, Tank> tanks = new ConcurrentHashMap<>();
//    private final ConcurrentMap<String, Long> playersIP = new ConcurrentHashMap<>();
//
//    private final Object monitor = new Object();
//
//    public Game() {
//        this.id = 0;
//    }
//
//    @JsonIgnore
//    public long getId() {
//        return id;
//    }
//
//    @JsonIgnore
//    public ArrayList<FieldHolder> getHolderGrid() {
//        return holderGrid;
//    }
//
//    public void addTank(String ip, Tank tank) {
//        synchronized (tanks) {
//            tanks.put(tank.getId(), tank);
//            playersIP.put(ip, tank.getId());
//        }
//    }
//
//    public Tank getTank(int tankId) {
//        return tanks.get(tankId);
//    }
//
//    public ConcurrentMap<Long, Tank> getTanks() {
//        return tanks;
//    }
//
//    public List<Optional<FieldEntity>> getGrid() {
//        synchronized (holderGrid) {
//            List<Optional<FieldEntity>> entities = new ArrayList<Optional<FieldEntity>>();
//
//            FieldEntity entity;
//            for (FieldHolder holder : holderGrid) {
//                if (holder.isPresent()) {
//                    entity = holder.getEntity();
//                    entity = entity.copy();
//
//                    entities.add(Optional.<FieldEntity>of(entity));
//                } else {
//                    entities.add(Optional.<FieldEntity>empty());
//                }
//            }
//            return entities;
//        }
//    }
//
//    public Tank getTank(String ip){
//        if (playersIP.containsKey(ip)){
//            return tanks.get(playersIP.get(ip));
//        }
//        return null;
//    }
//
//    public void removeTank(long tankId){
//        synchronized (tanks) {
//            Tank t = tanks.remove(tankId);
//            if (t != null) {
//                playersIP.remove(t.getIp());
//            }
//        }
//    }
//
//    public int[][] getGrid2D() {
//        int[][] grid = new int[FIELD_DIM][FIELD_DIM];
//
//        synchronized (holderGrid) {
//            FieldHolder holder;
//            for (int i = 0; i < FIELD_DIM; i++) {
//                for (int j = 0; j < FIELD_DIM; j++) {
//                    holder = holderGrid.get(i * FIELD_DIM + j);
//                    if (holder.isPresent()) {
//                        grid[i][j] = holder.getEntity().getIntValue();
//                    } else {
//                        grid[i][j] = 0;
//                    }
//                }
//            }
//        }
//
//        return grid;
//    }
//}