package edu.unh.cs.cs619.bulletzone;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ReplayRunner {

    ArrayList<int[][]> board_states;
    Context context;

    int frame;

    public ReplayRunner(Context context) {
        board_states = new ArrayList<>();
        this.context = context;

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
            getReplayStates(filename);
        }
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

    public String getReplayFile() {
        //String path = Environment.getExternalStorageDirectory().toString();
        //String path = context.getFilesDir();
        //Log.d("Files", "Path: " + path);
        //File directory = new File(path);
        File directory = context.getFilesDir();
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
            // Removing below line for now
            //speed_text.setText("NO REPLAY FILE FOUND");
            return "BAD";
        } else if (files.length == 1) {
            Log.d("Files", "this is the first and only replay file found");
            return files[0].getName();
        } else {
            Log.d("Files", "multiple replay files found (good!)");
        }

        ArrayList<Integer> nums = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            String file_no_extension = files[i].getName().substring(0, files[i].getName().length() - 4);
            Log.d("Files", "FileName:" + files[i].getName() + " -> " + file_no_extension);
            try {
                Integer val = Integer.parseInt(file_no_extension);
                nums.add(val);
            } catch (NumberFormatException e) {
                System.out.println("Input String cannot be parsed to Integer.");
            }
        }

        // max val in arraylist
        //Integer getMaxedValArrayList = getMaxArrayList(nums);
        while (checkNoMovesFile(nums.get(nums.size()-1))) {
            // checkNoMovesFile returns true if the curr val is one move board
            nums.remove(nums.size()-1);
        }
        //Integer second_max = getMaxArrayList(nums);
        //String second_filename = second_max.toString();
        //Log.d("Files", "Found filename is " + second_filename);
        int last_filename = nums.get(nums.size()-1);
        Log.d("Files", "Found filename is " + last_filename);
        return String.valueOf(last_filename);
    }

    public boolean checkNoMovesFile(int num_arr_list) {
        /*
        This function will check if a file only has one state in it and remove it from nums if it does

         */
        String ret = "";

        String filename = String.valueOf(num_arr_list);

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
            }
        }
        catch (FileNotFoundException e) {
            Log.e("REPLAY FILE", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("REPLAY FILE", "Can not read file: " + e.toString());
        }

        String[] board_line_split = ret.split(" ");
        Log.d("Files", "file " + num_arr_list + " has a size of " + board_line_split.length);
        // includes null character ig so +1?
        if (board_line_split.length <= 16*16+1) {
            return true;
        } else {
            return false;
        }
    }

    public void getReplayStates(String filename) {
        getReplayFile();
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
            }
        }
        catch (FileNotFoundException e) {
            Log.e("REPLAY FILE", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("REPLAY FILE", "Can not read file: " + e.toString());
        }

        String[] board_line_split = ret.split(" ");

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
        // remove first board_state (always blank)
        board_states.remove(0);
        Log.d("FRAME NUM", String.valueOf(board_states.size()));
    }

    public void updateFrame() {
        frame++;
    }

    public int[][] getCurrBoardState() {
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
        } else {
            // If we reached the end of the list, reset frame to 0
            frame = 0;
        }
        return board_states.get(frame);
    }
}