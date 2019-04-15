package com.ramijemli.sample.activity;


import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AnticipateOvershootInterpolator;

import com.ramijemli.percentagechartview.PercentageChartView;
import com.ramijemli.percentagechartview.callback.AdaptiveColorProvider;
import com.ramijemli.sample.R;
import com.ramijemli.sample.adapter.HomeAdapter;

import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class HomeActivity extends AppCompatActivity {

    @BindView(R.id.recycler_view)
    RecyclerView mHomeRv;
    @BindView(R.id.chart)
    PercentageChartView mChart;

    private Unbinder unbinder;
    private HomeAdapter adapter;
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        unbinder = ButterKnife.bind(this);
        setupLayout();
        setupAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        animate();
    }

    @Override
    protected void onStop() {
        handler.removeCallbacks(runnable);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(runnable);
        handler = null;
        runnable = null;
        adapter.setOnHomeClickedListener(null);
        adapter = null;
        unbinder.unbind();
        unbinder = null;
        super.onDestroy();
    }

    private void setupLayout() {
        final LinearLayoutManager llm = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        llm.setItemPrefetchEnabled(true);
        mHomeRv.setLayoutManager(llm);
        mHomeRv.setHasFixedSize(true);
        adapter = new HomeAdapter(this);
        mHomeRv.setAdapter(adapter);

        adapter.setOnHomeClickedListener(position -> {
            Bundle transitionbundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle();
            switch (position) {
                default:
                case 0:
                    startActivity(new Intent(this, PieActivity.class), transitionbundle);
                    break;

                case 1:
                    startActivity(new Intent(this, RingActivity.class), transitionbundle);
                    break;

                case 2:
                    startActivity(new Intent(this, FillActivity.class), transitionbundle);
                    break;

                case 3:
                    startActivity(new Intent(this, AdaptiveColorsActivity.class), transitionbundle);
                    break;

                case 4:
                    startActivity(new Intent(this, GradientColorsActivity.class), transitionbundle);
                    break;

                case 5:
                    startActivity(new Intent(this, TextFormatterActivity.class), transitionbundle);
                    break;

                case 6:
                    Uri url = Uri.parse(getString(R.string.github_url));
                    Intent intent = new Intent(Intent.ACTION_VIEW, url);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                    break;
            }
        });
    }

    private void setupAnimation() {
        mChart.setAnimationInterpolator(new AnticipateOvershootInterpolator());
        mChart.setAnimationDuration(600);
        mChart.setAdaptiveColorProvider(new AdaptiveColorProvider() {
            @Override
            public int provideProgressColor(float progress) {
                String color;

                if (progress <= 25)
                    color = "#F44336";
                else if (progress <= 50)
                    color = "#9C27B0";
                else if (progress <= 75)
                    color = "#03A9F4";
                else color = "#FFC107";

                return Color.parseColor(color);
            }

            @Override
            public int provideTextColor(float progress) {
                return ColorUtils.blendARGB(provideProgressColor(progress), Color.WHITE, .6f);
            }
        });

        handler = new Handler();
        runnable = () -> {
            mChart.setProgress(new Random().nextInt(100), true);
            animate();
        };
        animate();
    }

    private void animate() {
        handler.postDelayed(runnable, 1500);
    }

}
