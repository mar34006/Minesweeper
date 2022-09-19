package com.hfad.minesweeper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        Intent intent = getIntent();
        int seconds = intent.getIntExtra("time taken", -1);
        int game_status = intent.getIntExtra("game status", -1);

        TextView results_tv = findViewById(R.id.results_text);
        if(game_status == 0){
            results_tv.setText("You won.\nGood job.");
        }
        else if(game_status == 1){
            results_tv.setText("You lost.\nNice try.");
        }
        else{
            results_tv.setText("Error");
        }

        TextView time_taken_tv = findViewById(R.id.time_text);
        String time = Integer.toString(seconds);
        time_taken_tv.setText("Used " + time + " seconds.");

    }

    public void onClickPlayAgain(View view) {
        Intent intent = new Intent(this, MineSweeperActivity.class);
        startActivity(intent);
    }
}