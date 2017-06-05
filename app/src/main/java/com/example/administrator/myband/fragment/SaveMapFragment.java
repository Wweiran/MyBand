package com.example.administrator.myband.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.example.administrator.myband.R;

/**
 * Created by WANGWEIRAN on 2017/5/29.
 */

public class SaveMapFragment extends DialogFragment {

    SaveMapDialogOnClickListener mSaveMapDialogOnClickListener;

    public interface SaveMapDialogOnClickListener {
        void onSaveMapPositiveOnClick(SaveMapFragment fragment);

        void onSaveMapNegativeOnClick(SaveMapFragment fragment);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mSaveMapDialogOnClickListener = (SaveMapDialogOnClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement ExitDialogOnClickListener");
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.notice)
                .setMessage(R.string.dialog_message_save)
                .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "保存", Toast.LENGTH_SHORT).show();
                        mSaveMapDialogOnClickListener.onSaveMapPositiveOnClick(SaveMapFragment.this);
                    }
                })
                .setNegativeButton("算了，不", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSaveMapDialogOnClickListener.onSaveMapNegativeOnClick(SaveMapFragment.this);
                    }
                });

        return builder.create();
    }


}
