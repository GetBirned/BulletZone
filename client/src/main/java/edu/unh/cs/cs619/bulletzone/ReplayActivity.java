package edu.unh.cs.cs619.bulletzone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;

import java.util.ArrayList;

import edu.unh.cs.cs619.bulletzone.ui.GridAdapter;
//import edu.unh.cs.cs619.bulletzone.ui.ReplayGridAdapter;

public class ReplayActivity extends AppCompatActivity {
    ArrayList<int[][]> board_states;
    int[][] board_state;
    GridView replay_gv;
    //ReplayGridAdapter replayAdapter;
    GridAdapter replayAdapter;
    private Button play;
    private Button leave;
    int frame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replay);

        // get intent
        Intent intent = getIntent();
        // initialize board_states
        board_state = new int[16][16];
        // intializing frame
        frame = 0;
        // initialize adapter
        //replayAdapter = new ReplayGridAdapter(this, board_state);
        replay_gv = (GridView) findViewById(R.id.replayGV);
        replay_gv.setAdapter(replayAdapter);

        play = (Button) findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start animation
                animate();
            }
        });

        leave = (Button) findViewById(R.id.leave);
        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // destroy? activity
            }
        });

    }

    private void getReplayJSON() {
        // this gets the json from the text file
        /*
        for (JSON obj : loaded_JSON) {
            do something & add to arraylist
         }
         */
    }

    private void animate() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // UPDATE GRIDVIEW
                //replayAdapter.update(board_states.get(frame));
                replayAdapter.updateList(board_states.get(frame));

                if (frame < board_states.size()) {
                    frame++;
                } else {
                    // If we reached the end of the list, reset frame to 0
                    frame = 0;
                }
            }
        }, 5000);
    }

    private void cleanup() {
        // this will erase the previous replay?
    }
}