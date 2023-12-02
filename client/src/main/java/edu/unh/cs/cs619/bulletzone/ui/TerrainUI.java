package edu.unh.cs.cs619.bulletzone.ui;

import android.widget.ImageView;

import edu.unh.cs.cs619.bulletzone.R;

public class TerrainUI {

    // 0 grass, // 1 thingamajig //2 nuke //3 apple
    //4 hill // 5 rocky // 6 forest

    //return a 4 5 or 6 based on what it should be? return 0 if a tank isnt there?
    //boolean if a tank has been there or not?

    public void friendlyTankImage(ImageView imageView, int direction, int val) {

        if (val == 4) {
            if (direction == 0) {
                imageView.setImageResource(R.drawable.friendlytankuphilly);
            } else if (direction == 2) {
                imageView.setImageResource(R.drawable.friendlytankrighthilly);
            } else if (direction == 4) {
                imageView.setImageResource(R.drawable.friendlytankdownhilly);
            } else if (direction == 6) {
                imageView.setImageResource(R.drawable.friendlytanklefthilly);
            }
        } else if (val == 5) {
            if (direction == 0) {
                imageView.setImageResource(R.drawable.friendlytankuprocky);
            } else if (direction == 2) {
                imageView.setImageResource(R.drawable.friendlytankrightrocky);
            } else if (direction == 4) {
                imageView.setImageResource(R.drawable.friendlytankdownrocky);
            } else if (direction == 6) {
                imageView.setImageResource(R.drawable.friendlytankleftrocky);
            }
        } else if (val == 6) {
            //NEED TO DO THIS!!!!!!!!!!!!!!!!!

        } else if (val == 11) {
            if (direction == 0) { // Bridge
                imageView.setImageResource(R.drawable.friendlytankbridgeup);
            } else if (direction == 2) {
                imageView.setImageResource(R.drawable.friendlytankbridgeright);
            } else if (direction == 4) {
                imageView.setImageResource(R.drawable.friendlytankbridgedown);
            } else if (direction == 6) {
                imageView.setImageResource(R.drawable.friendlytankbridgeleft);
            }
        } else if (val == 12) {
            if (direction == 0) { // Road
                imageView.setImageResource(R.drawable.friendlytankroadup);
            } else if (direction == 2) {
                imageView.setImageResource(R.drawable.friendlytankroadright);
            } else if (direction == 4) {
                imageView.setImageResource(R.drawable.friendlytankroaddown);
            } else if (direction == 6) {
                imageView.setImageResource(R.drawable.friendlytankroadleft);
            }
        } else {
            if (direction == 0) {
                imageView.setImageResource(R.drawable.friendlytankup);
            } else if (direction == 2) {
                imageView.setImageResource(R.drawable.friendlytankright);
            } else if (direction == 4) {
                imageView.setImageResource(R.drawable.friendlytankdown);
            } else if (direction == 6) {
                imageView.setImageResource(R.drawable.friendlytankleft);
            }
        }

    }

    public void soldierImage(ImageView imageView, int direction, int val) {
        if (val == 4) {
            if (direction == 0) {
                imageView.setImageResource(R.drawable.hillysoldierup);
            } else if (direction == 2) {
                imageView.setImageResource(R.drawable.hillysoldierright);
            } else if (direction == 4) {
                imageView.setImageResource(R.drawable.hillysoldierdown);
            } else if (direction == 6) {
                imageView.setImageResource(R.drawable.hillysoldierleft);
            }
        } else if (val == 5) {
            if (direction == 0) {
                imageView.setImageResource(R.drawable.rockysoldierup);
            } else if (direction == 2) {
                imageView.setImageResource(R.drawable.rockysoldierright);
            } else if (direction == 4) {
                imageView.setImageResource(R.drawable.rockysoldierdown);
            } else if (direction == 6) {
                imageView.setImageResource(R.drawable.rockysoldierleft);
            }
        } else if (val == 6) {
            if (direction == 0) {
                imageView.setImageResource(R.drawable.soldierforestup);
            } else if (direction == 2) {
                imageView.setImageResource(R.drawable.soldierforestright);
            } else if (direction == 4) {
                imageView.setImageResource(R.drawable.soldierforestdown);
            } else if (direction == 6) {
                imageView.setImageResource(R.drawable.soldierforestleft);
            }
        } else {
            if (direction == 0) {
                imageView.setImageResource(R.drawable.soldiergrassup);
            } else if (direction == 2) {
                imageView.setImageResource(R.drawable.soldiergrassright);
            } else if (direction == 4) {
                imageView.setImageResource(R.drawable.soldiergrassdown);
            } else if (direction == 6) {
                imageView.setImageResource(R.drawable.soldiergrassleft);
            }
        }

    }

    public void builderImage(ImageView imageView, int direction, int val) {
        if (val == 4) {
            if (direction == 0) { // Hilly
                imageView.setImageResource(R.drawable.hillybuilderup);
            } else if (direction == 2) {
                imageView.setImageResource(R.drawable.hillybuilderright);
            } else if (direction == 4) {
                imageView.setImageResource(R.drawable.hillybuilderdown);
            } else if (direction == 6) {
                imageView.setImageResource(R.drawable.hillybuilderleft);
            }
        } else if (val == 5) {
            if (direction == 0) { // Rocky
                imageView.setImageResource(R.drawable.rockybuilderup);
            } else if (direction == 2) {
                imageView.setImageResource(R.drawable.rockybuilderright);
            } else if (direction == 4) {
                imageView.setImageResource(R.drawable.rockybuilderdown);
            } else if (direction == 6) {
                imageView.setImageResource(R.drawable.rockybuilderleft);
            }
        } else if (val == 6) {
            if (direction == 0) { // Forest
                imageView.setImageResource(R.drawable.builderforestup);
            } else if (direction == 2) {
                imageView.setImageResource(R.drawable.builderforestright);
            } else if (direction == 4) {
                imageView.setImageResource(R.drawable.builderforestdown);
            } else if (direction == 6) {
                imageView.setImageResource(R.drawable.builderforestleft);
            }
        } else if (val == 11) {
            if (direction == 0) { // Bridge
                imageView.setImageResource(R.drawable.builderbridgeup);
            } else if (direction == 2) {
                imageView.setImageResource(R.drawable.builderbridgeright);
            } else if (direction == 4) {
                imageView.setImageResource(R.drawable.builderbridgedown);
            } else if (direction == 6) {
                imageView.setImageResource(R.drawable.builderbridgeleft);
            }
        } else if (val == 12) {
            if (direction == 0) { // Road
                imageView.setImageResource(R.drawable.builderroadup);
            } else if (direction == 2) {
                imageView.setImageResource(R.drawable.builderroadright);
            } else if (direction == 4) {
                imageView.setImageResource(R.drawable.builderroaddown);
            } else if (direction == 6) {
                imageView.setImageResource(R.drawable.builderroadleft);
            }
        } else {
            if (direction == 0) {
                imageView.setImageResource(R.drawable.buildergrassup);
            } else if (direction == 2) {
                imageView.setImageResource(R.drawable.buildergrassright);
            } else if (direction == 4) {
                imageView.setImageResource(R.drawable.buildergrassdown);
            } else if (direction == 6) {
                imageView.setImageResource(R.drawable.buildergrassleft);
            }
        }

    }

    public void enemyTankImage(ImageView imageView, int direction, int val) {
        if (val == 4) {
            if (direction == 0) {
                imageView.setImageResource(R.drawable.enemytankuphilly);
            } else if (direction == 2) {
                imageView.setImageResource(R.drawable.enemytankrighthilly);
            } else if (direction == 4) {
                imageView.setImageResource(R.drawable.enemytankdownhilly);
            } else if (direction == 6) {
                imageView.setImageResource(R.drawable.enemytanklefthilly);
            }
        } else if (val == 5) {
            if (direction == 0) {
                imageView.setImageResource(R.drawable.enemytankuprocky);
            } else if (direction == 2) {
                imageView.setImageResource(R.drawable.enemytankrightrocky);
            } else if (direction == 4) {
                imageView.setImageResource(R.drawable.enemytankdownrocky);
            } else if (direction == 6) {
                imageView.setImageResource(R.drawable.enemytankleftrocky);
            }
        } else if (val == 6) {
            //NEED TO DO THIS!!!!!!!!!!!!!!!!!


        } else if (val == 11) {
            if (direction == 0) { // Bridge
                imageView.setImageResource(R.drawable.enemytankbridgeup);
            } else if (direction == 2) {
                imageView.setImageResource(R.drawable.enemytankbridgeright);
            } else if (direction == 4) {
                imageView.setImageResource(R.drawable.enemytankbridgedown);
            } else if (direction == 6) {
                imageView.setImageResource(R.drawable.enemytankbridgeleft);
            }
        } else if (val == 12) {
            if (direction == 0) { // Road
                imageView.setImageResource(R.drawable.enemytankroadup);
            } else if (direction == 2) {
                imageView.setImageResource(R.drawable.enemytankroadright);
            } else if (direction == 4) {
                imageView.setImageResource(R.drawable.enemytankroaddown);
            } else if (direction == 6) {
                imageView.setImageResource(R.drawable.enemytankroadleft);
            }
        } else {
            if (direction == 0) {
                imageView.setImageResource(R.drawable.enemytankup);
            } else if (direction == 2) {
                imageView.setImageResource(R.drawable.enemytankright);
            } else if (direction == 4) {
                imageView.setImageResource(R.drawable.enemytankdown);
            } else if (direction == 6) {
                imageView.setImageResource(R.drawable.enemytankleft);
            }
        }

    }
}

