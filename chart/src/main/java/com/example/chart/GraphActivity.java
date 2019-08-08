package com.example.chart;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class GraphActivity extends AppCompatActivity {

    GraphView mGraphView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        mGraphView = findViewById(R.id.graph_view);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void startClick(View view) {
        mGraphView.start();
    }
}
