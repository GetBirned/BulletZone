/**
 * @author Dartagnan Birnie
 * Made to handle the greying out of buttons / enabling
 */
package edu.unh.cs.cs619.bulletzone;

import android.app.Activity;
import android.widget.Button;

public class ButtonController extends Activity {

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

    public ButtonController(Activity activity) {
        this.activity = activity;
    }

    public void initializeButtons() { // Always have tank active at first.
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

    public void updateButtons(int controllingBuilder) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (controllingBuilder == 1) { // Controlling builder, only show builder buttons
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
                    buildWall.setEnabled(false);
                    buildWall.setAlpha(0.5f);
                    buildRoad.setEnabled(false);
                    buildRoad.setAlpha(0.5f);
                    buildBridge.setEnabled(false);
                    buildBridge.setAlpha(0.5f);
                    dismantle.setEnabled(false);
                    dismantle.setAlpha(0.5f);
                    deploySoldier.setEnabled(true);
                    deploySoldier.setAlpha(1.0f);
                    controlBuilder.setEnabled(true);
                    controlBuilder.setAlpha(1.0f);
                    controlTank.setEnabled(false);
                    controlTank.setAlpha(0.5f);
                    dropHijack.setEnabled(true);
                    dropHijack.setAlpha(1.0f);
                    dropMine.setEnabled(true);
                    dropMine.setAlpha(1.0f);
                }
            }
        });
    }

}
