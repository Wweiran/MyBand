package com.example.administrator.myband.fragment;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.myband.R;
import com.example.administrator.myband.activity.MainActivity;
import com.example.administrator.myband.activity.SphyListFileActivity;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.FillFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/4/14.
 */

public class SphyFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "SPHYFragment";
    private static final int HISTORY2_OK = 12;


    private static String key = "key";
    private static int mFlag;
    SphyFragmentOnClickListener mSphyFragmentOnClickListener;

    public static SphyFragment newInstance(int index) {
        SphyFragment fm = new SphyFragment();
        Bundle args = new Bundle();
        args.putInt(key, index);
        fm.setArguments(args);
        mFlag = index;
        return fm;
    }


    public interface SphyFragmentOnClickListener {

        void onSendWaveButtonClickListener(Fragment fragment);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mSphyFragmentOnClickListener = (SphyFragmentOnClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement SphyFragmentOnClickListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public static int SPHY_CLICKCOUNT = 0;
    public static int PULSE_CLICKCOUNT = 0;

    private LineChart mChart2;
    private PieChart mChart3;

    private Button mSend, mHistory2;
    private TextView mPulseTextView;
    private String mUploadSPHYChartName;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View view = inflater.inflate(R.layout.fragment_sphy, container, false);

        //用于显示当前的脉搏
        mPulseTextView = (TextView) view.findViewById(R.id.pulse);
        mPulseTextView.setText("脉搏：\t" + "");

        //这个是显示脉搏的线图
        mChart2 = (LineChart) view.findViewById(R.id.chart2);
        mChart2.setBackgroundColor(Color.rgb(150, 200, 200));
        mSend = (Button) view.findViewById(R.id.startsend2);
        mHistory2 = (Button) view.findViewById(R.id.history2);

        mSend.setOnClickListener(this);
        mHistory2.setOnClickListener(this);

        // no description text
        mChart2.setDescription("");
        mChart2.setNoDataTextDescription("目前暂无数据，请选择相应文件来显示数据");

        // enable touch gestures
        mChart2.setTouchEnabled(true);

        mChart2.setDragDecelerationFrictionCoef(0.9f);

        // enable scaling and dragging
        mChart2.setDragEnabled(true);
        mChart2.setScaleEnabled(true);
        //mChart2.setDrawGridBackground(false);
        //mChart2.setHighlightPerDragEnabled(true);


        // if disabled, scaling can be done on x- and y-axis separately
        //mChart2.setPinchZoom(false);
        mChart2.setPinchZoom(true);

        mChart2.setDrawGridBackground(false);
        mChart2.setMaxHighlightDistance(300);

        XAxis x = mChart2.getXAxis();
        x.setEnabled(false);

        YAxis y = mChart2.getAxisLeft();
        //y.setTypeface(mTfLight);
        y.setLabelCount(6, false);
        y.setTextColor(Color.WHITE);
        y.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        y.setDrawGridLines(false);
        y.setAxisLineColor(Color.WHITE);

        mChart2.getAxisRight().setEnabled(false);

        // add data
        //setData(45, 100);

        mChart2.getLegend().setEnabled(false);

        mChart2.animateXY(2000, 2000);

        // dont forget to refresh the drawing


        //上面的代码是关于设置脉搏曲线的Chart
        //下面的代码是设置扇形的Chart
        mChart3 = (PieChart) view.findViewById(R.id.chart3);
        mChart3.setBackgroundColor(Color.WHITE);

        //moveOffScreen();

        mChart3.setUsePercentValues(true);
        mChart3.setDescription("");

        //mChart3.setCenterTextTypeface(mTfLight);
        mChart3.setCenterText(generateCenterSpannableText());

        mChart3.setDrawHoleEnabled(true);
        mChart3.setHoleColor(Color.WHITE);

        mChart3.setTransparentCircleColor(Color.WHITE);
        mChart3.setTransparentCircleAlpha(110);

        mChart3.setHoleRadius(58f);
        mChart3.setTransparentCircleRadius(61f);

        mChart3.setDrawCenterText(true);

        mChart3.setRotationEnabled(false);
        mChart3.setHighlightPerTapEnabled(true);

        mChart3.setMaxAngle(180f); // HALF CHART
        mChart3.setRotationAngle(180f);
        mChart3.setCenterTextOffset(0, -20);

        setData3(MainActivity.SPHY_CONCEN);

        mChart3.animateY(1400, Easing.EasingOption.EaseInOutQuad);

        Legend l = mChart3.getLegend();
        l.setPosition(Legend.LegendPosition.ABOVE_CHART_CENTER);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        // entry label styling
        mChart3.setEntryLabelColor(Color.WHITE);
        //mChart3.setEntryLabelTypeface(mTfRegular);
        mChart3.setEntryLabelTextSize(12f);


        PULSE_CLICKCOUNT = 0;
        SPHY_CLICKCOUNT = 0;

        //Log.i("Main sphy", "resume之后" + MainActivity.mBuffer2.toString());
        mSphyHandler.postDelayed(mPPMRunnable,2000);
        return view;
    }


    //加上一个定时更新View的handler
    //下面的代码用于自动刷新页面数据
    private Handler mSphyHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };

    public Runnable mPPMRunnable = new Runnable() {
        @Override
        public void run() {
            setData3(MainActivity.SPHY_CONCEN);
            mChart3.invalidate();
            //Log.i("MainSport", "更新UI");
            mPulseTextView.setText("脉搏：" + MainActivity.PULSECOUNT);
            MainActivity.sendFlag('e');
            //延迟3000毫秒之后重复执行runnable
            mSphyHandler.postDelayed(mPPMRunnable, 3000);



        }
    };

    private SpannableString generateCenterSpannableText() {
        SpannableString s = new SpannableString("您的血氧浓度值");
        s.setSpan(new RelativeSizeSpan(1.7f), 0, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), 0, s.length(), 0);
        return s;
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
                Toast.makeText(getActivity(), "暂未找到数据，请先测试", Toast.LENGTH_SHORT).show();
                return;
            }
            while ((lineText = bufferedReader.readLine()) != null) {
                float val;
                try {
                    val = Integer.parseInt(lineText);
                } catch (Exception e) {
                    Log.i(TAG, "setData: 解析错误");
                    continue;
                }
                values.add(new Entry(i, (220 - val)));
                i++;
                //Log.i("Main3", "数据读取成功");
            }
            inputStreamReader.close();
            inStream.close();
            //mSecondTextView.setText(stream.toString());
            //Toast.makeText(Test1Activity.this, "读取成功", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            return;
        }


        LineDataSet set1;

        if (mChart2.getData() != null &&
                mChart2.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) mChart2.getData().getDataSetByIndex(0);
            set1.setValues(values);
            mChart2.getData().notifyDataChanged();
            mChart2.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values, "DataSet 1");

            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set1.setCubicIntensity(0.2f);
            //set1.setDrawFilled(true);
            set1.setDrawCircles(false);
            set1.setLineWidth(1.8f);
            set1.setCircleRadius(4f);
            set1.setCircleColor(Color.WHITE);
            set1.setHighLightColor(Color.rgb(244, 117, 117));
            set1.setColor(Color.WHITE);
            set1.setFillColor(Color.WHITE);
            set1.setFillAlpha(100);
            set1.setDrawHorizontalHighlightIndicator(false);
            set1.setFillFormatter(new FillFormatter() {
                @Override
                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                    return -10;
                }
            });

            // create a data object with the datasets
            LineData data = new LineData(set1);
            //data.setValueTypeface(mTfLight);
            data.setValueTextSize(9f);
            data.setDrawValues(false);

            // set data
            mChart2.setData(data);

        }
    }


    public void setData3(String concen) {

        ArrayList<PieEntry> values3 = new ArrayList<PieEntry>();

        //for (int i = 0; i < count; i++) {
        values3.add(new PieEntry(Integer.parseInt(MainActivity.SPHY_CONCEN), "血氧浓度"));
        values3.add(new PieEntry((int) (100 - Integer.parseInt(MainActivity.SPHY_CONCEN))));
        //}

        PieDataSet dataSet = new PieDataSet(values3, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        //dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.rgb(238, 59, 59));
        //data.setValueTypeface(mTfLight);
        mChart3.setData(data);

        mChart3.invalidate();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startsend2:
                mSphyFragmentOnClickListener.onSendWaveButtonClickListener(SphyFragment.this);
                break;
            case R.id.history2:
                Intent intent_History = new Intent(getActivity(), SphyListFileActivity.class);
                startActivityForResult(intent_History, HISTORY2_OK);
                //这里使用了startActivityForResult，其中的请求码是11，返回的resultCode，暂时定义为15
                break;

        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == SphyListFileActivity.HISTORYCHOICE2) {
            mUploadSPHYChartName = data.getExtras().getString("fileName");
            setData(mUploadSPHYChartName);
            mChart2.animateX(2400);
            mChart2.invalidate();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mSphyHandler.post(mPPMRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSphyHandler.removeCallbacks(mPPMRunnable);
    }
}
