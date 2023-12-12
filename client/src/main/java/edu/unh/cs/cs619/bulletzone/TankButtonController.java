package edu.unh.cs.cs619.bulletzone;

import android.app.Activity;
import android.widget.Button;

public class TankButtonController extends Activity implements ButtonStateInterface {

    Button buildWall;
    Button buildRoad;
    Button buildBridge;
    Button dismantle;
    Button deploySoldier;
    Button controlBuilder;
    Button controlTank;
    Button dropMine;
    Button dropHijack;
    Activity activity;

    public TankButtonController(Activity activity) {
        this.activity = activity;
    }
    @Override
    public void initializeButtons() {
        buildWall = activity.findViewById(R.id.buildWall);
        buildRoad = activity.findViewById(R.id.buildRoad);
        buildBridge = activity.findViewById(R.id.buildBridge);
        dismantle = activity.findViewById(R.id.dismantleImprovement);
        deploySoldier = activity.findViewById(R.id.deploySoldier);
        controlBuilder = activity.findViewById(R.id.controlBuilder);
        controlTank = activity.findViewById(R.id.controlTank);
        dropHijack = activity.findViewById(R.id.buildHijackTrap);
        dropMine = activity.findViewById(R.id.buildMine);
    }

    @Override
    public void updateButtons(int controllingTank) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (controllingTank == 1) { // Controlling builder, only show builder buttons
                    buildWall.setEnabled(true);
                    buildWall.setAlpha(1.0f);
                    buildRoad.setEnabled(true);
                    buildRoad.setAlpha(1.0f);
                    buildBridge.setEnabled(true);
                    buildBridge.setAlpha(1.0f);
                    dismantle.setEnabled(true);
                    dismantle.setAlpha(1.0f);
                    deploySoldier.setEnabled(false);
                    deploySoldier.setAlpha(0.5f);
                    controlBuilder.setEnabled(false);
                    controlBuilder.setAlpha(0.5f);
                    controlTank.setEnabled(true);
                    controlTank.setAlpha(1.0f);
                    dropHijack.setEnabled(false);
                    dropHijack.setAlpha(0.5f);
                    dropMine.setEnabled(false);
                    dropMine.setAlpha(0.5f);
                } else { // Controlling tank, only show tank buttons
                    buildWall.setEnabled(true);
                    buildWall.setAlpha(1.0f);
                    buildRoad.setEnabled(true);
                    buildRoad.setAlpha(1.0f);
                    buildBridge.setEnabled(true);
                    buildBridge.setAlpha(1.0f);
                    dismantle.setEnabled(true);
                    dismantle.setAlpha(1.0f);
                    deploySoldier.setEnabled(false);
                    deploySoldier.setAlpha(0.5f);
                    controlBuilder.setEnabled(false);
                    controlBuilder.setAlpha(0.5f);
                    controlTank.setEnabled(true);
                    controlTank.setAlpha(1.0f);
                    dropHijack.setEnabled(false);
                    dropHijack.setAlpha(0.5f);
                    dropMine.setEnabled(false);
                    dropMine.setAlpha(0.5f);
                }
            }
        });
    }
}
