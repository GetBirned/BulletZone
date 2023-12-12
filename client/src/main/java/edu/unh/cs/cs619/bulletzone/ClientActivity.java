package edu.unh.cs.cs619.bulletzone;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.NonConfigurationInstance;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.api.BackgroundExecutor;
import org.androidannotations.rest.spring.annotations.RestService;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Timer;
import java.util.TimerTask;

import edu.unh.cs.cs619.bulletzone.events.BusProvider;
import edu.unh.cs.cs619.bulletzone.events.ShakeDetector;
import edu.unh.cs.cs619.bulletzone.rest.BZRestErrorhandler;
import edu.unh.cs.cs619.bulletzone.rest.BulletZoneRestClient;
import edu.unh.cs.cs619.bulletzone.rest.GridPollerTask;
import edu.unh.cs.cs619.bulletzone.rest.GridUpdateEvent;
import edu.unh.cs.cs619.bulletzone.ui.GridAdapter;
import edu.unh.cs.cs619.bulletzone.util.GridWrapper;
import edu.unh.cs.cs619.bulletzone.util.IntegerWrapper;
import edu.unh.cs.cs619.bulletzone.util.LongWrapper;


@EActivity(R.layout.activity_client)
public class ClientActivity extends Activity {

    private static final String TAG = "ClientActivity";

    @Bean
    protected GridAdapter mGridAdapter;

    private Timer healthUpdateTimer;

    @ViewById
    protected GridView gridView;

    String file_timestamp;




    @Bean
    BusProvider busProvider;

    @NonConfigurationInstance
    @Bean
    GridPollerTask gridPollTask;

    EditText editDirection;

    @RestService
    BulletZoneRestClient restClient;


    @Bean
    BZRestErrorhandler bzRestErrorhandler;
    byte previousBuilderDirection = 0;
    byte previousTankDirection = 0;

    /**
     * Remote tank identifier
     */
    private long tankId = -1;

    private int curBalance;

    private long soldierId = -1;

    private long builderId = -1;
    private SensorManager sensorManager;
    private Sensor mAccelerometer;

    ShakeDetector mShakeDetector;
    ClientController controller = new ClientController();
    @ViewById(R.id.bank_balance)
    TextView bankBalanceTextView;
    int controllingTank;

    int controllingBuilder;
    ButtonController buttonController;
    private int tankIsActive;

    public String receivedTankID;

    public int calledMove;

    public int calledTurn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restClient.setRestErrorHandler(bzRestErrorhandler);
        controller.construct(restClient);
        buttonController = new ButtonController(this);
        // Establish shake/sensorManager. Will handle shakes.
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake() {
                Log.d(TAG, "Shake initiated, firing bullet");
                onButtonFire();
            }
        });

        // file gets way way way way too large too quickly
        // Delete the file
        //boolean deleted = this.deleteFile("replay_file.txt");
        // sending context over
        mGridAdapter.getContext(this);
        String filename_ts = createNewFile();
        mGridAdapter.setTs(filename_ts);

        // Receiving data in the NextActivity
        Intent receivedIntent = getIntent();
        String recievedTankID = receivedIntent.getStringExtra("username");
        mGridAdapter.setUsername(recievedTankID);
        this.receivedTankID = recievedTankID;


        sensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
    }

    @Click(R.id.ejectPowerup)
    @Background
    protected void ejectPowerup(){
        int type = controller.ejectPowerupAsync(isSoldierDeployed, tankId, controllingTank);
        if(type == -1) {
            noPowerupToEjectToast(this, "No Powerup To Eject!");
        }
        mGridAdapter.didEject = true;
        mGridAdapter.ejectedType = type;
    }

    @UiThread
    public void noPowerupToEjectToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }


    private String createNewFile() {
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        try {

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.openFileOutput(ts + ".txt", Context.MODE_PRIVATE));
            outputStreamWriter.write("");
            outputStreamWriter.close();
            Log.d("FILE", "created new file with ts " + ts);
            file_timestamp = ts;
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
        return ts;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        busProvider.getEventBus().unregister(gridEventHandler);
        sensorManager.unregisterListener(mShakeDetector);
        stopHealthUpdateTimer();
    }

    /**
     * Otto has a limitation (as per design) that it will only find
     * methods on the immediate class type. As a result, if at runtime this instance
     * actually points to a subclass implementation, the methods registered in this class will
     * not be found. This immediately becomes a problem when using the AndroidAnnotations
     * framework as it always produces a subclass of annotated classes.
     *
     * To get around the class hierarchy limitation, one can use a separate anonymous class to
     * handle the events.
     */
    private GridWrapper currentGridWrapper;
    private Object gridEventHandler = new Object()
    {
        @Subscribe
        public void onUpdateGrid(GridUpdateEvent event) {
            if (event.gw != null) {
                currentGridWrapper = event.gw;
                updateGrid(event.gw);
            }
        }

        public GridWrapper getCurrentGridWrapper() {
            return currentGridWrapper;
        }
    };


    @AfterViews
    protected void afterViewInjection() {
        joinAsync();
        SystemClock.sleep(500);
        gridView.setAdapter(mGridAdapter);
        mGridAdapter.setRestClient(restClient);
        mGridAdapter.setClientController(controller);
    }

    private Timer soldierHealthUpdateTimer;

    @AfterInject
    void afterInject() {

        //controller.setErrorHandler(bzRestErrorhandler);
        busProvider.getEventBus().register(gridEventHandler);
        startHealthUpdateTimer();
    }

    private void startHealthUpdateTimer() {
        healthUpdateTimer = new Timer();
        healthUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Call the method to update health information

                int total = controller.updateBankAccountAsync(receivedTankID);
                updateBalance(total);
                curBalance = total;
                updateTankHealth((int)controller.updateHealthAsync(tankId));
                updateBuilderHealth(controller.updateBuilderHealthAsync(tankId));
                //TODO: ADD builder health method here
            }
        }, 0, 1000); // Update health every 5 seconds (adjust the interval as needed)
        soldierHealthUpdateTimer = new Timer();
        soldierHealthUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Call the method to update soldier's health information
                if (isSoldierDeployed) {
                    updateSoldierHealth(controller.updateSoldierHealthAsync(soldierId));

                }
            }
        }, 0, 1000);
    }

    private void stopHealthUpdateTimer() {
        if (healthUpdateTimer != null) {
            healthUpdateTimer.cancel();
            healthUpdateTimer = null;
        }
        if (soldierHealthUpdateTimer != null) {
            soldierHealthUpdateTimer.cancel();
            soldierHealthUpdateTimer = null;
        }
    }
    @Background
    void joinAsync() {
        try {
            this.tankId = controller.getResult();
            gridPollTask.doPoll();
            buttonController.initializeButtons();
            controllingTank = 1;
            controllingBuilder = 0;
            buttonController.updateButtons(controllingBuilder);
            tankIsActive = 1;
            updateTankHealth((int)controller.updateHealthAsync(tankId));
            int total = controller.updateBankAccountAsync(receivedTankID);
            updateBalance(total);
            curBalance = total;
            updateBuilderHealth(controller.updateBuilderHealthAsync(tankId));
        } catch (Exception e) {
            System.out.println("ERROR: joining game");
        }
    }



    public void updateGrid(GridWrapper gw) {
        if (gw != null) {
            mGridAdapter.updateList(gw.getGrid());
        } else {
            Log.e(TAG, "GridWrapper is null");
        }
    }




    byte tempDirection;
    @Click({R.id.buttonUp, R.id.buttonDown, R.id.buttonLeft, R.id.buttonRight})
    @Background
    protected void onButtonMove(View view) {
        calledMove = 1;
        if (currentlyBuilding == 0) {
            final int viewId = view.getId();
            byte direction = 0;
            final Object lock = new Object();

            switch (viewId) {
                case R.id.buttonUp:
                    direction = 0;
                    tempDirection = 0;
                    //    tankId = 0;
                    break;
                case R.id.buttonDown:
                    direction = 4;
                    tempDirection = 4;
                    //   tankId = 4;
                    break;
                case R.id.buttonLeft:
                    direction = 6;
                    tempDirection = 6;
                    //   tankId = 6;
                    break;
                case R.id.buttonRight:
                    direction = 2;
                    tempDirection = 2;
                    // tankId = 2;
                    break;
                default:
                    Log.e(TAG, "Unknown movement button id: " + viewId);
                    break;
            }
            if (controllingTank == 1) {
                if (previousTankDirection == direction) {
                    previousTankDirection = tempDirection;
                    controller.moveAsync(tankId, direction);
                } else {
                    if (previousTankDirection == 2 && direction == 6) {
                        previousTankDirection = tempDirection;
                        controller.moveAsync(tankId, direction);
                    } else if (previousTankDirection == 6 && direction == 2) {
                        previousTankDirection = tempDirection;
                        controller.moveAsync(tankId, direction);
                    } else if (previousTankDirection == 0 && direction == 4) {
                        previousTankDirection = tempDirection;
                        controller.moveAsync(tankId, direction);
                    } else if (previousTankDirection == 4 && direction == 0) {
                        previousTankDirection = tempDirection;
                        controller.moveAsync(tankId, direction);
                    } else {
                        previousTankDirection = tempDirection;
                        controller.turnAsync(tankId, direction);
                    }
                }
            } else {
                if (previousBuilderDirection == direction) {
                    previousBuilderDirection = tempDirection;
                    controller.moveAsync(tankId, direction);
                } else {
                    if (previousBuilderDirection == 2 && direction == 6) {
                        previousBuilderDirection = tempDirection;
                        controller.moveAsync(tankId, direction);
                    } else if (previousBuilderDirection == 6 && direction == 2) {
                        previousBuilderDirection = tempDirection;
                        controller.moveAsync(tankId, direction);
                    } else if (previousBuilderDirection == 0 && direction == 4) {
                        previousBuilderDirection = tempDirection;
                        controller.moveAsync(tankId, direction);
                    } else if (previousBuilderDirection == 4 && direction == 0) {
                        previousBuilderDirection = tempDirection;
                        controller.moveAsync(tankId, direction);
                    } else {
                        previousBuilderDirection = tempDirection;
                        controller.turnAsync(tankId, direction);
                    }
                }
            }
        }
    }

    @Click(R.id.deploySoldier)
    @Background
    protected void deploySoldier() {
        if (controllingTank == 1) {
            long res = controller.deploySoldierAsync(tankId, soldierId, isSoldierDeployed);
            if (res == -1) {
                isSoldierDeployed = false;
                soldierId = -1;
            } else {
                isSoldierDeployed = true;
                soldierId = res;
            }

        } else {
            showCannotDeployMessage();
        }
    }

    @UiThread
    protected void showCannotDeployMessage() {
        Toast.makeText(getApplicationContext(), "Cannot deploy soldier when controlling Builder. Please switch to Tank then try again.", Toast.LENGTH_SHORT).show();
    }
    private boolean isSoldierDeployed = false;



    @UiThread
    public void updateSoldierHealth(int health) {
        TextView soldierHealthTextView = findViewById(R.id.soldierHealth);
        if (health != 0) {
            soldierHealthTextView.setText("" + health);
        } else {
            soldierHealthTextView.setText("N/A"); // or any default value
        }
    }

    // Method to reset soldier status after reentry
    private void resetSoldierStatus() {
        isSoldierDeployed = false;
    }

    @Click(R.id.buttonFire)
    @Background
    protected void onButtonFire() {
        controller.fire(tankId);
    }

    @Click(R.id.buttonReplay)
    @Background
    void replayButton() {
        Intent intent = new Intent(this, ReplayActivity.class);
        startActivity(intent);
    }

    @Click(R.id.buttonLeave)
    @Background
    void leaveGame() {
        showConfirmationDialog();
    }

    public void performLeave() {
        System.out.println("leaveGame() called, tank ID: "+tankId);
        BackgroundExecutor.cancelAll("grid_poller_task", true);
        controller.leave(tankId);
    }

    @UiThread
    public void showConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to quit BulletZone?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked Yes, proceed with leave action
                        performLeave();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Click(R.id.controlBuilder)
    @Background
    protected void controlBuilder() {
        controllingTank = 0;
        controllingBuilder = 1;
        buttonController.updateButtons(controllingBuilder);
        controller.controlBuilder(tankId);
    }

    @Click(R.id.controlTank)
    @Background
    protected void controlTank() {
        controllingTank = 1;
        controllingBuilder = 0;
        buttonController.updateButtons(controllingBuilder);
        controller.controlTank(tankId);
    }

    @Click(R.id.buildBridge)
    @Background
    void buildBridge() {
        if (curBalance >= 80) {
            startBuildTimer(3);
            //buildImprovement(3, tankId);
        } else {
            Log.d(TAG, "Bridge could not be built. Bank Account associated to " +
                    "ID: " + tankId + " doesn't have more than 80 credits.\n");
        }
    }

    @Click(R.id.buildRoad)
    @Background
    void buildRoad() {
        if (curBalance >= 40) {
            startBuildTimer(2);
            //buildImprovement(2 , tankId);
        } else {
            Log.d(TAG, "Road could not be built. Bank Account associated to " +
                    "ID: " + tankId + " doesn't have more than 40 credits.\n");
        }
    }

    @Click(R.id.buildWall)
    @Background
    void buildWall() {
        if (curBalance >= 100) {
            startBuildTimer(1);
            //buildImprovement(1, tankId);
        } else {
            Log.d(TAG, "Wall could not be built. Bank Account associated to " +
                    "ID: " + tankId + " doesn't have more than 100 credits.\n");
        }
    }


    int currentlyBuilding;

    public void startBuildTimer(int choice) {
        currentlyBuilding = 1;
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                // Check if calledMove is still 0, otherwise, exit the task
                    try {
                        // Ensure UI updates are done on the UI thread
                        if(controller.buildImprovement(choice, builderId, controllingBuilder, tankId, receivedTankID) == -1){
                            showCannotBuildMessage();
                        }

                        int total = controller.updateBankAccountAsync(receivedTankID);
                        updateBalance(total);
                        curBalance = total;
                        Log.d(TAG, "buildImprovement executed successfully.\n");
                        currentlyBuilding = 0;
                    } catch (Exception e) {
                        // Handle exceptions if necessary
                        Log.e(TAG, "Error during UI update or buildImprovement", e);
                    } finally {
                        // Optionally, you can cancel the timer task after executing buildImprovement
                        cancel();
                    }
            }
        };

        long buildTime = controller.getBuildTime(tankId);
        if (buildTime != 2000) {
            buildTime = 1000;
        }
        // Schedule the task to run after 2 seconds
        timer.schedule(timerTask, buildTime);
        Log.d(TAG, "Timer started. Waiting to build for " + buildTime + " milliseconds or until calledMove is set to 1...\n");
    }

    public long getDismantleTime(long tankId) {
        LongWrapper dismantleTime = controller.getDismantleTime(tankId);
        if (dismantleTime == null) {
            Log.e(TAG, "dismantleTime could not be received from server.");
        } else {
            return dismantleTime.getResult();
        }
        return 0;
    }

    public void startDismantleTimer() {
        calledMove = 0;
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                // Check if calledMove is still 0, otherwise, exit the task
                if (calledMove == 0) {
                    try {
                        // Ensure UI updates are done on the UI thread
                        if(controller.dismantleImprovementAsync(builderId, receivedTankID,controllingBuilder) == false) {
                            showCannotDismantleMessage();
                        }
                        int total = controller.updateBankAccountAsync(receivedTankID);
                        updateBalance(total);
                        curBalance = total;
                    } catch (Exception e) {
                        // Handle exceptions if necessary
                        Log.e(TAG, "Error during UI update or dismantleImprovement", e);
                    } finally {
                        // Optionally, you can cancel the timer task after executing buildImprovement
                        cancel();
                    }
                } else {
                    Log.d(TAG, "Timer aborted due to calledMove being set to 1.");
                }
            }
        };

        long buildTime = getDismantleTime(tankId);
        //if (buildTime != 2000) {
          //  buildTime = 1000;
        //}
        // Schedule the task to run after 2 seconds
        timer.schedule(timerTask, buildTime);
        Log.d(TAG, "Timer started. Waiting to dismantle for " + buildTime + " milliseconds or until calledMove is set to 1...");
    }


    @Click(R.id.buildMine)
    @Background
    void buildMine() {
        if (curBalance >= 20) {
            Log.d("MINE", "set mine");
            if(controller.buildTrap(1, soldierId, controllingTank, tankId,  receivedTankID) == -1) {
                showCannotBuildTrapMessage();
            }

            int total = controller.updateBankAccountAsync(receivedTankID);
            updateBalance(total);
            curBalance = total;
        } else {
            Log.d(TAG, "Mine could not be built. Bank Account associated to " +
                    "ID: " + tankId + " doesn't have more than 20 credits.\n");
        }
    }

    @Click(R.id.buildHijackTrap)
    @Background
    void buildHijackTrap() {
        if (curBalance >= 40) {
            if(controller.buildTrap(2, soldierId, controllingTank, tankId,  receivedTankID) == -1) {
                showCannotBuildTrapMessage();
            }

            int total = controller.updateBankAccountAsync(receivedTankID);
            updateBalance(total);
            curBalance = total;
        } else {
            Log.d(TAG, "HijackTrap could not be built. Bank Account associated to " +
                    "ID: " + tankId + " doesn't have more than 40 credits.\n");
        }
    }


    public int getTankIDFromFile() {
        try {
            InputStream inputStream = this.openFileInput(receivedTankID + ".txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String receiveString = bufferedReader.readLine();
                int tankID = Integer.parseInt(receiveString);
                //Log.d("Sending", "tankID from file is " + tankID);

                inputStream.close();
                return tankID;
            } else {
                Log.d("ERROR", "Could not parse file");
                return -1;
            }
        }
        catch (FileNotFoundException e) {
            Log.e("TANKID FILE", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("TANKID FILE", "Can not read file: " + e.toString());
        }
        return -1;
    }

    public void buildTrap(int choice, long soldierId, int userID) {
        if (controllingTank == 1) {
            LongWrapper res = controller.buildTrap(choice, tankId, userID);
            if (res != null) {
                if (res.getResult() == 1) {
                    Log.d(TAG, "Mine properly built by ID: " + tankId + "\n");
                    controller.updateBalance(receivedTankID, -20);
                } else if (res.getResult() == 2) {
                    Log.d(TAG, "Hijack Trap properly built by ID: " + tankId + "\n");
                    controller.updateBalance(receivedTankID, -40);
                }
                updateBankAccountAsync(receivedTankID);
            } else {
                Log.d(TAG, "Trap build failed with ID: " + tankId + "\n");
            }
        } else {
            showCannotBuildTrapMessage();
        }
    }

    @UiThread
    protected void showCannotBuildTrapMessage() {
        Toast.makeText(getApplicationContext(), "Cannot construct trap as builder. Please switch to Soldier then try again.\n", Toast.LENGTH_SHORT).show();
    }

    @UiThread
    protected void showCannotBuildMessage() {
        Toast.makeText(getApplicationContext(), "Cannot build while controlling Tank/Soldier. Please switch to Builder then try again.\n", Toast.LENGTH_SHORT).show();
    }

    @UiThread
    protected void showCannotBuildOnTopMessage() {
        Toast.makeText(getApplicationContext(), "Cannot build an improvement on top of another improvement / invalid area.\n", Toast.LENGTH_SHORT).show();
    }

    @UiThread
    protected void showCannotBuildBridgeMessage() {
        Toast.makeText(getApplicationContext(), "Cannot build bridge when not back is not facing water. Try again.\n", Toast.LENGTH_SHORT).show();
    }

    @Click(R.id.dismantleImprovement)
    @Background
    void dismantleImprovement() {
        startDismantleTimer();
    }



    @UiThread
    protected void showCannotDismantleMessage() {
        Toast.makeText(getApplicationContext(), "Cannot dismantle while controlling Tank/Soldier. Please switch to Builder then try again.\n", Toast.LENGTH_SHORT).show();
    }

    @UiThread
    public void updateTankHealth(int health) {
        TextView tankHealthTextView = findViewById(R.id.tankHealth);
        tankHealthTextView.setText("" + health);
    }
    @UiThread
    public void updateBalance(int health) {
        TextView builderHealthTextView = findViewById(R.id.bank_balance);

        builderHealthTextView.setText("" + health);

    }

    @UiThread
    public void updateBuilderHealth(int health) {
        TextView builderHealthTextView = findViewById(R.id.builderHealth);
        if(health <= 0) {
            builderHealthTextView.setText("0");
        } else {
            builderHealthTextView.setText("" + health);
        }
    }

}

