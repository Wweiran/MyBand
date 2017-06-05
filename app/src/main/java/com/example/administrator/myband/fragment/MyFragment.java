package com.example.administrator.myband.fragment;


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

public class MyFragment extends Fragment {
    private static String key = "key";
    private static int mFlag;
    private View view;
    private TextView tv;


    public static MyFragment newInstance( int index){
        MyFragment fm = new MyFragment();
        Bundle args = new Bundle();
        args.putInt(key,index);
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
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
        view = inflater.inflate(R.layout.content_main,container,false);
        tv = (TextView) view.findViewById(R.id.tv_data);
       switch(mFlag){
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
}
