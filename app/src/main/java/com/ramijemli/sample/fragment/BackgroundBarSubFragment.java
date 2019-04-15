package com.ramijemli.sample.fragment;


import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.ramijemli.sample.R;
import com.ramijemli.sample.util.ScreenUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class BackgroundBarSubFragment extends Fragment {


    //DRAW BACKGROUND BAR STATE
    @BindView(R.id.draw_bg_bar)
    CheckBox mDrawBgBar;

    //BACKGROUND BAR COLOR
    @BindView(R.id.bg_bar_color)
    Button mBgBarColor;

    //BACKGROUND BAR THICKNESS
    @BindView(R.id.bg_bar_thickness_value)
    TextView mBgBarThicknessValue;


    private OnBackgroundBarChangedListener mListener;
    private Unbinder unbinder;

    public BackgroundBarSubFragment() {
    }

    public static BackgroundBarSubFragment newInstance() {
        return new BackgroundBarSubFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnBackgroundBarChangedListener) {
            mListener = (OnBackgroundBarChangedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnBackgroundBarChangedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sub_fragment_background_bar, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupDrawBgBarState();
    }

    @Override
    public void onDestroyView() {
        mDrawBgBar.setOnCheckedChangeListener(null);
        unbinder.unbind();
        unbinder = null;
        super.onDestroyView();
    }

    //##############################################################################################   BEHAVIOR
    private void setupDrawBgBarState() {
        mDrawBgBar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String text = String.valueOf(isChecked);
            mDrawBgBar.setText(Character.toUpperCase(text.charAt(0)) + text.substring(1).toLowerCase());
            if (mListener != null) {
                mListener.onDrawBgBarChanged(isChecked);
            }
        });
    }

    private void tweakBgThickness(int amount) {
        int value = getTextViewValue(mBgBarThicknessValue);

        int maxValue = (int) ScreenUtil.convertPixelsToDIP(getActivity(), getView().getMeasuredWidth() / 2);
        if (value + amount > maxValue) {
            value = maxValue;
        } else if (value + amount <= 0) {
            value = 0;
        } else {
            value += amount;
        }

        mBgBarThicknessValue.setText(value + " dp");
        if (mListener != null) {
            mListener.onBgBarThicknessChanged(ScreenUtil.convertDIPToPixels(getActivity(), value));
        }
    }

    private int getTextViewValue(TextView view) {
        String value = view.getText().toString();
        return Integer.parseInt(value.substring(0, value.length() - 3));
    }

    //##############################################################################################   ACTIONS
    @OnClick(R.id.bg_bar_color)
    void bgBarColorAction() {
        ColorPickerDialogBuilder
                .with(getActivity())
                .setTitle("Choose Background bar color")
                .initialColor(Color.WHITE)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(6)
                .setPositiveButton("SET", (dialog, selectedColor, allColors) -> {
                    mBgBarColor.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
                    if (mListener != null) {
                        mListener.onBgBarColorChanged(selectedColor);
                    }
                })
                .setNegativeButton("DISMISS", (dialog, which) -> {
                })
                .build()
                .show();
    }

    @OnClick(R.id.increment_bg_bar_thickness)
    void incrementBgThicknessAction() {
        tweakBgThickness(1);
    }

    @OnClick(R.id.decrement_bg_bar_thickness)
    void decrementBgThicknessAction() {
        tweakBgThickness(-1);
    }

    //##############################################################################################   LISTENER
    public interface OnBackgroundBarChangedListener {
        void onDrawBgBarChanged(boolean draw);

        void onBgBarColorChanged(int color);

        void onBgBarThicknessChanged(int thickness);
    }
}
