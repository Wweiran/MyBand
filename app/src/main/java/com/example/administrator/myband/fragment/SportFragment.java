package com.example.administrator.myband.fragment;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.administrator.myband.R;

/**
 * Created by Administrator on 2017/4/14.
 */

public class SportFragment extends Fragment {
    private static String key = "key";
    private static int mFlag;
    private View view;
    public TextView mSportSpeedTV, mSportTemperatureTV, mSportUVTV, mSportStepsTV, mSportTotalTV;


    public static SportFragment newInstance(int index) {
        SportFragment fm = new SportFragment();
        Bundle args = new Bundle();
        args.putInt(key, index);
        fm.setArguments(args);
        mFlag = index;
        return fm;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle
            savedInstanceState) {
        if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.

            return null;

        }
        view = inflater.inflate(R.layout.fragment_sport, container, false);
        mSportSpeedTV = (TextView) view.findViewById(R.id.tv_sport_speed);
        mSportTemperatureTV = (TextView) view.findViewById(R.id.tv_sport_temperature);
        mSportUVTV = (TextView) view.findViewById(R.id.tv_sport_uv);
        mSportStepsTV = (TextView) view.findViewById(R.id.tv_sport_steps);
        mSportTotalTV = (TextView) view.findViewById(R.id.tv_sport_total);
        //        tv = (TextView) view.findViewById(R.id.tv_homepage);

        mSportSpeedTV.setText(getResources().getString(R.string.tv_sport_fore_speed));
        mSportTemperatureTV.setText(getResources().getString(R.string.tv_sport_fore_temperature));
        mSportUVTV.setText(getResources().getString(R.string.tv_sport_fore_uv));
        mSportStepsTV.setText(getResources().getString(R.string.tv_sport_fore_steps));
        mSportTotalTV.setText(getResources().getString(R.string.tv_sport_fore_total));

        switch (mFlag) {
            case 1:

                break;
            case 2:

                break;
            case 3:

                break;
            case 4:
                break;
        }

        return view;
    }

    @SuppressLint("SetTextI18n")
    public void setSportSpeedTV(String data) {
        mSportSpeedTV.setText(getResources().getString(R.string.tv_sport_fore_speed) + data + "KM/H");
    }

    @SuppressLint("SetTextI18n")
    public void setSportTemperatureTV(String data) {
        mSportTemperatureTV.setText(getResources().getString(R.string.tv_sport_fore_temperature) + data +
                "℃");
    }

    @SuppressLint("SetTextI18n")
    public void setSportUVTV(String data) {
        mSportUVTV.setText(getResources().getString(R.string.tv_sport_fore_uv) + data);
    }

    @SuppressLint("SetTextI18n")
    public void setSportStepsTV(String data) {
        mSportStepsTV.setText(getResources().getString(R.string.tv_sport_fore_steps) + data + "步");
    }


    public void setSportTotalTV(String data) {
        mSportTotalTV.setText(getResources().getString(R.string.tv_sport_fore_total) + data + "步");
    }
}
