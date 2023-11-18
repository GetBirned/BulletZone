package edu.unh.cs.cs619.bulletzone.ui;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Random;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.rest.spring.annotations.RestService;

import java.util.Random;

import edu.unh.cs.cs619.bulletzone.R;
import edu.unh.cs.cs619.bulletzone.rest.BulletZoneRestClient;

@EBean
public class GridAdapter extends BaseAdapter {

    private final Object monitor = new Object();
    @SystemService
    protected LayoutInflater inflater;
    private int[][] mEntities = new int[16][16];
    private int[][] oldMEntities = new int[16][16];
    Context context;
    Random random = new Random();

    @RestService
    BulletZoneRestClient restClient;
    private static final int[] ITEM_RESOURCES = {
            R.drawable.applepowerupgrass,
            R.drawable.nukepowerupgrass,
            R.drawable.coingrass
    };
    public void setRestClient(BulletZoneRestClient restClient) {
        this.restClient = restClient;
    }

    public void updateList(int[][] entities) {
        synchronized (monitor) {
            this.mEntities = entities;
            this.notifyDataSetChanged();
        }
    }

    private int currentItemIndex = 0;

    int tankRow;
    int tankCol;
    public int flag = 0;
    public int numCoins = 1000;
    private static final String TAGFRIEND = "GridAdapter (Friendly):";
    private static final String TAGENEMY = "GridAdapter (Enemy):";

    @Override
    public int getCount() {
        return 16 * 16;
    }

    @Override
    public Object getItem(int position) {
        return mEntities[(int) position / 16][position % 16];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int friendlyTank(int value) {
        String tankID = Integer.toString(value);
        tankID = tankID.substring(2, 4);
        return Integer.parseInt(tankID);
    }

    int lastFriendlyDirection; // keeps record of last friendly direction
    int lastEnemyDirection; // keeps record of last enemy direction
    int numItems;
    int numPlayers;
    double chance;

    private int[][] hasPowerUp = new int[16][16];
    // 0 grass, // 1 thingamajig //2 nuke //3 apple
    //4 hill // 5 rocky // 6 forest // 7 soldier

    public void setFriendlyTank(ImageView imageView, int direction, int val) {

        lastFriendlyDirection = direction;
        TerrainUI t = new TerrainUI();
        t.friendlyTankImage(imageView, direction, val);
    }

    public void setEnemyTank(ImageView imageView, int direction, int val) {
        lastEnemyDirection = direction;
        TerrainUI t = new TerrainUI();
        t.enemyTankImage(imageView, direction, val);
    }

    public void setSoldier(ImageView imageView, int direction, int val) {
        TerrainUI t = new TerrainUI();
        t.soldierImage(imageView, direction, val);
    }

    public void addSoldier(long soldierId) {
        mEntities[tankRow + 1][tankCol] = (int) soldierId;
        notifyDataSetChanged();
    }

    public void getContext(Context context) {
        this.context = context;
    }

    private boolean isChanged(int[][] grid) {
        for (int i = 0; i < 16; i++) {
            for (int k = 0; k < 16; k++) {
                if (mEntities[i][k] != oldMEntities[i][k]) {
                    //Log.d("STATE CHANGE", "yes");
                    return true;
                }
            }
        }
        return false;
    }
    private void writeToFile() {
        // NEED TO ADD CHECK to see if mendities has changed

        if (isChanged(mEntities)) {
            oldMEntities = mEntities;
            try {
                String grid_string = "";
                for (int[] nested_arr : mEntities) {
                    for (int val : nested_arr) {
                        grid_string += String.valueOf(val) + " ";
                    }
                }
                //String result = ts + " " + grid_string;
                String result = grid_string;
                //Log.d("GRID STRING", grid_string);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("replay_file.txt", Context.MODE_APPEND));
                outputStreamWriter.write(result);
                outputStreamWriter.close();
                //Log.d("FILE APPEND", result);
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        }
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        flag = 0;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.field_item, null);
        }

        ImageView imageView = convertView.findViewById(R.id.imageView);

        int row = position / 16;
        int col = position % 16;

        int val = mEntities[row][col];

        synchronized (monitor) {
            if(hasPowerUp[row][col] == 4) {
                imageView.setImageResource(R.drawable.hillyterrain);
            } else if(hasPowerUp[row][col] == 5) {
                imageView.setImageResource(R.drawable.rockyterrain);
            }
            if (val > 0) {

                int direction = (val % 10);
                if (val == 1000 || (val > 1000 && val <= 2000)) {
                    imageView.setImageResource(R.drawable.brick); // Set the appropriate image resource for walls
                } else if (val >= 2000000 && val <= 3000000) {
                    imageView.setImageResource(R.drawable.bulletgrass);
                } else if (val >= 10000000 && val <= 20000000) {
                    if (hasPowerUp[row][col] == 1 || hasPowerUp[row][col] == 2 || hasPowerUp[row][col] == 3) {
                        if(hasPowerUp[row][col] == 1){
                            int rand = random.nextInt(196) + 5;
                            numCoins += rand;
                            Log.d("NUMCOINS:", this.numCoins+"");
                        }
                        else if(hasPowerUp[row][col] != 1){
                            int finalType = hasPowerUp[row][col];
                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... voids) {
                                    restClient.setTankPowerup(friendlyTank(val), finalType, true);
                                    Log.e("Sending " + friendlyTank(val) + " toRestClient", "withVal: " + finalType);

                                    return null;
                                }
                            }.execute();
                        }
                        hasPowerUp[row][col] = 0;
                        numItems--;
                    }
                    numPlayers++;
                    if (friendlyTank(val) == 0) {
                        setFriendlyTank(imageView, direction, hasPowerUp[row][col]); // Set proper friendly tank image
                    } else {
                        tankRow = row;
                        tankCol = col;
                        setEnemyTank(imageView, direction, hasPowerUp[row][col]); // Set proper enemy tank image
                    }
                } else if (val >= 40000000 && val <= 50000000) {
                    setSoldier(imageView, direction, hasPowerUp[row][col]);
                    if (hasPowerUp[row][col] == 1 || hasPowerUp[row][col] == 2 || hasPowerUp[row][col] == 3) {
                        if(hasPowerUp[row][col] == 1){
                            int rand = random.nextInt(196) + 5;
                            numCoins += rand;
                            Log.d("NUMCOINS:", this.numCoins+"");
                        }
                        else if(hasPowerUp[row][col] != 1){
                            int finalType = hasPowerUp[row][col];
                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... voids) {
                                    restClient.setTankPowerup(friendlyTank(val), finalType, false);
                                    Log.e("Solder #: " + friendlyTank(val) +  "toRestClient", "withVal: " + finalType);

                                    return null;
                                }
                            }.execute();
                        }
                        hasPowerUp[row][col] = 0;
                        numItems--;
                    }
                } else if (val == 7) {
                    hasPowerUp[row][col] = 1;
                    numItems++;
                    imageView.setImageResource(R.drawable.coingrass);
                } else if (val == 2002) {
                    hasPowerUp[row][col] = 2;
                    numItems++;
                    imageView.setImageResource(R.drawable.nukepowerupgrass);
                } else if (val == 2003) {
                    hasPowerUp[row][col] = 3;
                    numItems++;
                    imageView.setImageResource(R.drawable.applepowerupgrass);
                } else if (val == 2) {
                    hasPowerUp[row][col] = 4;
                    imageView.setImageResource(R.drawable.hillyterrain);
                } else if (val == 1) {
                    hasPowerUp[row][col] = 5;
                    imageView.setImageResource(R.drawable.rockyterrain);
                } else if (val == 3) {
                    hasPowerUp[row][col] = 6;
                    imageView.setImageResource(R.drawable.forestterrain);
                }
            } else {

                if (hasPowerUp[row][col] != 0) {
                    if (hasPowerUp[row][col] == 1) {
                        numItems++;
                        imageView.setImageResource(R.drawable.coingrass);
                    } else if (hasPowerUp[row][col] == 2) {
                        numItems++;
                        imageView.setImageResource(R.drawable.nukepowerupgrass);
                    } else if (hasPowerUp[row][col] == 3) {
                        numItems++;
                        imageView.setImageResource(R.drawable.applepowerupgrass);
                    }

                } else {
                    if(0.25 * (numPlayers / (numItems + 1)) > 0) {
                        // Determine whether to place a power-up
                        if (shouldPlacePowerUp()) {
                            int appear = new Random().nextInt(3);
                            hasPowerUp[row][col] = appear + 1;
                            numItems++;
                            setImageForPowerUp(imageView, hasPowerUp[row][col]);
                        } //else {
                        //imageView.setImageResource(R.drawable.grass);
                        //}
                    } else {

                        imageView.setImageResource(R.drawable.grass);
                    }
                }
            }

        }
        writeToFile();

        return imageView;
    }

    private boolean shouldPlacePowerUp() {
        int randNum = new Random().nextInt(101);
        return randNum <= (chance * 100);
    }

    // Set the image for the power-up based on its type
    private void setImageForPowerUp(ImageView imageView, int powerUpType) {
        switch (powerUpType) {
            case 1:
                imageView.setImageResource(R.drawable.coingrass);
                break;
            case 2:
                imageView.setImageResource(R.drawable.nukepowerupgrass);
                break;
            case 3:
                imageView.setImageResource(R.drawable.applepowerupgrass);
                break;
        }
    }
}