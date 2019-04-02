package com.ramijemli.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.ramijemli.percentagechartview.PercentageChartView;

import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PercentageChartView chart = findViewById(R.id.chart);
        chart.setOnClickListener(view -> chart.setPercentage(100, true));

        //COLOR PROVIDER
        chart.setColorProvider(value -> {
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

        //PROGRESS CHANGE LISTENER
        chart.setOnProgressChangeListener(new PercentageChartView.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(float progress) {
                Log.d(TAG, String.valueOf(progress));
            }
        });
    }
}
