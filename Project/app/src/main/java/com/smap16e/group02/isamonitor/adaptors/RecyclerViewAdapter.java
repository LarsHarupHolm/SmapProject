package com.smap16e.group02.isamonitor.adaptors;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.smap16e.group02.isamonitor.ParameterDetailActivity;
import com.smap16e.group02.isamonitor.ParameterDetailFragment;
import com.smap16e.group02.isamonitor.ParameterListActivity;
import com.smap16e.group02.isamonitor.R;
import com.smap16e.group02.isamonitor.model.Parameter;

import java.util.List;

/**
 * Created by KSJensen on 1/10/2016.
 * Based on the official Master/Detail template.
 */

public class RecyclerViewAdapter
        extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private final List<Parameter> mValues;
    private FragmentManager fragmentManager;
    private Context context;

    public RecyclerViewAdapter(List<Parameter> items, FragmentManager fragmentManager) {
        mValues = items;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.parameter_list_content, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).name);
        holder.mContentView.setText(mValues.get(position).readingToString());
        if (holder.mItem != null && holder.mItem.isValid != null) {
            holder.mIsValidView.setColorFilter(ContextCompat.getColor(context, holder.mItem.isValid ? R.color.greenA700 : R.color.redA700));
        }
        // When user clicks on item in list
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ParameterListActivity.modeTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(ParameterDetailFragment.ARG_ITEM_ID, Integer.toString(holder.mItem.id));
                    ParameterDetailFragment fragment = new ParameterDetailFragment();
                    fragment.setArguments(arguments);

                    fragmentManager.beginTransaction()
                            .replace(R.id.parameter_detail_container, fragment)
                            .commit();
                } else {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, ParameterDetailActivity.class);
                    intent.putExtra(ParameterDetailFragment.ARG_ITEM_ID, Integer.toString(holder.mItem.id));

                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public final ImageView mIsValidView;
        public Parameter mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
            mIsValidView = (ImageView) view.findViewById(R.id.statusIcon);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
