package com.example.chart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chart.util.DisplayUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayUtil.init(this);
        setContentView(R.layout.activity_main);

    }

    public void multiClick(View view) {
        startActivity(new Intent(MainActivity.this, MultiGroupActivity.class));
    }

    public void lineClick(View view) {
        startActivity(new Intent(MainActivity.this, LineChartActivity.class));
    }

    public void graphClick(View view){
        startActivity(new Intent(MainActivity.this, GraphActivity.class));
    }

    public void tableClick(View view){
        startActivity(new Intent(MainActivity.this, TableActivity.class));
    }

    public void lineGraphicClick(View view){
        startActivity(new Intent(MainActivity.this, SimpleAnimationActivity.class));
    }
}
