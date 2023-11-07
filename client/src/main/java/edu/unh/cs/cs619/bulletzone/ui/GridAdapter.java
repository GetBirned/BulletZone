package edu.unh.cs.cs619.bulletzone.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;

import edu.unh.cs.cs619.bulletzone.R;

@EBean
public class GridAdapter extends BaseAdapter {

    private final Object monitor = new Object();
    @SystemService
    protected LayoutInflater inflater;
    private int[][] mEntities = new int[16][16];

    public void updateList(int[][] entities) {
        synchronized (monitor) {
            this.mEntities = entities;
            this.notifyDataSetChanged();
        }
    }
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
        tankID = tankID.substring(2,4);
        return Integer.parseInt(tankID);
    }

    int lastFriendlyDirection; // keeps record of last friendly direction
    int lastEnemyDirection; // keeps record of last enemy direction

    public void setFriendlyTank(ImageView imageView, int direction) {
        lastFriendlyDirection = direction;
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

    public void setEnemyTank(ImageView imageView, int direction) {
        lastEnemyDirection = direction;
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.field_item, null);
        }

        ImageView imageView = convertView.findViewById(R.id.imageView);

        int row = position / 16;
        int col = position % 16;

        int val = mEntities[row][col];
        int friendly;

        synchronized (monitor) {
            if (val > 0) {
                int direction = (val % 10);
                if (val == 1000 || (val > 1000 && val <= 2000)) {
                    imageView.setImageResource(R.drawable.brick); // Set the appropriate image resource for walls
                } else if (val >= 2000000 && val <= 3000000) {
                    imageView.setImageResource(R.drawable.bulletgrass);
                } else if (val >= 10000000 && val <= 20000000) {
                    if (friendlyTank(val) == 0) {
                        setFriendlyTank(imageView, direction); // Set proper friendly tank image
                    } else {
                        setEnemyTank(imageView, direction); // Set proper enemy tank image
                    }
                }
            } else {
                imageView.setImageResource(R.drawable.grass); // Set a default image if no entity
            }
        }

        return imageView;
    }
}


