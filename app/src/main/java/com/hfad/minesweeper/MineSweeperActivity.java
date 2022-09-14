package com.hfad.minesweeper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MineSweeperActivity extends AppCompatActivity {

    private static final int ROW_COUNT = 10;
    private static final int COLUMN_COUNT = 8;
    private static final int NUM_BOMBS = 4;
    private static boolean WON = false;
    private static boolean LOST = false;
    private static boolean FLAG_MODE = false;
    Set<Pair<Integer,Integer>> bombs = new HashSet<>();
    Set<Integer> dug_cells = new HashSet<>();
    Set<Pair<Integer,Integer>> flag_cells = new HashSet<>();

    private static boolean FIRST_CLICK = true;
    private static int seconds = 0;
    private static boolean running = false;

    // save the TextViews of all cells in an array, so later on,
    // when a TextView is clicked, we know which cell it is
    private ArrayList<TextView> cell_tvs;

    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mine_sweeper);

        cell_tvs = new ArrayList<TextView>();
        GridLayout grid = (GridLayout) findViewById(R.id.gridLayout01);
        LayoutInflater li = LayoutInflater.from(this);

        runTimer();

        for (int i = 0; i < NUM_BOMBS; i++){
            while(true) {
                Random rand = new Random();
                int random_row = rand.nextInt(9);
                int random_col = rand.nextInt(7);
                Pair<Integer, Integer> curr_bomb = new Pair<>(random_row, random_col);
                if (!bombs.contains(curr_bomb)){
                    bombs.add(curr_bomb);
                    break;
                }
            }
        }

        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COLUMN_COUNT ; j++) {
                TextView tv = (TextView) li.inflate(R.layout.custom_cell_layout, grid, false);
                tv.setTextColor(Color.GREEN);
                tv.setBackgroundColor(Color.parseColor("lime"));
                tv.setOnClickListener(this::onClickTV);

                GridLayout.LayoutParams lp = (GridLayout.LayoutParams) tv.getLayoutParams();
                lp.rowSpec = GridLayout.spec(i);
                lp.columnSpec = GridLayout.spec(j);

                grid.addView(tv, lp);
                cell_tvs.add(tv);
            }
        }
    }

    private void runTimer(){
        final TextView timeView = (TextView)findViewById(R.id.time_view);
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours = seconds/3600;
                int minutes = (seconds%3600)/60;
                int secs = seconds%60;
                String time = String.format("%d:%02d:%02d", hours, minutes, secs);
                timeView.setText(time);
                if (running){
                    seconds++;
                }
                handler.postDelayed(this,1000);
            }
        });
    }

    private int findIndexOfCellTextView(TextView tv) {
        for (int n = 0; n < cell_tvs.size(); n++) {
            if (cell_tvs.get(n) == tv)
                return n;
        }
        return -1;
    }

    private int countNearbyBombs(int row, int col){
        int count = 0;
        for(Integer i = row - 1; i <= row + 1; i++){
            for(Integer j = col -1; j <= col + 1; j++) {
                if(i == row && j == col){
                    continue;
                }
                if((i >= 0 && i < ROW_COUNT) && (j >= 0 && j < COLUMN_COUNT)){
                    Pair<Integer, Integer> curr_location = new Pair<>(i, j);
                    if(bombs.contains(curr_location)){
                        count += 1;
                    }
                }
            }
        }
        return count;
    }

    public void reveal(){
        for(Pair<Integer,Integer> bomb: bombs){
            int row = bomb.first;
            int col = bomb.second;
            int n = row*COLUMN_COUNT + col;
            TextView curr_tv = cell_tvs.get(n);
            curr_tv.setText("X");
            curr_tv.setTextColor(Color.GRAY);
            curr_tv.setBackgroundColor(Color.LTGRAY);
        }
    }

    public boolean checkWin(){
        if (dug_cells.size() == ROW_COUNT*COLUMN_COUNT - NUM_BOMBS) {
            return true;
        }
        return false;
    }

    public void winning_reveal(){
        for(Pair<Integer,Integer> bomb: bombs){
            int row = bomb.first;
            int col = bomb.second;
            int n = row*COLUMN_COUNT + col;
            TextView curr_tv = cell_tvs.get(n);
            curr_tv.setText(":)");
            curr_tv.setTextColor(Color.GRAY);
            curr_tv.setBackgroundColor(Color.LTGRAY);
        }
    }

    public void onClickTV(View view) {

        if(FIRST_CLICK){
            running = true;
        }

        TextView tv = (TextView) view;
        int n = findIndexOfCellTextView(tv);
        int row = n / COLUMN_COUNT;
        int col = n % COLUMN_COUNT;
        Pair<Integer, Integer> curr_location = new Pair<>(row, col);
        if(!WON && !LOST) {
            if (!FLAG_MODE) {
                if (!flag_cells.contains(curr_location)) {
                    if (bombs.contains(curr_location)) {
                        reveal();
                        running = false;
                        LOST = true;
                    } else {
                        Integer value = countNearbyBombs(row, col);
                        if (value == 0) {
                            tv.setText("");
                            tv.setTextColor(Color.GRAY);
                            tv.setBackgroundColor(Color.LTGRAY);
                            dug_cells.add(n);
                            for (int col_mult = -1; col_mult <= 1; col_mult++) {
                                for (int index = n + col_mult * COLUMN_COUNT - 1; index <= n + col_mult * COLUMN_COUNT + 1; index++) {
                                    int index_row = index / COLUMN_COUNT;
                                    int index_col = index % COLUMN_COUNT;
                                    Pair<Integer, Integer> index_location = new Pair<>(index_row, index_col);
                                    if (index >= 0 && index < ROW_COUNT * COLUMN_COUNT && !dug_cells.contains(index) && !bombs.contains(index_location)) {
                                        TextView new_tv = cell_tvs.get(index);
                                        onClickTV(new_tv);
                                    }
                                }
                            }
                        } else {
                            tv.setText(value.toString());
                            tv.setTextColor(Color.GRAY);
                            tv.setBackgroundColor(Color.LTGRAY);
                            dug_cells.add(n);
                        }
                        if (checkWin()) {
                            running = false;
                            WON = true;
                            winning_reveal();
                        }
                    }
                }
            } else {
                if (tv.getCurrentTextColor() == Color.GRAY) {
                    tv.setText("");
                    tv.setTextColor(Color.GREEN);
                    flag_cells.remove(curr_location);
                } else {
                    tv.setText("F");
                    tv.setTextColor(Color.LTGRAY);
                    flag_cells.add(curr_location);
                }
            }

        }
        else if(WON){
            Intent intent = new Intent(this, ResultsActivity.class);
            intent.putExtra("time taken",seconds);
            intent.putExtra("game status",0);
            startActivity(intent);
        }
        else{
            Intent intent = new Intent(this, ResultsActivity.class);
            intent.putExtra("time taken",seconds);
            intent.putExtra("game status",1);
            startActivity(intent);
        }
    }

    public void onClickFlag(View view){
        TextView tv = (TextView) view;
        if(FLAG_MODE){
            FLAG_MODE = false;
            tv.setBackgroundColor(Color.parseColor("#FF3700B3"));
        } else {
            FLAG_MODE = true;
            tv.setBackgroundColor(Color.GRAY);
        }
    }
}
