package edu.unh.cs.cs619.bulletzone;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.content.Context;
import android.widget.GridView;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.NonConfigurationInstance;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.api.BackgroundExecutor;
import org.androidannotations.rest.spring.annotations.RestService;

import edu.unh.cs.cs619.bulletzone.events.BusProvider;
import edu.unh.cs.cs619.bulletzone.rest.BZRestErrorhandler;
import edu.unh.cs.cs619.bulletzone.rest.BulletZoneRestClient;
import edu.unh.cs.cs619.bulletzone.rest.GridPollerTask;
import edu.unh.cs.cs619.bulletzone.rest.GridUpdateEvent;
import edu.unh.cs.cs619.bulletzone.ui.GridAdapter;
import edu.unh.cs.cs619.bulletzone.util.GridWrapper;
import edu.unh.cs.cs619.bulletzone.events.ShakeDetector;

@EActivity(R.layout.activity_client)
public class ClientActivity extends Activity {

    private static final String TAG = "ClientActivity";
    private SensorManager sensorManager;
    private Sensor mAccelerometer;

    private ShakeDetector mShakeDetector;

    @Bean
    protected GridAdapter mGridAdapter;

    @ViewById
    protected GridView gridView;

    @Bean
    BusProvider busProvider;

    @NonConfigurationInstance
    @Bean
    GridPollerTask gridPollTask;

    @RestService
    BulletZoneRestClient restClient;

    @Bean
    BZRestErrorhandler bzRestErrorhandler;

    byte previousDirection;
    byte tempDirection;

    /**
     * Remote tank identifier
     */
    private long tankId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        sensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);

    }

    @Override
    public void onPause() {
        sensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        busProvider.getEventBus().unregister(gridEventHandler);
        sensorManager.unregisterListener(mShakeDetector);    }

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
    private Object gridEventHandler = new Object()
    {
        @Subscribe
        public void onUpdateGrid(GridUpdateEvent event) {
            updateGrid(event.gw);
        }
    };


    @AfterViews
    protected void afterViewInjection() {
        joinAsync();
        SystemClock.sleep(500);
        gridView.setAdapter(mGridAdapter);
    }

    @AfterInject
    void afterInject() {
        restClient.setRestErrorHandler(bzRestErrorhandler);
        busProvider.getEventBus().register(gridEventHandler);
    }

    @Background
    void joinAsync() {
        try {
            tankId = restClient.join().getResult();
            gridPollTask.doPoll();
        } catch (Exception e) {
        }
    }

    public void updateGrid(GridWrapper gw) {
        mGridAdapter.updateList(gw.getGrid());
    }

    @Click({R.id.buttonUp, R.id.buttonDown, R.id.buttonLeft, R.id.buttonRight})
    protected void onButtonMove(View view) {
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

        if (previousDirection == direction) {
            previousDirection = tempDirection;
            this.moveAsync(tankId, direction);
        } else {
            if (previousDirection == 2 && direction == 6) {
                previousDirection = tempDirection;
                this.moveAsync(tankId, direction);
            } else if (previousDirection == 6 && direction == 2) {
                previousDirection = tempDirection;
                this.moveAsync(tankId, direction);
            } else if (previousDirection == 0 && direction == 4) {
                previousDirection = tempDirection;
                this.moveAsync(tankId, direction);
            } else if (previousDirection == 4 && direction == 0) {
                previousDirection = tempDirection;
                this.moveAsync(tankId, direction);
            } else {
                previousDirection = tempDirection;
                this.turnAsync(tankId, direction);
            }
        }

    }

    @Background
    void moveAsync(long tankId, byte direction) {
        restClient.move(tankId, direction);
    }

    @Background
    void turnAsync(long tankId, byte direction) {
        restClient.turn(tankId, direction);
    }

    @Click(R.id.buttonFire)
    @Background
    protected void onButtonFire() {
        restClient.fire(tankId);
    }

    @Click(R.id.buttonLeave)
    @Background
    void leaveGame() {
        System.out.println("leaveGame() called, tank ID: "+tankId);
        BackgroundExecutor.cancelAll("grid_poller_task", true);
        restClient.leave(tankId);
    }

    @Click(R.id.buttonLogin)
    void login() {
        Intent intent = new Intent(this, AuthenticateActivity_.class);
        startActivity(intent);
    }

    @Background
    void leaveAsync(long tankId) {
        System.out.println("Leave called, tank ID: " + tankId);
        BackgroundExecutor.cancelAll("grid_poller_task", true);
        restClient.leave(tankId);
    }
}
