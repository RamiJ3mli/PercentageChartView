package com.ramijemli.sample.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ramijemli.sample.R;
import com.ramijemli.sample.viewmodel.Showcase;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.BaseViewHolder> {

    private final int VIEW_TYPE_SHOWCASE = R.layout.item_home;
    private final int VIEW_TYPE_DEV = R.layout.item_dev;

    private Context mContext;
    private SparseArray<Showcase> mData;
    private OnHomeClickedListener mListener;

    public HomeAdapter(Context context) {
        this.mContext = context;
        mData = new SparseArray<>();
        int[] colors = mContext.getResources().getIntArray(R.array.home_colors);
        String[] titles = mContext.getResources().getStringArray(R.array.home_titles);
        String[] subtitles = mContext.getResources().getStringArray(R.array.home_subtitles);

        for (int i = 0; i < colors.length; i++) {
            mData.append(i, new Showcase(colors[i], titles[i], subtitles[i]));
        }

        mData.append(colors.length, new Showcase(Color.parseColor("#37474F"), "", ""));
    }

    @Override
    public HomeAdapter.BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(viewType, parent, false);

        switch (viewType) {
            default:
            case VIEW_TYPE_SHOWCASE:
                return new ShowcaseViewHolder(view);
            case VIEW_TYPE_DEV:
                return new DevViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HomeAdapter.BaseViewHolder viewHolder, int position) {
        Showcase data = mData.get(position);
        viewHolder.bind(data);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mData.size() - 1) return VIEW_TYPE_DEV;
        return VIEW_TYPE_SHOWCASE;
    }

    //##############################################################################################   VIEW HOLDERS
    class BaseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View card;

        BaseViewHolder(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_bg);
        }

        void bind(Showcase data){
            card.setBackgroundTintList(ColorStateList.valueOf(data.getColor()));
            card.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClicked(getAdapterPosition());
            }
        }
    }

    class ShowcaseViewHolder extends BaseViewHolder {
        TextView title;
        TextView subtitle;

        ShowcaseViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
        }

        void bind(Showcase data) {
            super.bind(data);
            title.setText(data.getTitle());
            subtitle.setText(data.getSubtitle());
        }
    }

    class DevViewHolder extends BaseViewHolder {

        DevViewHolder(View itemView) {
            super(itemView);
        }

        void bind(Showcase data) {
            super.bind(data);
        }
    }

    //##############################################################################################   CLICK LISTENER
    public void setOnHomeClickedListener(OnHomeClickedListener mListener) {
        this.mListener = mListener;
    }

    public interface OnHomeClickedListener {
        void onItemClicked(int position);
    }
}

