package com.ramijemli.sample.activity;


import android.animation.TimeInterpolator;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.transition.Explode;
import android.view.animation.OvershootInterpolator;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.Bundler;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.ramijemli.percentagechartview.PercentageChartView;
import com.ramijemli.sample.R;
import com.ramijemli.sample.fragment.BackgroundSubFragment;
import com.ramijemli.sample.fragment.BehaviorSubFragment;
import com.ramijemli.sample.fragment.ProgressSubFragment;
import com.ramijemli.sample.fragment.TextSubFragment;

import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.ramijemli.sample.fragment.BackgroundSubFragment.OFFSET_STATE_ARG;
import static com.ramijemli.sample.fragment.BehaviorSubFragment.ORIENTATION_STATE_ARG;


public class FillActivity extends AppCompatActivity implements BehaviorSubFragment.OnBehaviorChangedListener,
        ProgressSubFragment.OnProgressChangedListener,
        BackgroundSubFragment.OnBackgroundChangedListener,
        TextSubFragment.OnTextChangedListener {

    @BindView(R.id.chart)
    PercentageChartView mChart;
    @BindView(R.id.view_pager)
    ViewPager mViewPager;
    @BindView(R.id.tabs)
    SmartTabLayout mTAbs;

    private Unbinder unbinder;
    private int shadowColor;
    private float blur, distX, distY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill);
        unbinder = ButterKnife.bind(this);
        setupLayout();
    }

    @Override
    protected void onDestroy() {
        unbinder.unbind();
        unbinder = null;
        super.onDestroy();
    }

    private void setupLayout() {
        Explode transition = new Explode();
        transition.setDuration(600);
        transition.setInterpolator(new OvershootInterpolator(1f));
        getWindow().setEnterTransition(transition);

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add("behavior", BehaviorSubFragment.class, new Bundler().putBoolean(ORIENTATION_STATE_ARG, false).get())
                .add("progress", ProgressSubFragment.class)
                .add("background", BackgroundSubFragment.class, new Bundler().putBoolean(OFFSET_STATE_ARG, true).get())
                .add("text", TextSubFragment.class)
                .create());

        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(3);
        mTAbs.setViewPager(mViewPager);

        shadowColor = Color.WHITE;
        blur = distX = distY = 2f;
    }

    //##############################################################################################   CALLBACKS
    @Override
    public void onOrientationChanged(int orientation) {
        mChart.setOrientation(orientation);
    }

    @Override
    public void onStartAngleChanged(int angle) {
        mChart.setStartAngle(angle);
    }

    @Override
    public void onAnimDurationChanged(int duration) {
        mChart.setAnimationDuration(duration);
    }

    @Override
    public void onInterpolatorChanged(TimeInterpolator interpolator) {
        mChart.setAnimationInterpolator(interpolator);
    }

    @Override
    public void onProgressChanged(float progress, boolean animate) {
        mChart.setProgress(progress, animate);
    }

    @Override
    public void onProgressColorChanged(int color) {
        mChart.setProgressColor(color);
    }

    @Override
    public void onDrawBackgroundChanged(boolean draw) {
        mChart.setDrawBackgroundEnabled(draw);
    }

    @Override
    public void onBackgroundColorChanged(int color) {
        mChart.setBackgroundColor(color);
    }

    @Override
    public void onBackgroundOffsetChanged(int offset) {
        mChart.setBackgroundOffset(offset);
    }

    @Override
    public void onTextColorChanged(int color) {
        mChart.setTextColor(color);
    }

    @Override
    public void onTextSizeChanged(int textSize) {
        mChart.setTextSize(textSize);
    }

    @Override
    public void onTextFontChanged(Typeface typeface) {
        mChart.setTypeface(typeface);
    }

    @Override
    public void onTextStyleChanged(int textStyle) {
        mChart.setTextStyle(textStyle);
    }

    @Override
    public void onDrawShadowChanged(boolean draw) {
        if (draw && shadowColor != -1) {
            mChart.setTextShadow(shadowColor, blur, distX, distY);
        } else {
            mChart.setTextShadow(0, 0, 0, 0);
        }
    }

    @Override
    public void onShadowChanged(int color, float blur, float distX, float distY) {
        this.shadowColor = color;
        this.blur = blur;
        this.distX = distX;
        this.distY = distY;
        mChart.setTextShadow(color, blur, distX, distY);
    }

    //##############################################################################################   ACTIONS
    @OnClick(R.id.chart)
    public void chartClickAction() {
        mChart.setProgress(new Random().nextInt(100), true);
    }

}
