package com.example.chart;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chart.util.DisplayUtil;

import java.text.MessageFormat;

public class GraphActivity extends AppCompatActivity {

    GraphView mGraphView;

    SeekBar mSeekBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        mGraphView = findViewById(R.id.graph_view);
        mSeekBar = findViewById(R.id.seekBar);
        mSeekBar.setMax(DisplayUtil.dp2px(200));
        mSeekBar.setMin(10);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Button button = findViewById(R.id.button41);
                button.setText(MessageFormat.format("开启/关闭 宽度滑动:{0}", progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void startClick(View view) {
        switch (view.getId()) {
            case R.id.button40:
                mGraphView.toggleAnim();
                break;
            case R.id.button41:
                mGraphView.toggleSpace(mSeekBar.getProgress());
                break;
            case R.id.button42:
                mGraphView.toggleBezierLine();
                break;
        }

    }
}
