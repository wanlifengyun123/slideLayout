package com.example.chart;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class TableActivity extends AppCompatActivity {

    private TableView mTableView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);

        mTableView = findViewById(R.id.table_view);
        with();
    }

    // 初始化数据表格相关
    private void with() {
        // 配置坐标系
        mTableView.setupCoordinator("日", "人", /*这里是横坐标的值*/0f, 5f, 10f, 15f, 20f, 25f, 30f);
        // 添加曲线, 确保纵坐标的数值位数相等
//        mTableView.addWave(ContextCompat.getColor(TableActivity.this, R.color.colorYellow), false,
//                0f, 10f, 30f, 54f, 30f, 100f, 10f);
//        mTableView.addWave(ContextCompat.getColor(TableActivity.this, R.color.colorGreen), false,
//                0f, 30f, 20f, 20f, 46f, 25f, 5f);
//        mTableView. addWave(ContextCompat.getColor(TableActivity.this, R.color.colorPink), false,
//                0f, 30f, 20f, 50f, 46f, 30f, 30f);
        mTableView.addWave(Color.parseColor("#8596dee9"), true,
                0f, 15f, 10f, 10f, 40f, 20f, 5f);
    }
}
