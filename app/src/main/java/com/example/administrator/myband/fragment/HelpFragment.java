package com.example.administrator.myband.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.administrator.myband.R;

/**
 * Created by WANGWEIRAN on 2017/6/4.
 */

public class HelpFragment extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_help, null);

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("\t\t在使用各种模式之前，建议先与腕带连接好。\n\t\t使用心电与血氧模式的时候，可以通过点击按钮，获得数据。");
        return builder.create();
    }
}
