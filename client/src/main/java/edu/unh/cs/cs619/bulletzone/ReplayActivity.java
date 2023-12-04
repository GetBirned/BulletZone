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
    SeekBar seekBar;
    GridView replay_gv;
    protected ReplayGridAdapter replayAdapter;
    ReplayRunner replayRunner;
    ReplaySpeed replaySpeed;
    TextView speed_text;
    private Button play;
    private Button leave;
    private Button pause;
    boolean paused;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replay);

        replayRunner = new ReplayRunner(this);
        replaySpeed = new ReplaySpeed();

        // get intent
        Intent intent = getIntent();

        // TODO: Abstract into ReplaySpeed class (no boolean just 0 delay)
        paused = false;

        // initialize adapter
        replayAdapter = new ReplayGridAdapter(this);

        replay_gv = (GridView) findViewById(R.id.replayGV);
        replay_gv.setAdapter(replayAdapter);

        // set speed_text view
        speed_text = (TextView) findViewById(R.id.speed_text);
        // Initial speed_text value
        speed_text.setText("CURR SPEED 1x");

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

                replaySpeed.setSpeed(progress+1);
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

    private void animate() {
        Log.d("PAUSED", "animate called");
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (!paused) {
                    Log.d("PAUSED", "not paused");

                    int[][] currFrame = replayRunner.getCurrBoardState();
                    replayRunner.updateFrame();
                    replayAdapter.updateList(currFrame);
                    replayAdapter.notifyDataSetChanged();
                } else {
                    Log.d("PAUSED", "replay is paused");
                }
                // Reschedule the execution of this Runnable after a delay
                handler.postDelayed(this, replaySpeed.getSpeed());
            }
        };

        // Start the loop by posting the Runnable for the first time
        handler.postDelayed(runnable, 1000);
    }
}