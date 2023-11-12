package edu.unh.cs.cs619.bulletzone.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class ReplayGridAdapter extends BaseAdapter {
    private Context context;
    private int[][] board;

    public ReplayGridAdapter(Context context, int[][] board) {
        this.context = context;
        this.board = board;
    }

    @Override
    public int getCount() {
        // Return the total number of items in your grid
        return board.length * board[0].length;
    }

    @Override
    public Object getItem(int position) {
        // Return the data at the specified position
        int row = position / board[0].length;
        int col = position % board[0].length;
        return board[row][col];
    }

    @Override
    public long getItemId(int position) {
        // Return the item ID. In this case, you can use the position as the ID.
        return position;
    }

    public void update(int[][] new_board) {
        this.board = new_board;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Create or reuse an ImageView for the grid item
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(150, 150)); // Set the size of the ImageView
        } else {
            imageView = (ImageView) convertView;
        }

        // Set the drawable resource for the ImageView based on your data
        int data = (int) getItem(position);

        // Assuming your drawable resources are named "drawable1", "drawable2", etc.
        String drawableResourceName = "drawable" + data;
        int drawableResourceId = context.getResources().getIdentifier(drawableResourceName, "drawable", context.getPackageName());

        // Set the drawable resource to the ImageView
        //imageView.setImageResource(drawableResourceId);

        //return imageView;
        return null;
    }

}