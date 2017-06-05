package com.example.administrator.myband.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.administrator.myband.R;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by WANGWEIRAN on 2016/8/4.
 * 处理心电数据
 */
public class ECGListFileActivity extends ListActivity {

    private static final String TAG = "Test_ListFile_Activity";

    private List<String> items = null;//存放名称
    private List<String> paths = null;//存放路径
    private TextView tv;


    public static final int HISTORYCHOICE = 16;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listfiles);
        tv = (TextView) this.findViewById(R.id.TextView);
        this.getFileDir(getFilesDir().getPath());//获取rootPath目录下的文件.
    }

    public void getFileDir(String filePath) {
        try {
            this.tv.setText("选择下面的文件查看波形");// 设置当前所在路径
            this.tv.setTextSize(25);
            items = new ArrayList<String>();
            paths = new ArrayList<String>();
            File f = new File(filePath);
            File[] files = f.listFiles(getFileExtensionFilter("-xin.txt"));// 列出所有文件

            if (files != null) {
                int count = files.length;// 文件个数
                for (int i = 0; i < count; i++) {
                    File file = files[i];
                    items.add(file.getName());
                    paths.add(file.getPath());
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, items);
            this.setListAdapter(adapter);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    public static FilenameFilter getFileExtensionFilter(String extension) {
        final String _extension = extension;
        return new FilenameFilter() {
            public boolean accept(File file, String name) {
                boolean ret = name.endsWith(_extension);
                return ret;
            }
        };
    }

//    File file = new File("c:\\");
//    File[] zipFiles = file.listFiles(getFileExtensionFilter(".zip"));


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        String path = paths.get(position);
        File file = new File(path);
        //如果是文件夹就继续分解
        if (file.isDirectory()) {
            this.getFileDir(path);
        } else {
            //Toast.makeText(Test_ListFile.this, "点击了文件" + file.getName(), Toast.LENGTH_SHORT).show();
            Intent getNameIntent = new Intent();
            getNameIntent.putExtra("fileName", file.getName());
            setResult(RESULT_OK, getNameIntent);
            Log.i(TAG, "onListItemClick: 脉搏文件选择确定" + file.getName());
            finish();
        }
    }

}