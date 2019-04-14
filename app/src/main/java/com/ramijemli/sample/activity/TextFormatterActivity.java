package com.ramijemli.sample.activity;


import android.graphics.Color;
import android.os.Bundle;
import android.transition.Explode;
import android.view.animation.OvershootInterpolator;

import com.ramijemli.percentagechartview.PercentageChartView;
import com.ramijemli.percentagechartview.callback.AdaptiveColorProvider;
import com.ramijemli.sample.R;

import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class TextFormatterActivity extends AppCompatActivity {

    @BindView(R.id.pie_chart)
    PercentageChartView mPieChart;
    @BindView(R.id.ring_chart)
    PercentageChartView mRingChart;
    @BindView(R.id.fill_chart)
    PercentageChartView mFillChart;

    private Unbinder unbinder;
    private AdaptiveColorProvider colorProvider;
    private int maxVoters = 10000;
    private int maxCalories = 3500;
    private int maxDays = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_formatter);
        unbinder = ButterKnife.bind(this);
        setupLayout();
        setupColorProvider();
    }

    @Override
    protected void onDestroy() {
        colorProvider = null;
        unbinder.unbind();
        unbinder = null;
        super.onDestroy();
    }

    private void setupLayout() {
        Explode transition = new Explode();
        transition.setDuration(600);
        transition.setInterpolator(new OvershootInterpolator(1f));
        getWindow().setEnterTransition(transition);

        mPieChart.setTextFormatter(progress -> {
            int voters = (int) (progress * maxVoters / 100);
            if(voters > 1000) {
                voters /= 1000;
                return voters + " k voters";
            }
            return voters + " Voters";
        });

        mFillChart.setTextFormatter(progress -> {
            int cals = (int) (progress * maxCalories / 100);
            return cals + " cal";
        });

        mRingChart.setTextFormatter(progress -> {
            int days = (int) (progress * maxDays / 100);
            return days + " days";
        });
    }

    private void setupColorProvider() {
        //COLOR PROVIDER
        colorProvider = new AdaptiveColorProvider() {
            @Override
            public int provideProgressColor(float progress) {
                if (progress <= 25)
                    return Color.parseColor("#F44336");
                else if (progress <= 50)
                    return Color.parseColor("#FFEA00");
                else if (progress <= 75)
                    return Color.parseColor("#03A9F4");
                else
                    return Color.parseColor("#00E676");
            }

            @Override
            public int provideBackgroundColor(float progress) {
                return ColorUtils.blendARGB(provideProgressColor(progress),
                        Color.BLACK,
                        .8f);
            }

            @Override
            public int provideTextColor(float progress) {
                return ColorUtils.blendARGB(provideProgressColor(progress),
                        Color.WHITE,
                        .8f);
            }

            @Override
            public int provideBackgroundBarColor(float progress) {
                return ColorUtils.blendARGB(provideProgressColor(progress),
                        Color.BLACK,
                        .5f);
            }
        };

        mPieChart.setAdaptiveColorProvider(colorProvider);
        mRingChart.setAdaptiveColorProvider(colorProvider);
        mFillChart.setAdaptiveColorProvider(colorProvider);
    }

    //##############################################################################################   ACTIONS
    @OnClick(R.id.root_layout)
    public void pieChartClickAction() {
        int rand = new Random().nextInt(100);
        mPieChart.setProgress(rand, true);
        mRingChart.setProgress(rand, true);
        mFillChart.setProgress(rand, true);
    }

}
