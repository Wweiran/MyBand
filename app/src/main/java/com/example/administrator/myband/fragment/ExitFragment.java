package com.example.administrator.myband.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.example.administrator.myband.R;

/**
 * Created by WANGWEIRAN on 2017/5/28.
 */

public class ExitFragment extends DialogFragment {

    ExitDialogOnClickListener mDialogOnClickListener;

    public interface ExitDialogOnClickListener {
        void onExitPositiveClick(DialogFragment fragment);

        void onExitNegativeClick(DialogFragment fragment);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mDialogOnClickListener = (ExitDialogOnClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement ExitDialogOnClickListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.notice)
                .setMessage(R.string.dialog_message_run)
                .setPositiveButton("继续跑", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDialogOnClickListener.onExitPositiveClick(ExitFragment.this);
                    }
                })
                .setNegativeButton("累了，退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDialogOnClickListener.onExitNegativeClick(ExitFragment.this);
                    }
                });
        return builder.create();
    }
}
