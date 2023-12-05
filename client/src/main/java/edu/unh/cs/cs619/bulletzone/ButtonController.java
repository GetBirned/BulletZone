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
                }
            }
        });
    }

}
