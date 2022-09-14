package com.hfad.minesweeper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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
            results_tv.setText("You won!");
        }
        else if(game_status == 1){
            results_tv.setText("You lost... :(");
        }
        else{
            results_tv.setText("Error");
        }

        TextView time_taken_tv = findViewById(R.id.time_text);
        int hours = seconds/3600;
        int minutes = (seconds%3600)/60;
        int secs = seconds%60;
        String time = String.format("%d:%02d:%02d", hours, minutes, secs);
        time_taken_tv.setText(time);

    }
}