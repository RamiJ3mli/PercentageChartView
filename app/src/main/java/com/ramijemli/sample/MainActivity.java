package com.ramijemli.sample;

import android.graphics.Color;
import android.os.Bundle;

import com.ramijemli.percentagechartview.PercentageChartView;

import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PercentageChartView circular = findViewById(R.id.progress);
        circular.setOnClickListener(view -> circular.setPercentage(new Random().nextInt(100), true));
        circular.setColorProvider(value -> {
            String color;

            if (value <= 25)
                color ="#F44336";
            else if (value <= 50)
                color = "#FFB300";
            else if (value <= 75)
                color = "#00E676";
            else
                color = "#18FFFF";

            return Color.parseColor(color);
        });
    }
}
