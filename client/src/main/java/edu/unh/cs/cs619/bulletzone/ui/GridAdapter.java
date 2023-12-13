package edu.unh.cs.cs619.bulletzone.ui;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.rest.spring.annotations.RestService;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Random;

import edu.unh.cs.cs619.bulletzone.ClientController;
import edu.unh.cs.cs619.bulletzone.R;
import edu.unh.cs.cs619.bulletzone.rest.BulletZoneRestClient;

@EBean
public class GridAdapter extends BaseAdapter {

    private final Object monitor = new Object();
    @SystemService
    protected LayoutInflater inflater;
    private static int[][] mEntities = new int[16][16];
    private int[][] oldMEntities = new int[16][16];
    Context context;
    String ts;
    String username;
    Random random = new Random();

    public boolean didEject = false;
    public int ejectedType = 0;
    public int convert(int val){
        if (val == 2) {
            return 2002;
        } else if (val == 3) {
            return 2003;
        }else if (val == 9) {
            return 3131;
        } else if(val == 10){
            return 3141;
        }
        return -1;
    }

    @RestService
    BulletZoneRestClient restClient;

    private ClientController controller;

    public void setClientController(ClientController clientController) {
        this.controller = clientController;
    }
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
        tankID = tankID.substring(1, 4);
        //Log.d("Sending", tankID);
        return Integer.parseInt(tankID);
    }

    int lastFriendlyDirection; // keeps record of last friendly direction
    int lastEnemyDirection; // keeps record of last enemy direction
    int numItems;
    int numPlayers;
    double chance;

    private static int[][] hasPowerUp = new int[16][16];
    // 0 grass, // 1 thingamajig //2 nuke //3 apple
    //4 hill // 5 rocky // 6 forest // 7 soldier // 8 water
    //9 deflector //10 repair kit // 11 bridge // 12 road

    static boolean isValidIndex(int row, int col) {
        return row >= 0 && row < 16 && col >= 0 && col < 16 && mEntities[row][col] == 0 && hasPowerUp[row][col] == 0;
    }
    public void ejectPowerup(int row, int col) {
        //FIND SOLDIER'S/BUILDER'S POSITION
        if (convert(ejectedType) != -1) {
            int[][] offsets = {
                    {1, 1}, {1, 0}, {1, -1},
                    {0, 1},          {0, -1},
                    {-1, 1}, {-1, 0}, {-1, -1}
            };
            for (int[] offset : offsets) {
                int newRow = row + offset[0];
                int newCol = col + offset[1];


                if (isValidIndex(newRow, newCol)) {
                    mEntities[newRow][newCol] = convert(ejectedType);
                    hasPowerUp[newRow][newCol] = ejectedType;
                    ejectedType = 0;
                    didEject = false;
                    break;
                }
            }
        }
    }

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

    public void setBuilder(ImageView imageView, int direction, int val) {
        TerrainUI t = new TerrainUI();
        t.builderImage(imageView, direction, val);
    }


    public void getContext(Context context) {
        this.context = context;
    }

    public void setTs(String ts) {
        this.ts = ts;
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
        if (isChanged(mEntities)) {
            oldMEntities = mEntities;
            try {
                String grid_string = "";
                int i = 0;
                for (int[] nested_arr : mEntities) {
                    int k = 0;
                    for (int val : nested_arr) {
                        /*
                            check against powerup thing

                        if (hasPowerUp[i][k] != 0) {
                            grid_string += String.valueOf(hasPowerUp[i][k]) + " ";
                        } else {
                            grid_string += String.valueOf(val) + " ";
                        }
                        grid_string += String.valueOf(val) + " ";
                        k++;
                         */
                        grid_string += String.valueOf(val) + " ";
                    }
                    i++;
                }
                //String result = ts + " " + grid_string;
                String result = grid_string;
                //Log.d("GRID STRING", grid_string);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(ts + ".txt", Context.MODE_APPEND));
                outputStreamWriter.write(result);
                outputStreamWriter.close();
                //Log.d("FILE APPEND", result);
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getTankIDFromFile() {
        try {
            InputStream inputStream = context.openFileInput(username + ".txt");

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
            if (hasPowerUp[row][col] == 4) {
                mEntities[row][col] = 2;
            } else if (hasPowerUp[row][col] == 5) {
                mEntities[row][col] = 1;
            } else if (hasPowerUp[row][col] == 6) {
                mEntities[row][col] = 3;
            } else if( hasPowerUp[row][col] == 8){
                mEntities[row][col] = 50;
            }
            if(hasPowerUp[row][col] == 4) {
                imageView.setImageResource(R.drawable.hillyterrain);


            } else if(hasPowerUp[row][col] == 5) {
                imageView.setImageResource(R.drawable.rockyterrain);


            } else if (hasPowerUp[row][col] == 6) {
                imageView.setImageResource(R.drawable.forestterrain);
            } else if (hasPowerUp[row][col] == 8) {
                imageView.setImageResource(R.drawable.water);
            } else if (hasPowerUp[row][col] == 11) {
                imageView.setImageResource(R.drawable.waterwithbridge);
            } else if (hasPowerUp[row][col] == 12) {
                imageView.setImageResource(R.drawable.roadongrass);
            } else if (hasPowerUp[row][col] == 13 || hasPowerUp[row][col] == 15) {
                imageView.setImageResource(R.drawable.grass);
            } else if (hasPowerUp[row][col] == 14 || hasPowerUp[row][col] == 16) {
                imageView.setImageResource(R.drawable.hillyterrain);
            }
            if (val > 0) {
                int direction = (val % 10);
                if (val == 1000 || (val > 1000 && val <= 2000)) {
                    imageView.setImageResource(R.drawable.brick); // Set the appropriate image resource for walls
                } else if (val >= 2000000 && val <= 3000000) {
                    if (hasPowerUp[row][col] == 4) {
                        imageView.setImageResource(R.drawable.bullethilly);
                    } else if(hasPowerUp[row][col] == 5) {
                        imageView.setImageResource(R.drawable.bulletrocky);
                    } else if (hasPowerUp[row][col] == 8) {
                        imageView.setImageResource(R.drawable.bulletwater);
                    } else if (hasPowerUp[row][col] == 11) {
                        imageView.setImageResource(R.drawable.bulletbridge);
                    } else if (hasPowerUp[row][col] == 12) {
                        imageView.setImageResource(R.drawable.bulletroad);
                    } else {
                        imageView.setImageResource(R.drawable.bulletgrass);
                    }
                } else if (val >= 10000000 && val <= 20000000) {
                    if(didEject) {
                        if(convert(ejectedType) != 3141) {
                            ejectPowerup(row, col);
                        }
                    }
                    if (hasPowerUp[row][col] == 1 || hasPowerUp[row][col] == 2 || hasPowerUp[row][col] == 3
                            || hasPowerUp[row][col] == 9 || hasPowerUp[row][col] == 10) {
                        if(hasPowerUp[row][col] == 1){

                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... voids) {
                                    Log.e("Sending money toRest", " Sending money toRest withVal: " + username);
                                    System.out.println(" Sending money toRest withVal: " + username);
                                    int rand = random.nextInt(196) + 5;
                                    controller.updateBalance(username, rand );


                                    return null;
                                }
                            }.execute();
                        }
                        else if(hasPowerUp[row][col] != 1){
                            Log.e("TAG", "MAKING IT TO THE COIN");
                            int finalType = hasPowerUp[row][col];
                            int finalVal = val;
                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... voids) {
                                    controller.setTankPowerup(friendlyTank(finalVal), finalType, 't');
                                    Log.e("Sending " + friendlyTank(finalVal) + " toRestClient", "withVal: " + finalType);


                                    return null;
                                }
                            }.execute();
                        }
                        mEntities[row][col] = 0;
                        hasPowerUp[row][col] = 0;
                        numItems--;
                    }
                    numPlayers++;
                    // TODO: need to discern between friendly tank
                    // problem is friendlyTank(val) isn't right value (from file works)
                    Log.d("ID THANG", "ft " + friendlyTank(val) + ": from file " + getTankIDFromFile());
                    if (friendlyTank(val) == getTankIDFromFile()) {
                        setFriendlyTank(imageView, direction, hasPowerUp[row][col]); // Set proper friendly tank image
                    } else {
                        tankRow = row;
                        tankCol = col;
                        setEnemyTank(imageView, direction, hasPowerUp[row][col]); // Set proper enemy tank image
                    }
                } else if (val >= 40000000 && val <= 50000000) {
                    setSoldier(imageView, direction, hasPowerUp[row][col]);
                    if(didEject) {
                        if(convert(ejectedType) != 3141) {
                            ejectPowerup(row, col);
                        }
                    }
                    if (hasPowerUp[row][col] == 1 || hasPowerUp[row][col] == 2 || hasPowerUp[row][col] == 3
                            || hasPowerUp[row][col] == 9 || hasPowerUp[row][col] == 10) {
                        if(hasPowerUp[row][col] == 1){
                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... voids) {
                                    Log.e("Sending money toRest", " Sending money toRest withVal: " + username);
                                    System.out.println(" Sending money toRest withVal: " + username);
                                    int rand = random.nextInt(196) + 5;
                                    controller.updateBalance(username, rand );


                                    return null;
                                }
                            }.execute();
                        }
                        else if(hasPowerUp[row][col] != 1){
                            int finalType = hasPowerUp[row][col];
                            int finalVal1 = val;
                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... voids) {
                                    controller.setTankPowerup(friendlyTank(finalVal1), finalType, 's');
                                    Log.e("Solder #: " + friendlyTank(finalVal1) +  "toRestClient", "withVal: " + finalType);

                                    return null;
                                }
                            }.execute();
                        }
                        mEntities[row][col] = 0;
                        hasPowerUp[row][col] = 0;
                        numItems--;
                    }
                } else if (val >= 50000000 && val <= 60000000) {
                    setBuilder(imageView, direction, hasPowerUp[row][col]);
                    if(didEject) {
                        if(convert(ejectedType) != 3141) {
                            ejectPowerup(row, col);
                        }
                    }
                    if (hasPowerUp[row][col] == 1 || hasPowerUp[row][col] == 2 || hasPowerUp[row][col] == 3
                            || hasPowerUp[row][col] == 9 || hasPowerUp[row][col] == 10) {
                        if(hasPowerUp[row][col] == 1){
                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... voids) {
                                    Log.e("Sending money toRest", " Sending money toRest withVal: " + username);
                                    System.out.println(" Sending money toRest withVal: " + username);
                                    int rand = random.nextInt(196) + 5;
                                    controller.updateBalance(username, rand );


                                    return null;
                                }
                            }.execute();
                        }
                        else if(hasPowerUp[row][col] != 1){
                            int finalType = hasPowerUp[row][col];
                            int finalVal1 = val;
                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... voids) {
                                    controller.setTankPowerup(friendlyTank(finalVal1), finalType, 'b');
                                    Log.e("Solder #: " + friendlyTank(finalVal1) +  "toRestClient", "withVal: " + finalType);

                                    return null;
                                }
                            }.execute();
                        }
                        mEntities[row][col] = 0;
                        hasPowerUp[row][col] = 0;
                        numItems--;
                    }
                } else if (val == 7) {
                    hasPowerUp[row][col] = 1;
                    //numItems++;
                    imageView.setImageResource(R.drawable.coingrass);
                } else if (val == 2002) {
                    hasPowerUp[row][col] = 2;
                    // numItems++;
                    imageView.setImageResource(R.drawable.nukepowerupgrass);
                } else if (val == 2003) {
                    hasPowerUp[row][col] = 3;
                    ///numItems++;
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
                } else if (val == 50) {
                    hasPowerUp[row][col] = 8;
                    imageView.setImageResource(R.drawable.water);
                } else if (val == 3131) {
                    //numItems++;
                    hasPowerUp[row][col] = 9;
                    imageView.setImageResource(R.drawable.shieldgrass);
                } else if (val == 3141) {
                    // numItems++;
                    hasPowerUp[row][col] = 10;
                    imageView.setImageResource(R.drawable.toolsgrass);
                } else if (val == 60) {
                    hasPowerUp[row][col] = 11;
                    imageView.setImageResource(R.drawable.waterwithbridge);
                } else if (val == 70) {
                    hasPowerUp[row][col] = 12;
                    imageView.setImageResource(R.drawable.roadongrass);
                } else {
                    String val_str_base = String.valueOf(val);
                    // Getting trap type
                    String val_str = val_str_base.substring(0, val_str_base.length()-1);
                    // Stripping the tankID last num off of val
                    String val_tankid = val_str_base.substring(val_str_base.length()-1, val_str_base.length());

                    Log.d("TRAP", val_str + " : " + val_tankid);
                    if (val_str.equals("83030")) {
                        Log.d("TRAP", "mine recieved gridadapter");
                        if (Integer.parseInt(val_tankid) == getTankIDFromFile()) {
                            if (hasPowerUp[row][col] == 0 || hasPowerUp[row][col] == 13) {
                                imageView.setImageResource(R.drawable.minegrass);
                                hasPowerUp[row][col] = 13; // TODO: figure out why
                            } else if (hasPowerUp[row][col] == 4 || hasPowerUp[row][col] == 14) {
                                Log.d("TRAP", "was on hill");
                                imageView.setImageResource(R.drawable.minehilly);
                                hasPowerUp[row][col] = 14; // TODO: figure out why
                            }
                        } else {
                            Log.d("TRAP", "not a mine placed by user");
                        }
                    } else if (val_str.equals("2345")) {
                        Log.d("TRAP", "hijack trap recieved gridadapter");
                        if (Integer.parseInt(val_tankid) == getTankIDFromFile()) {
                            if (hasPowerUp[row][col] == 0 || hasPowerUp[row][col] == 15) {
                                imageView.setImageResource(R.drawable.hijackgrass);
                                hasPowerUp[row][col] = 15; // TODO: figure out why
                            } else if (hasPowerUp[row][col] == 4 || hasPowerUp[row][col] == 16) {
                                Log.d("TRAP", "was on hill");
                                imageView.setImageResource(R.drawable.hijackhilly);
                                hasPowerUp[row][col] = 16; // TODO: figure out why
                            }
                        } else {
                            Log.d("TRAP", "not a hijack trap placed by user");
                        }
                    }
                }
            } else {
                if(val == 0) {
                    imageView.setImageResource(R.drawable.grass);
                }

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
                    }else if (hasPowerUp[row][col] == 9) {
                        numItems++;
                        imageView.setImageResource(R.drawable.shieldgrass);
                    } else if (hasPowerUp[row][col] == 10) {
                        numItems++;
                        imageView.setImageResource(R.drawable.toolsgrass);
                    }
                } else {
                    /**
                    if(0.25 * (numPlayers / (numItems + 1)) > 0) {
                        // Determine whether to place a power-up
                        if (shouldPlacePowerUp()) {
                            int appear = new Random().nextInt(5);
                            hasPowerUp[row][col] = appear + 1;
                            //powerups should have been vals 1 2 3 4 5 but theyre not and its too
                            //late to change so for shield and health kit i changed them to their
                            //respective vals
                            if(hasPowerUp[row][col] == 4) {
                                hasPowerUp[row][col] =9;
                            } else if(hasPowerUp[row][col] == 5) {
                                hasPowerUp[row][col] = 10;
                            }
                            numItems++;
                            //setImageForPowerUp(imageView, hasPowerUp[row][col]);
                        }
                    } else {
                        imageView.setImageResource(R.drawable.grass);
                    }
                     **/
                }
            }
            if(hasPowerUp[row][col] == 4) {
                mEntities[row][col] = 2;
            } else if(hasPowerUp[row][col] == 5) {
                mEntities[row][col] = 1;


            } else if (hasPowerUp[row][col] == 6) {
                mEntities[row][col] = 3;
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
            case 4:
                imageView.setImageResource(R.drawable.shieldgrass);
                break;
            case 5:
                imageView.setImageResource(R.drawable.toolsgrass);
                break;
            default:
                imageView.setImageResource(R.drawable.grass);
        }
    }
}