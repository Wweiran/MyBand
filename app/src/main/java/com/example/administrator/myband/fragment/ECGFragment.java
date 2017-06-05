package com.example.administrator.myband.fragment;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.administrator.myband.R;
import com.example.administrator.myband.activity.ECGListFileActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Administrator on 2017/4/14.
 */

public class ECGFragment extends Fragment implements View.OnClickListener, OnChartGestureListener,
        OnChartValueSelectedListener {

    private static final String TAG = "ECGFragment";

    private static String key = "key";
    private static int mFlag;

    public static final int HISTORY_OK = 11;

    private LineChart mChart;
    private Button mAddButton;
    private Button mHistoryButton;

    private String mChartName;

    private ECGFragmentOnClickListener mECGFragmentOnClickListener;

    public static ECGFragment newInstance(int index) {
        ECGFragment fm = new ECGFragment();
        Bundle args = new Bundle();
        args.putInt(key, index);
        fm.setArguments(args);
        mFlag = index;
        return fm;
    }


    public interface ECGFragmentOnClickListener {
        void OnSendECGWaveButtonClickListener(Fragment fragment);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mECGFragmentOnClickListener = (ECGFragmentOnClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement ECGFragmentOnClickListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ecg, container, false);


        mChart = (LineChart) view.findViewById(R.id.chart);
        mAddButton = (Button) view.findViewById(R.id.startsenddata);
        mHistoryButton = (Button) view.findViewById(R.id.history);

        mHistoryButton.setOnClickListener(this);
        mAddButton.setOnClickListener(this);
        //chart.setDrawGridBackground(true);

        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDrawGridBackground(false);

        // no description text      文字表述。当没有数据的时候，可以说明
        mChart.setDescription("心电图表");
        mChart.setNoDataTextDescription("目前暂无数据");

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        // mChart.setScaleXEnabled(true);
        // mChart.setScaleYEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.rgb(229, 229, 229));

        // x-axis limit line
        LimitLine llXAxis = new LimitLine(10f, "Index 10");
        llXAxis.setLineWidth(4f);
        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);

        XAxis xAxis = mChart.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);

        mChart.getAxisRight().setEnabled(false);

        return view;
    }


    private void setData(String str) {
        ArrayList<Entry> values = new ArrayList<Entry>();
        int i = 0;
        try {
            FileInputStream inStream = getActivity().openFileInput(str);
            InputStreamReader inputStreamReader = new InputStreamReader(inStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String lineText = null;
            if (bufferedReader.readLine() == null) {
                Toast.makeText(getActivity(), "暂未找到数据", Toast.LENGTH_SHORT).show();
                return;
            }
            while ((lineText = bufferedReader.readLine()) != null) {
                float val = Integer.parseInt(lineText);
                values.add(new Entry(i, (220 - val)));
                i++;
            }

            inputStreamReader.close();
            inStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            return;
        }

        LineDataSet set1;

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) mChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values, new Date().toString());

            // set the line to be drawn like this "- - - - - -"
            set1.enableDashedLine(10f, 5f, 0f);
            set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.BLACK);
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(7f);
            set1.setDrawFilled(true);

            if (Utils.getSDKInt() >= 18) {
                // fill drawable only supported on api level 18 and above
                Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.fade_red);
                set1.setFillDrawable(drawable);
            } else {
                set1.setFillColor(Color.BLACK);
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(set1); // add the datasets

            // create a data object with the datasets
            LineData data = new LineData(dataSets);

            // set data
            mChart.setData(data);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startsenddata:
                mECGFragmentOnClickListener.OnSendECGWaveButtonClickListener(ECGFragment.this);
                break;
            case R.id.history:
                Intent intentHistory = new Intent(getActivity(), ECGListFileActivity.class);
                startActivityForResult(intentHistory, HISTORY_OK);
                //这里使用了startActivityForResult，其中的请求码是11，返回的resultCode，暂时定义为16
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult: HomeFragment：选择心电文件之后返回");
        switch (requestCode) {
            case HISTORY_OK:
                if (resultCode == FragmentActivity.RESULT_OK) {
                    mChartName = data.getExtras().getString("fileName");
                    Log.i(TAG, "onActivityResult: mChartName" + mChartName);
                    setData(mChartName);
                    mChart.invalidate();
                    mChart.notifyDataSetChanged();
                    mChart.animateX(2500);
                }

                break;


        }
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartLongPressed(MotionEvent me) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {

    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {

    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }
}
