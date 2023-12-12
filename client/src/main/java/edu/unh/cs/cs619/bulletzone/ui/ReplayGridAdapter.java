package edu.unh.cs.cs619.bulletzone.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import edu.unh.cs.cs619.bulletzone.R;

public class ReplayGridAdapter extends BaseAdapter {
    private Context context;
    public int[][] board; // Your 2D int array

    public ReplayGridAdapter(Context context) {
        this.context = context;
        this.board = new int[16][16];
    }

    @Override
    public int getCount() {
        return board.length * board[0].length;
    }

    @Override
    public Object getItem(int position) {
        int row = position / board[0].length;
        int col = position % board[0].length;
        return board[row][col];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void updateList(int[][] board) {
        this.board = board;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        if (convertView == null) {
            // If the view is not recycled, create a new TextView
            imageView = new ImageView(context);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(100, 100)); // Adjust the size as needed
        } else {
            imageView = (ImageView) convertView;
        }

        int row = position / board[0].length;
        int col = position % board[0].length;
        int cellValue = board[row][col];

        int mill_scale = 1000000;
        if (cellValue == 0) {
            imageView.setImageResource(R.drawable.grass);
        } else if (cellValue == 1) {
            // CHANGE
            imageView.setImageResource(R.drawable.rockyterrain);
        } else if (cellValue == 2) {
            imageView.setImageResource(R.drawable.hillyterrain);
        } else if (cellValue == 3) {
            imageView.setImageResource(R.drawable.forestterrain);
        } else if (cellValue == 2003) {
            imageView.setImageResource(R.drawable.nukepowerupgrass);
        } else if (cellValue == 2002) {
            imageView.setImageResource(R.drawable.applepowerupgrass);
        } else if (cellValue == 7) {
            imageView.setImageResource(R.drawable.coingrass);
        } else if (cellValue == 50) {
            imageView.setImageResource(R.drawable.water);
        } else if (cellValue == 3131) {
            imageView.setImageResource(R.drawable.shieldgrass);
        } else if (cellValue == 3141) {
            imageView.setImageResource(R.drawable.toolsgrass);
        } else if (cellValue == 60) {
            imageView.setImageResource(R.drawable.waterwithbridge);
        } else if (cellValue == 1000 || (cellValue > 1000 && cellValue <= 2000)) {
            imageView.setImageResource(R.drawable.brick);
        } else if (cellValue >= 2 * mill_scale && cellValue < 3 * mill_scale) {
            imageView.setImageResource(R.drawable.bulletgrass);
        } else if (cellValue >= 10 * mill_scale && cellValue < 20 * mill_scale) {
            // we have a tank
            switch (cellValue % 10) {
                case 0:
                    imageView.setImageResource(R.drawable.friendlytankup);
                    break;
                case 2:
                    imageView.setImageResource(R.drawable.friendlytankright);
                    break;
                case 4:
                    imageView.setImageResource(R.drawable.friendlytankdown);
                    break;
                case 6:
                    imageView.setImageResource(R.drawable.friendlytankleft);
                    break;
            }
        } else if (cellValue >= 40 * mill_scale && cellValue <= 50 * mill_scale) {
            // we have a tank
            switch (cellValue % 10) {
                case 0:
                    imageView.setImageResource(R.drawable.soldiergrassup);
                    break;
                case 2:
                    imageView.setImageResource(R.drawable.soldiergrassright);
                    break;
                case 4:
                    imageView.setImageResource(R.drawable.soldiergrassdown);
                    break;
                case 6:
                    imageView.setImageResource(R.drawable.soldiergrassleft);
                    break;
            }
        } else if (cellValue >= 50 * mill_scale && cellValue < 60 * mill_scale) {
            switch (cellValue % 10) {
                case 0:
                    imageView.setImageResource(R.drawable.buildergrassup);
                    break;
                case 2:
                    imageView.setImageResource(R.drawable.buildergrassleft);
                    break;
                case 4:
                    imageView.setImageResource(R.drawable.buildergrassdown);
                    break;
                case 6:
                    imageView.setImageResource(R.drawable.buildergrassright);
                    break;
            }
        } else if (cellValue == 60) {
            imageView.setImageResource(R.drawable.waterwithbridge);
        } else if (cellValue == 70) {
            imageView.setImageResource(R.drawable.roadongrass);
        } else {
            // displaying traps
            String str_val = String.valueOf(cellValue);
            // TODO: decide if I want traps to be visible to everyone in replay
            String trap_val = str_val.substring(0, str_val.length()-1);
            if (trap_val.equals("83030")) {
                // mine detected
                imageView.setImageResource(R.drawable.minegrass);
            } else if (trap_val.equals("2345")) {
                // hijack trap detected
                imageView.setImageResource(R.drawable.hijackgrass);
            }
        }
        return imageView;
    }
}