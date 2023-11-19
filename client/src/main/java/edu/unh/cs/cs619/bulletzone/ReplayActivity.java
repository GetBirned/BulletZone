package edu.unh.cs.cs619.bulletzone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;

import edu.unh.cs.cs619.bulletzone.ui.GridAdapter;
import edu.unh.cs.cs619.bulletzone.ui.ReplayGridAdapter;
//import edu.unh.cs.cs619.bulletzone.ui.ReplayGridAdapter;

public class ReplayActivity extends AppCompatActivity {
    ArrayList<int[][]> board_states;
    SeekBar seekBar;
    //int[][] board_state;
    GridView replay_gv;
    Context context;
    //ReplayGridAdapter replayAdapter;
    protected ReplayGridAdapter replayAdapter;
    TextView speed_text;
    private Button play;
    private Button leave;
    private Button pause;
    boolean paused;
    int frame;
    int delay_default;
    int delay_amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replay);

        // get intent
        Intent intent = getIntent();
        // initialize board_states
        //board_state = new int[16][16];

        board_states = new ArrayList<>();
        // initializing frame
        frame = 0;
        paused = false;
        // in ms
        delay_default = 4000;
        // in ms
        delay_amount = delay_default;
        // initialize adapter
        replayAdapter = new ReplayGridAdapter(this);

        replay_gv = (GridView) findViewById(R.id.replayGV);
        replay_gv.setAdapter(replayAdapter);

        // set speed_text view
        speed_text = (TextView) findViewById(R.id.speed_text);

        context = this;
        String filename = getReplayFile();
        if (filename == "BAD") {
            int[][] blank_board = new int[16][16];
            for (int i = 0; i < 16; i++) {
                for (int k = 0; k < 16; k++) {
                    blank_board[i][k] = 0;
                }
            }
            board_states.add(new int[16][16]);
        } else {
            speed_text.setText("CURR SPEED 1x");
            getReplayStates(filename);
        }

        play = (Button) findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start animation
                paused = false;
                animate();
            }
        });

        pause = (Button) findViewById(R.id.pause);
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paused = true;
            }
        });

        leave = (Button) findViewById(R.id.leave);
        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // ending activity
            }
        });

        seekBar = (SeekBar)findViewById(R.id.seekbar);

        // Get the progress value of the SeekBar
        // using setOnSeekBarChangeListener() method
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // progress 0-3 + 1 (1-4)
                // delay_default = 4000
                delay_amount = delay_default / (progress+1);
                // default delay is 4 == 1x 4000 / 4 ;; 4000 ( delay_amount)
                speed_text.setText("CURR SPEED " + String.valueOf(progress+1) + "x");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private Integer getMaxArrayList(ArrayList<Integer> nums) {
        Integer max_val = 0;
        for (Integer int_t : nums) {
            if (int_t > max_val) {
                max_val = int_t;
            }
        }
        return max_val;
    }

    private String getReplayFile() {
        String path = Environment.getExternalStorageDirectory().toString();
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);

        /*
        if file size is 0 -> error in ClientActivity onCreate
        if file size is 1 -> display only file
        if file size is 2-n -> display 2nd file (n-1)
         */

        if (files.length == 0) {
            // we have an error
            Log.d("Files", "no files found (error)");
            speed_text.setText("NO REPLAY FILE FOUND");
            return "BAD";
        } else if (files.length == 1) {
            Log.d("Files", "this is the first and only replay file found");
            return files[0].getName();
        } else {
            Log.d("Files", "multiple replay files found (good!)");
        }

        ArrayList<Integer> nums = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            Log.d("Files", "FileName:" + files[i].getName());
            try {
                Integer val = Integer.parseInt(files[i].getName());
                nums.add(val);
            } catch (NumberFormatException e) {
                System.out.println("Input String cannot be parsed to Integer.");
            }
        }

        // max val in arraylist
        Integer getMaxedValArrayList = getMaxArrayList(nums);
        nums.remove(getMaxedValArrayList);
        Integer second_max = getMaxArrayList(nums);
        String second_filename = second_max.toString();
        return second_filename;
    }

    private void getReplayStates(String filename) {
        getReplayFile();
        //String data = readFromFile(this);
        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(filename + ".txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString).append("\n");
                }

                inputStream.close();
                ret = stringBuilder.toString();
                //Log.d("RES", ret);
            }
        }
        catch (FileNotFoundException e) {
            Log.e("REPLAY FILE", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("REPLAY FILE", "Can not read file: " + e.toString());
        }

        String[] board_line_split = ret.split(" ");
        //Log.d("Array RET split", Arrays.toString(board_line_split));
        //Log.d("Array RET size", String.valueOf(ret.length()));
        //Log.d("ARR", Arrays.toString(board_line_split));

        int[][] board_state = new int[16][16];
        // NOTE: need to do length-1 as split includes newline char or something
        for (int i = 0; i < board_line_split.length - 1; i++) {
            int row = (i % (16*16)) / 16;
            int col = i % 16;
            //Log.d("VAL", board_line_split[i]);
            board_state[row][col] = Integer.parseInt(board_line_split[i]);
            String logging_thang = "";
            for (int[] a : board_state) {
                for (int b : a) {
                    logging_thang += b;
                }
            }
            if (i % (16*16-1) == 0) {
                // we have reached the last val for the frame so add
                board_states.add(board_state);
                board_state = new int[16][16];
            }
            //Log.d("VAL VAL", logging_thang);
        }
        Log.d("FRAME NUM", String.valueOf(board_states.size()));
    }

    private void animate() {
        Log.d("PAUSED", "animate called");
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (!paused) {
                    Log.d("PAUSED", "not paused");
                    if (frame < board_states.size()) {
                        Log.d("FRAME", String.valueOf(frame));
                        int[][] frame_t = board_states.get(frame);
                        for (int[] tt : frame_t) {
                            String lineline = "";
                            for (int ttt : tt) {
                                lineline += ttt + " ";
                            }
                            Log.d("SSA", lineline);
                        }
                        replayAdapter.updateList(board_states.get(frame));
                        frame++;
                        replayAdapter.notifyDataSetChanged();
                    } else {
                        // If we reached the end of the list, reset frame to 0
                        frame = 0;
                        return;
                    }
                } else {
                    Log.d("PAUSED", "replay is paused");
                }
                // Reschedule the execution of this Runnable after a delay
                handler.postDelayed(this, delay_amount);
            }
        };

        // Start the loop by posting the Runnable for the first time
        handler.postDelayed(runnable, 1000);
    }
}