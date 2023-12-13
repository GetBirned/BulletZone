package edu.unh.cs.cs619.bulletzone.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import edu.unh.cs.cs619.bulletzone.util.LongWrapper;
import jdk.internal.org.jline.utils.Log;

public final class Game {
    /**
     * Field dimensions
     */
    private static final int FIELD_DIM = 16;
    private static final int REPAIR_KIT_EFFECT_DURATION = 120; // 120 seconds
    private static final int DEFLECTOR_SHIELD_DAMAGE_REDUCTION = 1; // Damage reduction per second

    private final long id;
    private long lastEjectionTime;
    double chance;
    private final ConcurrentMap<Long, Soldier> soldiers = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Tank> tanks = new ConcurrentHashMap<>();

    private final ConcurrentMap<Long, Builder> builders = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> playersIP = new ConcurrentHashMap<>();
    private final Object monitor = new Object();
    private GameBoardBuilder gbb = null;
    private GameBoard gb = null;
    int numItems = 5;

    public Game() {
        this.id = 0;
        this.initialize();
    }

    public void initialize() {
        if (gbb != null) {
            gb = gbb.build();
            return;
        }
        gbb = new GameBoardBuilder();
        gb = gbb.build();
    }

    @JsonIgnore
    public long getId() {
        return id;
    }

    @JsonIgnore
    public ArrayList<FieldHolder> getHolderGrid() {
        return gb.getHolderGrid();
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
        synchronized (gb.getHolderGrid()) {
            List<Optional<FieldEntity>> entities = new ArrayList<Optional<FieldEntity>>();

            FieldEntity entity;
            for (FieldHolder holder : gb.getHolderGrid()) {
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
        setPowerup();
        synchronized (gb.getHolderGrid()) {
            FieldHolder holder;
            for (int i = 0; i < FIELD_DIM; i++) {
                for (int j = 0; j < FIELD_DIM; j++) {
                    holder = gb.getHolderGrid().get(i * FIELD_DIM + j);
                    if (holder.isPresent()) {
                        //System.out.print(holder.getEntity().toString() + ", ");
                        grid[i][j] = holder.getEntity().getIntValue(); // changing to 2345 turns everything into mines
                    } else {
                        grid[i][j] = 0;
                    }
                }
                //System.out.println();
            }
        }

        return grid;
    }

    // 0 grass, // 1 thingamajig //2 nuke //3 apple
    //4 hill // 5 rocky // 6 forest // 7 soldier // 8 water
    //9 deflector //10 repair kit // 11 bridge // 12 road

    public void setPowerup() {
        System.out.println("Made it here TO POWERUPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP");
        int numPlayers = tanks.size();
        numItems = countPowerUps();
        if(numItems > 7) {
            return;
        }
        double res1 = (double)numPlayers / (numItems + 1);
        double res = (0.25 * res1);
        System.out.println("VAL IS " + res);
        System.out.println("there are this many tanks" + numPlayers + " and this many items " + numItems);
        if (res > 0) {
            System.out.println("Made it here");
            // Determine whether to place a power-up

            if(shouldPlacePowerUp()) {
                int row = generateRandomNumber();
                int col = generateRandomNumber();


                FieldHolder holder = gb.get(row,col);

                if (holder.getEntity() instanceof Road || holder.getEntity() instanceof Water || holder.getEntity() instanceof Bridge
                        || holder.getEntity() instanceof Hill || holder.getEntity() instanceof Rocky || holder.getEntity() instanceof Wall
                        || holder.getEntity() instanceof Forest || holder.getEntity() instanceof Tank || holder.getEntity() instanceof Soldier
                        || holder.getEntity() instanceof Builder) {
                    return;
                } else {


                    System.out.println("should place");
                    // Check if the selected position is empty
                    //if (!gb.getHolderGrid().get(row * FIELD_DIM + col).isPresent()) {
                    int appear = new Random().nextInt(5);
                    appear = appear + 1;
                    System.out.println("Random value: " + appear);
                    switch (appear) {
                        case 1:
                            gb.setEntity(row, col, new Thingamajig());
                            System.out.println(" value: " + new Thingamajig().getIntValue());
                            break;
                        case 2:
                            gb.setEntity(row, col, new nukePowerUp());
                            System.out.println(" value: " + new nukePowerUp().getIntValue());
                            break;
                        case 3:
                            gb.setEntity(row, col, new applePowerUp());
                            System.out.println(" value: " + new applePowerUp().getIntValue());
                            break;
                        case 4:
                            gb.setEntity(row, col, new Shield());
                            System.out.println(" value: " + new Shield().getIntValue());
                            break;
                        case 5:
                            gb.setEntity(row, col, new HealthKit());
                            System.out.println(" value: " + new HealthKit().getIntValue());
                            break;
                    }

                    //numItems++;
                    //}
                }
            }
        }
    }


    private static int generateRandomNumber() {
        // Create an instance of the Random class
        Random random = new Random();

        // Generate a random number between 0 (inclusive) and 16 (exclusive)
        int randomNumber = random.nextInt(16);

        return randomNumber;
    }

    private int countPowerUps() {
        int count = 0;
        for (FieldHolder holder : gb.getHolderGrid()) {
            if (holder.isPresent() && isPowerUp(holder.getEntity())) {
                count++;
            }
        }
        return count;
    }

    private boolean isPowerUp(FieldEntity entity) {
        // Add conditions to check if the entity is a power-up
        return entity instanceof Thingamajig
                || entity instanceof nukePowerUp
                || entity instanceof applePowerUp
                || entity instanceof Shield
                || entity instanceof HealthKit;
    }

    private boolean shouldPlacePowerUp() {
        int randNum = new Random().nextInt(101);
        return randNum <= (0.02 * 100);
    }


    public TankLocation findTank(Tank tank, long tankID) {
        /**
        if (tanks.containsKey(tankID)) {
            if (tanks.get(tankID).getTankLocation() == null) {
                synchronized (gb.getHolderGrid()) {
                    FieldHolder holder;

                    for (int i = 0; i < FIELD_DIM; i++) {
                        for (int j = 0; j < FIELD_DIM; j++) {
                            holder = gb.getHolderGrid().get(i * FIELD_DIM + j);
                            if (holder.isPresent() && holder.getEntity() instanceof Tank) {
                                Tank currentTank = (Tank) holder.getEntity();
                                if (currentTank.getId() == tankID) {
                                    tanks.get(tankID).setTankLocation(new TankLocation(i, j));
                                }
                            }
                        }
                    }
                }
            }
            return tanks.get(tankID).getTankLocation();
        } else {
            return null;
        }
         */
        synchronized (gb.getHolderGrid()) {
            FieldHolder holder;

            for (int i = 0; i < FIELD_DIM; i++) {
                for (int j = 0; j < FIELD_DIM; j++) {
                    holder = gb.getHolderGrid().get(i * FIELD_DIM + j);
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
    public TankLocation findSoldier(Soldier soldier, long soldierID) {
        /*
        if (soldiers.containsKey(soldierID)) {
            if(soldiers.get(soldierID).getTankLocation() == null) {
                synchronized (gb.getHolderGrid()) {
                    FieldHolder holder;
                    for (int i = 0; i < FIELD_DIM; i++) {
                        for (int j = 0; j < FIELD_DIM; j++) {
                            holder = gb.getHolderGrid().get(i * FIELD_DIM + j);
                            if (holder.isPresent() && holder.getEntity() instanceof Soldier) {
                                Soldier currentSoldier = (Soldier) holder.getEntity();
                                if (currentSoldier.getId() == soldierID) {
                                    soldiers.get(soldierID).setTankLocation(new TankLocation(i, j));
                                }
                            }
                        }
                    }
                }
            }
            return soldiers.get(soldierID).getTankLocation();
        } else {
            return null;
        }
         */
        synchronized (gb.getHolderGrid()) {
            FieldHolder holder;
            for (int i = 0; i < FIELD_DIM; i++) {
                for (int j = 0; j < FIELD_DIM; j++) {
                    holder = gb.getHolderGrid().get(i * FIELD_DIM + j);
                    if (holder.isPresent() && holder.getEntity() instanceof Soldier) {
                        Soldier currentSoldier = (Soldier) holder.getEntity();
                        if (currentSoldier.getId() == soldierID) {
                            return new TankLocation(i, j);
                        }
                    }
                }
            }
        }
        return null;
    }

    public TankLocation findBuilder(Builder builder, long builderID) {
        /**
        if (builders.containsKey(builderID)) {
            if (builders.get(builderID).getTankLocation() == null) {
                synchronized (gb.getHolderGrid()) {
                    FieldHolder holder;
                    for (int i = 0; i < FIELD_DIM; i++) {
                        for (int j = 0; j < FIELD_DIM; j++) {
                            holder = gb.getHolderGrid().get(i * FIELD_DIM + j);
                            if (holder.isPresent() && holder.getEntity() instanceof Builder) {
                                Builder currentSoldier = (Builder) holder.getEntity();
                                if (currentSoldier.getId() == builderID) {
                                    builders.get(builderID).setTankLocation(new TankLocation(i, j));
                                }
                            }
                        }
                    }
                }
            }
            return builders.get(builderID).getTankLocation();
        } else {
            return null;
        }
         */
        synchronized (gb.getHolderGrid()) {
            FieldHolder holder;
            for (int i = 0; i < FIELD_DIM; i++) {
                for (int j = 0; j < FIELD_DIM; j++) {
                    holder = gb.getHolderGrid().get(i * FIELD_DIM + j);
                    if (holder.isPresent() && holder.getEntity() instanceof Builder) {
                        Builder currentSoldier = (Builder) holder.getEntity();
                        if (currentSoldier.getId() == builderID) {
                            return new TankLocation(i, j);
                        }
                    }
                }
            }
        }
        return null;
    }
    public GameBoard getGameBoard() {
        return this.gb;
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

    public LongWrapper getBuildTime(long builderId) {
        Builder builder = getBuilders().get(builderId);
        if (builder != null) {
            FieldHolder fieldElement = gb.getHolderGrid().get(getPosition(builder, builderId)); // find the FieldHolder of element behind builder
            if (fieldElement.getEntity() instanceof Water) {
                builder.setBuildTime(1000);
                return new LongWrapper(1000);
            } else if (fieldElement.getEntity() instanceof Hill || fieldElement.getEntity() instanceof Rocky
                    || fieldElement.getEntity() instanceof Forest) {
                builder.setBuildTime(2000);
                return new LongWrapper(2000);
            }
        } else {
            throw new IllegalArgumentException("Builder associated with Id: " + builderId + " not found.");
        }
        return new LongWrapper(1000);
    }

    public LongWrapper getDismantleTime(long builderId) {
        Builder builder = getBuilders().get(builderId);
        if (builder != null) {

            int[] specialPositions = {
                    50, 66, 82, 98,
                    51, 67, 83, 99,
                    149, 165, 181, 197,
                    150, 166, 182, 198,
                    202, 218, 203, 219,
                    58, 74, 90, 106,
                    59, 75, 91, 107
            };

            int result = getPosition(builder, builderId);
            FieldHolder fieldElement = gb.getHolderGrid().get(result); // find the FieldHolder of element behind builder
            if (fieldElement.getEntity() instanceof Bridge) {
                return new LongWrapper(1000);
            }
        } else {
            throw new IllegalArgumentException("Builder associated with Id: " + builderId + " not found.");
        }
        return new LongWrapper(1000);
    }

    public int getPosition(Builder builder, long builderId) {
        TankLocation builderLocation = findBuilder(builder, builderId);
        int x = builderLocation.getRow();
        int y = builderLocation.getColumn();
        int direction = (builder.getIntValue() % 10);
        int[] offset = getOffsetForDirection(direction);

        int newX = x + offset[0];
        int newY = y + offset[1];
        return newX * FIELD_DIM + newY;
    }

    public LongWrapper dismantleImprovement(long builderId) {
        Builder builder = getBuilders().get(builderId);
        if (builder != null) {
            FieldHolder fieldElement = gb.getHolderGrid().get(getPosition(builder, builderId)); // find the FieldHolder of element behind builder
            if (fieldElement.getEntity() instanceof BuilderWall) { // WALL - RETURN 100 CREDITS
                BuilderWall wall = (BuilderWall) fieldElement.getEntity();
                wall.getParent().clearField();
                wall.setParent(null);
                return new LongWrapper(1);
            } else if (fieldElement.getEntity() instanceof Road) { // ROAD - RETURN 40 CREDITS
                Road road = (Road) fieldElement.getEntity();
                road.getParent().clearField();
                road.setParent(null);
                return new LongWrapper(2);
            } else if (fieldElement.getEntity().getIntValue() == 60) { // BRIDGE - RETURN 80 CREDITS
                Bridge bridge = (Bridge) fieldElement.getEntity();
                bridge.getParent().clearField();
                bridge.setParent(null);
                fieldElement.setFieldEntity(new Water());
                return new LongWrapper(3);
            } else if (fieldElement.getEntity() instanceof applePowerUp || fieldElement.getEntity().getIntValue() == 2002) { // ANTIGRAV - RETURN 300 CREDITS
                applePowerUp apple = (applePowerUp) fieldElement.getEntity();
                apple.getParent().clearField();
                apple.setParent(null);
                fieldElement.setFieldEntity(new Grass());
                return new LongWrapper(4);
            } else if (fieldElement.getEntity() instanceof nukePowerUp|| fieldElement.getEntity().getIntValue() == 2003) { // FUSION - RETURN 400 CREDITS
                nukePowerUp nuke = (nukePowerUp) fieldElement.getEntity();
                nuke.getParent().clearField();
                nuke.setParent(null);
                fieldElement.setFieldEntity(new Grass());
                return new LongWrapper(5);
            } else if (fieldElement.getEntity() instanceof Shield|| fieldElement.getEntity().getIntValue() == 3131) { // SHIELD - RETURN 300 CREDITS
                Shield s = (Shield) fieldElement.getEntity();
                if (!fieldElement.isPresent()) {
                    fieldElement.setFieldEntity(s);
                    s.setParent(fieldElement);
                }
                s.getParent().clearField();
                s.setParent(null);
                fieldElement.setFieldEntity(new Grass());
                return new LongWrapper(6);
            } else if (fieldElement.getEntity() instanceof HealthKit|| fieldElement.getEntity().getIntValue() == 3141) { // TOOLKIT - RETURN 200 CREDITS
                HealthKit h = (HealthKit) fieldElement.getEntity();
                h.getParent().clearField();
                h.setParent(null);
                fieldElement.setFieldEntity(new Grass());
                return new LongWrapper(7);
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
            FieldHolder fieldElement = gb.getHolderGrid().get(newX * FIELD_DIM + newY);

            if (choice == 1) { // WALL - COSTS 100 CREDITS
                BuilderWall wall = new BuilderWall();
                 if (!fieldElement.isPresent()) {
                    fieldElement.setFieldEntity(wall);
                    wall.setParent(fieldElement);
                    return new LongWrapper(1);
                }
                if (!(fieldElement.getEntity() instanceof Wall) && !(fieldElement.getEntity() instanceof Water)
                        && !(fieldElement.getEntity() instanceof BuilderWall)) {
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
                if (!(fieldElement.getEntity() instanceof Wall) && !(fieldElement.getEntity() instanceof Water)
                        && !(fieldElement.getEntity() instanceof BuilderWall)) {
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

    public LongWrapper buildTrap(int choice, long tankID, int userID) {
        System.out.println("SET TRAP SET TRAP SET TRAP");
        Soldier soldier = getSoldier(tankID);
        if (soldier != null) {
            TankLocation soldierLocation = findSoldier(soldier, tankID);
            int x = soldierLocation.getRow();
            int y = soldierLocation.getColumn();
            int direction = (soldier.getIntValue() % 10);
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

            if (choice == 1) { // Mine -20 credits
                System.out.println("SET MINE");
                Mine mine = new Mine(userID);
                if (!fieldElement.isPresent()) {
                    fieldElement.setFieldEntity(mine);
                    mine.setParent(fieldElement);
                    return new LongWrapper(1);
                }
            } else if (choice == 2) { // Hijack Trap -40 credits
                System.out.println("SET HIJACK TRAP");
                HijackTrap hijackTrap = new HijackTrap(userID);
                if (!fieldElement.isPresent()) {
                    fieldElement.setFieldEntity(hijackTrap);
                    hijackTrap.setParent(fieldElement);
                    return new LongWrapper(2);
                }
            } else { // improper input -- this should never happen with how I have buildChoice added.
                throw new IllegalArgumentException("Improper Build Request: " + choice);
            }
        } else {
            throw new IllegalArgumentException("Soldier associated with Id: " + tankID + " not found.");
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
                                    soldier.setTankLocation(new TankLocation(y, x));
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
    public int getBuilderPowerup(long tankId) {
        Builder curr = builders.get(tankId);
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
        } //DEFLECTOR SHIELD
        if (powerupValue == 9) {
            curr.numShield++;
            curr.deflectorShield();

        }
        // REPAIR KIT
        else if (powerupValue == 10) {
            curr.applyRepairKitEffect(tankId);
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
        //DEFLECTOR SHIELD
        if (powerupValue == 9) {
            curr.numShield++;
            curr.deflectorShield();
        }
        // REPAIR KIT
        else if (powerupValue == 10) {
            curr.applyRepairKitEffect(tankId);
        }

    }
    public ArrayList<Integer> retrieveTankPowerups(long tankId) {
        Tank curr = getTank(tankId);
        return new ArrayList<>(curr.pQ);

    }

    public ArrayList<Integer> retrieveSoldierPowerups(long tankId) {
        Soldier curr = getSoldier(tankId);
        return new ArrayList<>(curr.pQ);
    }

    public ArrayList<Integer> retrieveBuilderPowerups(long tankId) {
        Builder curr = getBuilder(tankId);
        return new ArrayList<>(curr.pQ);
    }
    public void setBuilderPowerup(long tankId, int powerupValue) {
        getTank(tankId).setPowerUpType(powerupValue);
        Builder curr = getBuilder(tankId);
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
        //DEFLECTOR SHIELD
        if (powerupValue == 9) {
            curr.numShield++;
            curr.deflectorShield();
        }
        // REPAIR KIT
        else if (powerupValue == 10) {
            curr.applyRepairKitEffect(tankId);
        }

    }

}