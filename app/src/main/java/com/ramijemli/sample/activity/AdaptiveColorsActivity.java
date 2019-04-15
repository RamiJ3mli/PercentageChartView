package com.ramijemli.sample.activity;


import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.ramijemli.percentagechartview.PercentageChartView;
import com.ramijemli.percentagechartview.callback.AdaptiveColorProvider;
import com.ramijemli.sample.R;

import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.ColorUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.MODE_FILL;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.MODE_PIE;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.MODE_RING;


public class AdaptiveColorsActivity extends AppCompatActivity {

    @BindView(R.id.constraint_layout)
    ConstraintLayout mConstraintLayout;

    @BindView(R.id.pie_chart)
    PercentageChartView mPieChart;
    @BindView(R.id.ring_chart)
    PercentageChartView mRingChart;
    @BindView(R.id.fill_chart)
    PercentageChartView mFillChart;

    //PROVIDED COLORS
    @BindView(R.id.color_one)
    Button mColorOne;
    @BindView(R.id.color_two)
    Button mColorTwo;
    @BindView(R.id.color_three)
    Button mColorThree;
    @BindView(R.id.color_four)
    Button mColorFour;

    private Unbinder unbinder;
    private ConstraintSet mConstraintSet;
    private Transition fade;
    private AdaptiveColorProvider colorProvider;
    private int colorOne;
    private int colorTwo;
    private int colorThree;
    private int colorFour;
    private int displayedMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adaptive_colors);
        unbinder = ButterKnife.bind(this);
        setupLayoutAnimation();
        setupColorProvider();
    }

    @Override
    protected void onDestroy() {
        colorProvider = null;
        unbinder.unbind();
        unbinder = null;
        super.onDestroy();
    }

    private void setupLayoutAnimation() {
        Explode transition = new Explode();
        transition.setDuration(600);
        transition.setInterpolator(new OvershootInterpolator(1f));
        getWindow().setEnterTransition(transition);

        displayedMode = MODE_PIE;

        mConstraintSet = new ConstraintSet();
        mConstraintSet.clone(mConstraintLayout);

        fade = new Fade();
        fade.setDuration(400);
    }

    private void setupColorProvider() {
        colorOne = Color.parseColor("#F44336");
        colorTwo = Color.parseColor("#FFEA00");
        colorThree = Color.parseColor("#03A9F4");
        colorFour = Color.parseColor("#00E676");

        //COLOR PROVIDER
        colorProvider = new AdaptiveColorProvider() {
            @Override
            public int provideProgressColor(float progress) {
                if (progress <= 25)
                    return colorOne;
                else if (progress <= 50)
                    return colorTwo;
                else if (progress <= 75)
                    return colorThree;
                else
                    return colorFour;
            }

            @Override
            public int provideBackgroundColor(float progress) {
                return ColorUtils.blendARGB(provideProgressColor(progress),
                        Color.BLACK,
                        .8f);
            }

            @Override
            public int provideTextColor(float progress) {
                if (displayedMode == MODE_RING) {
                    return provideProgressColor(progress);
                }

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

    private void changeMode(int pendingMode) {
        switch (pendingMode) {
            case MODE_PIE:
                mConstraintSet.setVisibility(R.id.pie_chart, ConstraintSet.VISIBLE);
                mConstraintSet.setVisibility(R.id.fill_chart, ConstraintSet.GONE);
                mConstraintSet.setVisibility(R.id.ring_chart, ConstraintSet.GONE);
                break;
            case MODE_FILL:
                mConstraintSet.setVisibility(R.id.pie_chart, ConstraintSet.GONE);
                mConstraintSet.setVisibility(R.id.fill_chart, ConstraintSet.VISIBLE);
                mConstraintSet.setVisibility(R.id.ring_chart, ConstraintSet.GONE);
                break;
            case MODE_RING:
                mConstraintSet.setVisibility(R.id.pie_chart, ConstraintSet.GONE);
                mConstraintSet.setVisibility(R.id.fill_chart, ConstraintSet.GONE);
                mConstraintSet.setVisibility(R.id.ring_chart, ConstraintSet.VISIBLE);
                break;
        }

        displayedMode = pendingMode;
        applyTransition();
    }

    private void applyTransition() {
        TransitionManager.beginDelayedTransition(mConstraintLayout, fade);
        mConstraintSet.applyTo(mConstraintLayout);
    }

    //##############################################################################################   ACTIONS
    @OnClick(R.id.pie_chart)
    public void pieChartClickAction() {
        mPieChart.setProgress(new Random().nextInt(100), true);
    }

    @OnClick(R.id.ring_chart)
    public void ringChartClickAction() {
        mRingChart.setProgress(new Random().nextInt(100), true);
    }

    @OnClick(R.id.fill_chart)
    public void fillChartClickAction() {
        mFillChart.setProgress(new Random().nextInt(100), true);
    }

    @OnClick(R.id.pie_mode)
    public void pieModeAction() {
        if (displayedMode == MODE_PIE) return;
        changeMode(MODE_PIE);
    }

    @OnClick(R.id.ring_mode)
    public void ringModeAction() {
        if (displayedMode == MODE_RING) return;
        changeMode(MODE_RING);
    }

    @OnClick(R.id.fill_mode)
    public void fillModeAction() {
        if (displayedMode == MODE_FILL) return;
        changeMode(MODE_FILL);
    }


    @OnClick(R.id.color_one)
    public void colorOneAction() {
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose first color")
                .initialColor(Color.WHITE)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(6)
                .setPositiveButton("SET", (dialog, selectedColor, allColors) -> {
                    colorOne = selectedColor;
                    mColorOne.setBackgroundTintList(ColorStateList.valueOf(colorOne));
                })
                .setNegativeButton("DISMISS", (dialog, which) -> {
                })
                .build()
                .show();
    }

    @OnClick(R.id.color_two)
    public void colorTwoAction() {
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose second color")
                .initialColor(Color.WHITE)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(6)
                .setPositiveButton("SET", (dialog, selectedColor, allColors) -> {
                    colorTwo = selectedColor;
                    mColorTwo.setBackgroundTintList(ColorStateList.valueOf(colorTwo));
                })
                .setNegativeButton("DISMISS", (dialog, which) -> {
                })
                .build()
                .show();
    }

    @OnClick(R.id.color_three)
    public void colorThreeAction() {
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose three color")
                .initialColor(Color.WHITE)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(6)
                .setPositiveButton("SET", (dialog, selectedColor, allColors) -> {
                    colorThree = selectedColor;
                    mColorThree.setBackgroundTintList(ColorStateList.valueOf(colorThree));
                })
                .setNegativeButton("DISMISS", (dialog, which) -> {
                })
                .build()
                .show();
    }

    @OnClick(R.id.color_four)
    public void colorFourAction() {
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose four color")
                .initialColor(Color.WHITE)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(6)
                .setPositiveButton("SET", (dialog, selectedColor, allColors) -> {
                    colorFour = selectedColor;
                    mColorFour.setBackgroundTintList(ColorStateList.valueOf(colorFour));
                })
                .setNegativeButton("DISMISS", (dialog, which) -> {
                })
                .build()
                .show();
    }

}
