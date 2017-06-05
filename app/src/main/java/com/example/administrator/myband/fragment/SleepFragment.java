package com.example.administrator.myband.fragment;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.administrator.myband.R;
import com.example.administrator.myband.activity.MainActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/4/14.
 */

public class SleepFragment extends Fragment {

    private static String key = "key";
    private static int mFlag;




    private BarChart mChart6;

    private TextView mSleepTemperatureTextView, mSleepWakeTimeTextView, mSleepShallowTimeTextView,
            mSleepDeepTimeTextView;
    private TextView mSleepQuality, mSleepTotalTime, mDreamTest;
    private int mMode;

    private int hour1 = 0, hour2 = 0, hour3 = 0, min1 = 0, min2 = 0, min3 = 0;
    private float flag1 = 0, flag2 = 0, flag3 = 0;
    private float dreamtest = 70;
    private float quality = 0;
    private float sleeptime = 0;       //总的睡眠时间定为450分钟


    //这是条形图的Y轴的值
    ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();


    public static SleepFragment newInstance(int index) {
        SleepFragment fm = new SleepFragment();
        Bundle args = new Bundle();
        args.putInt(key, index);
        fm.setArguments(args);
        mFlag = index;
        return fm;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {

        View view =LayoutInflater.from(getActivity()).inflate(R.layout.fragment_sleep, null);


//        mSleepTemperatureTextView = (TextView) findViewById(R.id.temperature);
        mSleepWakeTimeTextView = (TextView) view.findViewById(R.id.waketime);
        mSleepShallowTimeTextView = (TextView) view.findViewById(R.id.shallowsleeptime);
        mSleepDeepTimeTextView = (TextView) view.findViewById(R.id.deepsleeptime);


        mSleepQuality = (TextView) view.findViewById(R.id.sleepquality);
        mSleepTotalTime = (TextView) view.findViewById(R.id.totalsleeptime);
        mDreamTest = (TextView) view.findViewById(R.id.dreamtest);


        //这里的代码用于用于修改柱状图
        mChart6 = (BarChart) view.findViewById(R.id.chart6);

        mChart6.setDescription("");

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mChart6.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        mChart6.setPinchZoom(true);

        mChart6.setDrawBarShadow(false);
        mChart6.setDrawGridBackground(false);

        XAxis xAxis6 = mChart6.getXAxis();
        xAxis6.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis6.setDrawGridLines(false);

        mChart6.getAxisLeft().setDrawGridLines(false);


        // add a nice and smooth animation
        mChart6.animateX(2500);

        mChart6.getLegend().setEnabled(false);


        setData6();

        mSleepWakeTimeTextView.setText("清醒时间：\t\t" + hour1 + "\t小时\t" + min1 + "\t分钟");
        mSleepShallowTimeTextView.setText("浅睡时间：\t\t" + hour2 + "\t小时\t" + min2 + "\t分钟");
        mSleepDeepTimeTextView.setText("深睡时间：\t\t" + hour3 + "\t小时\t" + min3 + "\t分钟");

        return view;
    }

    public void setData6() {

        BarDataSet set1;
        if (mChart6.getData() != null &&
                mChart6.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) mChart6.getData().getDataSetByIndex(0);
            set1.setValues(yVals1);
            mChart6.getData().notifyDataChanged();
            mChart6.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(yVals1, "Data Set");
            //set1.setColors(ColorTemplate.VORDIPLOM_COLORS);
            set1.setDrawValues(false);

            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            mChart6.setData(data);
            mChart6.setFitBars(true);
        }

        mChart6.invalidate();
    }


    @Override
    public void onResume() {
        super.onResume();
        setData6();
        MainActivity.sendFlag('b');
        mHandler4.post(runnable);
    }


    //下面的代码用于自动刷新页面数据
    Handler mHandler4 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    Runnable runnable = new Runnable() {
        String lineText2 = null;

        @Override
        public void run() {

            flag1 = 0;
            flag2 = 0;
            flag3 = 0;
            int k = 0;


            for (int i = 0; i < MainActivity.sSleepBuffer.length(); i++) {
                k++;
                //清醒时间
                if ('3'==MainActivity.sSleepBuffer.charAt(i)) {
                    yVals1.add(new BarEntry(k, 1));
                    min1 += 10;
                    flag1 += 10;
                    if (min1 == 60) {
                        min1 = 0;
                        hour1++;
                    }
                }
                //浅睡
                if ('2'==MainActivity.sSleepBuffer.charAt(i)) {
                    yVals1.add(new BarEntry(k, 10));
                    sleeptime += 10;
                    min2 += 10;
                    flag2 += 10;
                    if (min2 == 60) {
                        min2 = 0;
                        hour2++;
                    }
                }
                //深睡
                if ('1'==MainActivity.sSleepBuffer.charAt(i)) {
                    yVals1.add(new BarEntry(k, 20));
                    sleeptime += 10;
                    min3 += 10;
                    flag3 += 10;
                    if (min3 == 60) {
                        min3 = 0;
                        hour3++;
                    }
                }
            }

            quality = (flag2 + flag3) / (flag1 + flag2 + flag3);
            Log.i("Main", "睡眠质量" + quality);
            Log.i("Main", "睡眠总时间" + sleeptime);
            setData6();

            mSleepWakeTimeTextView.setText("清醒时间：\t\t" + hour1 + "\t小时\t" + min1 + "\t分钟");
            mSleepShallowTimeTextView.setText("浅睡时间：\t\t" + hour2 + "\t小时\t" + min2 + "\t分钟");
            mSleepDeepTimeTextView.setText("深睡时间：\t\t" + hour3 + "\t小时\t" + min3 + "\t分钟");

            if (k > 1) {
                mSleepQuality.setText("睡眠质量：\n" + String.valueOf(quality * 100 - 11).substring(0, 4) + "%");
                if (MainActivity.DREAM != -1) {
                    mDreamTest.setText("美梦测试\n" + "Bad");
                } else {
                    mDreamTest.setText("美梦测试\n" + "Good");
                }
                mSleepTotalTime.setText("睡眠总时间\n" + (int) (sleeptime / 60) + "小时" + (int) (sleeptime % 60)
                        + "分钟");
            } else {
                mSleepQuality.setText("睡眠质量\n" + "暂无数据" + "%");
                mDreamTest.setText("美梦测试\n" + "暂无数据");
                mSleepTotalTime.setText("睡眠总时间\n" + "暂无数据");
            }

            hour1 = 0;
            min1 = 0;
            hour2 = 0;
            min2 = 0;
            hour3 = 0;
            min3 = 0;
            sleeptime = 0;

            mHandler4.postDelayed(runnable, 10 * 1000); //5分钟更新一次
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        mHandler4.removeCallbacks(runnable);
    }

    @Override
    public void onStop() {
        super.onStop();
        MainActivity.RECEIVER_MODE = 30;
    }


}
