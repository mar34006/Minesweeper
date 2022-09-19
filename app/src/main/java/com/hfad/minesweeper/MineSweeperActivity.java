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

    // Initialize global variables for grid, bombs, winning/losing booleans, and flag mode
    private static final int ROW_COUNT = 10;
    private static final int COLUMN_COUNT = 8;
    private static final int NUM_BOMBS = 4;
    private static final int FLAG_COUNT = 4;

    private static boolean WON = false;
    private static boolean LOST = false;
    private static boolean FLAG_MODE = false;

    // Initialize global data structures for storing which cells cells have bombs, are dug, or
    // have flags
    Set<Pair<Integer,Integer>> bombs = new HashSet<>();
    Set<Integer> dug_cells = new HashSet<>();
    Set<Pair<Integer,Integer>> flag_cells = new HashSet<>();

    // Initialize global variables for timer including first click to start timer, seconds passed,
    // and whether the clock is running or not
    private static boolean FIRST_CLICK = true;
    private static int seconds = 0;
    private static boolean running = false;

    // Initialize global variable for number of flags
    private static int num_flags = FLAG_COUNT;

    // save the TextViews of all cells in an array, so later on,
    // when a TextView is clicked, we know which cell it is
    private ArrayList<TextView> cell_tvs;

    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    // Create method to create main view
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Intialize won and lost in case user clicked play again

        WON = false;
        LOST = false;
        num_flags = FLAG_COUNT;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mine_sweeper);

        // Initialize grid for game
        cell_tvs = new ArrayList<TextView>();
        GridLayout grid = (GridLayout) findViewById(R.id.gridLayout01);
        LayoutInflater li = LayoutInflater.from(this);

        // Reset clock variables in case game is played again
        seconds = 0;
        running = false;
        runTimer();

        // Initialize bomb locations in the grid
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

        // Initialize grid for the game using li.inflate
        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COLUMN_COUNT ; j++) {
                TextView tv =
                        (TextView) li.inflate(R.layout.custom_cell_layout, grid, false);
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

    // Timer method for the clock
    private void runTimer(){

            final TextView timeView = (TextView)findViewById(R.id.time_view);
            final Handler handler = new Handler();
            handler.post(new Runnable() {

            @Override

            // Seconds are added if the clock is running
            public void run() {
                String time = String.format(String.valueOf(seconds));
                timeView.setText(time);
                if (running){
                    seconds++;
                }
                handler.postDelayed(this,1000);
                //Stop the runnable if the game is won or lost
                if(WON || LOST){
                    handler.removeCallbacksAndMessages(null);
                }
            }
        });
    }

    // Method to find index of a cell given the view
    private int findIndexOfCellTextView(TextView tv) {
        for (int n = 0; n < cell_tvs.size(); n++) {
            if (cell_tvs.get(n) == tv)
                return n;
        }
        return -1;
    }

    // Method to count if a cell has nearby bombs
    // Loops through the 9 cells surrounding it, adding to count if a cell nearby contains a bomb
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

    // Reveal method to reveal all bombs if one is clicked
    public void reveal(){
        for(Pair<Integer,Integer> bomb: bombs){
            int row = bomb.first;
            int col = bomb.second;
            int n = row*COLUMN_COUNT + col;
            TextView curr_tv = cell_tvs.get(n);
            String text = getString(R.string.mine);
            curr_tv.setText(text);
            curr_tv.setTextColor(Color.GRAY);
            curr_tv.setBackgroundColor(Color.LTGRAY);
        }
    }

    // Method to reveal mines with stars rather than bombs if the game is won
    public void win_reveal(){
        for(Pair<Integer,Integer> bomb: bombs){
            int row = bomb.first;
            int col = bomb.second;
            int n = row*COLUMN_COUNT + col;
            TextView curr_tv = cell_tvs.get(n);
            String text = getString(R.string.star);
            curr_tv.setText(text);
            curr_tv.setTextColor(Color.GRAY);
            curr_tv.setBackgroundColor(Color.LTGRAY);
        }
    }

    // Method to check if the game is won. If all cells are dug, the game is ober
    public boolean checkWin(){
        if (dug_cells.size() == ROW_COUNT*COLUMN_COUNT - NUM_BOMBS) {
            return true;
        }
        return false;
    }

    // Main click method for grid of cells
    public void onClickTV(View view) {

        // If it is the first click, the timer starts running
        if(FIRST_CLICK){
            running = true;
        }

        // Find n as well as i,j index of the cell that is clicked, and stores as a pair
        TextView tv = (TextView) view;
        int n = findIndexOfCellTextView(tv);
        int row = n / COLUMN_COUNT;
        int col = n % COLUMN_COUNT;
        Pair<Integer, Integer> curr_location = new Pair<>(row, col);

        // If the game is not yet won or lost...
        if(!WON && !LOST) {
            // If the player is not placing a flag...
            if (!FLAG_MODE) {
                // If the player is not trying to dig a cell with a flag on it...
                if (!flag_cells.contains(curr_location)) {

                    // If the player clicked a bomb, stop the clock, reveal all other bombs, and
                    // change LOST boolean to true
                    if (bombs.contains(curr_location)) {
                        reveal();
                        running = false;
                        LOST = true;
                    }
                    // If the player did not click a cell with a bomb in it...
                    else {
                        // Count nearby bombs
                        Integer value = countNearbyBombs(row, col);

                        // If there are no nearby bombs, we must reveal values adjacent cells as
                        // well. This is implemented recursively
                        if (value == 0) {
                            // Set color and value and add cell to dug cells
                            tv.setText("");
                            tv.setTextColor(Color.GRAY);
                            tv.setBackgroundColor(Color.LTGRAY);
                            dug_cells.add(n);

                            // For each of the nine cells surrounding the current cell, call the
                            // onClick method again. Ensure we do not reveal a bomb or an already
                            // clicked cell. If we reveal an already clicked cell, the method will
                            // recurse indefinitely
                            for (int col_mult = -1; col_mult <= 1; col_mult++) {
                                for (int index = n + col_mult * COLUMN_COUNT - 1;
                                     index <= n + col_mult * COLUMN_COUNT + 1; index++) {
                                    int index_row = index / COLUMN_COUNT;
                                    int index_col = index % COLUMN_COUNT;
                                    Pair<Integer, Integer> index_location =
                                            new Pair<>(index_row, index_col);

                                    //Cells should not be revealed if n is on the opposite side of
                                    // the grid
                                    if((col == 0 && index_col == COLUMN_COUNT -1)||
                                            (index_col == 0 && col == COLUMN_COUNT -1)){
                                        continue;
                                    }

                                    if (index >= 0 && index < ROW_COUNT * COLUMN_COUNT){
                                        if (!dug_cells.contains(index) &&
                                                !bombs.contains(index_location) &&
                                                !flag_cells.contains(index_location)) {
                                            TextView new_tv = cell_tvs.get(index);
                                            onClickTV(new_tv);
                                        }
                                    }
                                }
                            }
                        }
                        // If the cell is adjacent to a bomb, reveal how many bombs it is adjacent
                        // to and add to dug cells
                        else {
                            tv.setText(value.toString());
                            tv.setTextColor(Color.GRAY);
                            tv.setBackgroundColor(Color.LTGRAY);
                            dug_cells.add(n);
                        }
                        // Check to see if the game is won after the last dig
                        if (checkWin()) {
                            running = false;
                            WON = true;
                            win_reveal();
                        }
                    }
                }
            }
            // If the player is currently placing a flag...
            else {
                // If the player is not trying to place a flag on an already dug cell...
                if(!dug_cells.contains(n)) {
                    TextView flag_view = findViewById(R.id.flag_count);

                    // If the player is trying to remove a flag rather than place it, remove flag
                    // icon from cell and increment number of flags count
                    if (tv.getCurrentTextColor() == Color.LTGRAY) {
                        tv.setText("");
                        tv.setTextColor(Color.GREEN);
                        flag_cells.remove(curr_location);
                        num_flags++;
                        flag_view.setText(String.valueOf(num_flags));
                    }
                    // If the player is trying to place a flag rather than remove it, add flag
                    // icon to cell and decrement number of flags count
                    else {
                        String text = getString(R.string.flag);
                        tv.setText(text);
                        tv.setTextColor(Color.LTGRAY);
                        flag_cells.add(curr_location);
                        num_flags--;
                        flag_view.setText(String.valueOf(num_flags));
                    }
                }
            }

        }
        // If the game was won/lost on the last click, send game status and seconds passed to
        // results view via intent
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

    // Click method for the pick/flag icon.
    public void onClickFlag(View view) {
        TextView tv = (TextView) view;
        // If flag mode is currently on, switch icon to pick and turn flag mode off
        if (FLAG_MODE) {
            FLAG_MODE = false;
            String text = getString(R.string.pick);
            tv.setText(text);
        }
        // If flag mode is currently off, switch icon to flag and turn flag mode on
        else {
            FLAG_MODE = true;
            String text = getString(R.string.flag);
            tv.setText(text);
        }
    }
}
