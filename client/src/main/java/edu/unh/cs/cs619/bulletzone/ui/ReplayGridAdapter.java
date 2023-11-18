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
    private int[][] board; // Your 2D int array

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

        // 0 grass, // 1 thingamajig //2 nuke //3 apple
        //4 hill // 5 rocky // 6 forest

        int mill_scale = 1000000;
        if (cellValue == 0) {
            imageView.setImageResource(R.drawable.grass);
        } else if (cellValue == 1) {
            // CHANGE
            imageView.setImageResource(R.drawable.grass);
        } else if (cellValue == 2) {
            imageView.setImageResource(R.drawable.nukepowerupgrass);
        } else if (cellValue == 3) {
            imageView.setImageResource(R.drawable.applepowerupgrass);
        } else if (cellValue == 4) {
            imageView.setImageResource(R.drawable.hillyterrain);
        } else if (cellValue == 5) {
            imageView.setImageResource(R.drawable.rockyterrain);
        } else if (cellValue == 6) {
            imageView.setImageResource(R.drawable.forestterrain);
        } else if (cellValue == 1000) {
            imageView.setImageResource(R.drawable.brick);
        } else if (cellValue >= 2*mill_scale && cellValue < 3*mill_scale) {
            imageView.setImageResource(R.drawable.bulletgrass);
        } else if (cellValue >= 10*mill_scale && cellValue < 20*mill_scale) {
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
        }

        return imageView;
    }
}