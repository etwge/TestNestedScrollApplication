package com.etwge.testnestedscrollapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Sample2Activity extends AppCompatActivity {

	MyAdapter mAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sample2);
		RecyclerView recyclerView = findViewById(R.id.recyclerView);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		mAdapter = new MyAdapter();
		mAdapter.setData(obtainData());
		recyclerView.setAdapter(mAdapter);
	}

	private List<String> obtainData() {
		List<String> list = new ArrayList<>();
		for (int i = 0; i < 120; i++) {
			list.add("标题" + (i + 1));
		}
		return list;
	}
}
