package com.ramijemli.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.ramijemli.percentagechartview.PercentageChartView;
import com.ramijemli.percentagechartview.renderer.BaseModeRenderer;

import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PercentageChartView chart = findViewById(R.id.chart);
        chart.setOnClickListener(view -> {
            int rand = new Random().nextInt(100);
            chart.setProgress(rand, true);
//            chart.setStartAngle(new Random().nextInt(360));
//            chart.setDrawBackgroundEnabled(rand % 2 == 0);
            chart.setOrientation((rand % 2 == 0)? BaseModeRenderer.ORIENTATION_CLOCKWISE: BaseModeRenderer.ORIENTATION_CLOCKWISE);
            chart.setBackgroundColor((rand % 2 == 0) ? Color.BLACK : Color.GREEN);
            chart.setProgressColor((rand % 2 == 0) ? Color.GREEN : Color.BLACK);
        });

        //COLOR PROVIDER
        chart.setAdaptiveColorProvider(value -> {
            String color;

            if (value <= 25)
                color = "#03A9F4";
            else if (value <= 50)
                color = "#FFEB3B";
            else if (value <= 75)
                color = "#26A69A";
            else
                color = "#F44336";

            return Color.parseColor(color);
        });

        //PROGRESS CHANGE LISTENER
        chart.setOnProgressChangeListener(progress -> Log.d(TAG, String.valueOf(progress)));
    }

}
