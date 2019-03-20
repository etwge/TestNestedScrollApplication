package com.etwge.testnestedscrollapplication;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

	private List<String> mData;
	@NonNull
	@Override
	public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_text, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
		holder.title.setText("标题" + (position+1));
	}

	@Override
	public int getItemCount() {
		return mData == null? 0 : mData.size();
	}

	static class MyViewHolder extends RecyclerView.ViewHolder{
		TextView title;
		public MyViewHolder(View itemView) {
			super(itemView);
			title = (TextView) itemView;
		}
	}

	public void onItemMove(int from, int to) {
		Collections.swap(mData, from, to);
		notifyItemMoved(from, to);
	}

	public void setData(List<String> data) {
		mData = data;
		notifyDataSetChanged();
	}
}

