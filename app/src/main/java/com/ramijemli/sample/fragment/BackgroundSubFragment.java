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
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class BackgroundSubFragment extends Fragment {

    public static final String OFFSET_STATE_ARG = "BackgroundSubFragment.OFFSET_STATE_ARG";

    //DRAW BACKGROUND STATE
    @BindView(R.id.draw_background)
    CheckBox mDrawBackground;

    //BACKGROUND COLOR
    @BindView(R.id.background_color)
    Button mBackgroundColor;

    //BACKGROUND OFFSET
    @BindView(R.id.offset_value)
    TextView mOffsetValue;
    @BindView(R.id.offset_visibility)
    Group mOffsetGroup;

    private OnBackgroundChangedListener mListener;
    private Unbinder unbinder;
    private boolean enableOffset;

    public BackgroundSubFragment() {
    }

    public static BackgroundSubFragment newInstance() {
        return new BackgroundSubFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnBackgroundChangedListener) {
            mListener = (OnBackgroundChangedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnBackgroundChangedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            enableOffset = getArguments().getBoolean(OFFSET_STATE_ARG, false);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sub_fragment_background, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupDrawBackgroundState();
        if(enableOffset){
            mOffsetGroup.setVisibility(View.VISIBLE);
        } else {
            mOffsetGroup.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        mDrawBackground.setOnCheckedChangeListener(null);
        unbinder.unbind();
        unbinder = null;
        super.onDestroyView();
    }

    //##############################################################################################   BEHAVIOR
    private void setupDrawBackgroundState() {
        mDrawBackground.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String text = String.valueOf(isChecked);
            mDrawBackground.setText(Character.toUpperCase(text.charAt(0)) + text.substring(1).toLowerCase());
            if (mListener != null) {
                mListener.onDrawBackgroundChanged(isChecked);
            }
        });
    }

    private void tweakOffset(int amount) {
        int value = getTextViewValue(mOffsetValue);

        int maxValue = (int) ScreenUtil.convertPixelsToDIP(getActivity(), getView().getMeasuredWidth() / 2);
        if (value + amount > maxValue) {
            value = maxValue;
        } else if (value + amount <= 0) {
            value = 0;
        } else {
            value += amount;
        }

        mOffsetValue.setText(value + " dp");

        if (mListener != null) {
            mListener.onBackgroundOffsetChanged(ScreenUtil.convertDIPToPixels(getActivity(), value));
        }
    }

    private int getTextViewValue(TextView view) {
        String value = view.getText().toString();
        return Integer.parseInt(value.substring(0, value.length() - 3));
    }

    //##############################################################################################   ACTIONS
    @OnClick(R.id.background_color)
    void backgroundAction() {
        ColorPickerDialogBuilder
                .with(getActivity())
                .setTitle("Choose background color")
                .initialColor(Color.WHITE)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(6)
                .setPositiveButton("SET", (dialog, selectedColor, allColors) -> {
                    mBackgroundColor.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
                    if (mListener != null) {
                        mListener.onBackgroundColorChanged(selectedColor);
                    }
                })
                .setNegativeButton("DISMISS", (dialog, which) -> {
                })
                .build()
                .show();
    }

    @OnClick(R.id.increment_offset)
    void incrementOffsetAction() {
        tweakOffset(1);
    }

    @OnClick(R.id.decrement_offset)
    void decrementOffsetAction() {
        tweakOffset(-1);
    }

    //##############################################################################################   LISTENER
    public interface OnBackgroundChangedListener {
        void onDrawBackgroundChanged(boolean draw);

        void onBackgroundColorChanged(int color);

        void onBackgroundOffsetChanged(int offset);
    }
}
