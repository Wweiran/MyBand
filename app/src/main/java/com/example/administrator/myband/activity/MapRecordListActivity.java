package com.example.administrator.myband.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.administrator.myband.R;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class MapRecordListActivity extends ListActivity {

    private List<String> items = null;//存放名称
    private List<String> paths = null;//存放路径
    private TextView tv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_record);
        tv = (TextView) this.findViewById(R.id.TextView);
        getFileDir(getFilesDir().getPath());//获取file目录下的文件.
//        Toast.makeText(this, getFilesDir().getPath(), Toast.LENGTH_SHORT).show();
    }

    public void getFileDir(String filePath) {
        try {
            this.tv.setText("选择下面的文件查看历史记录");// 设置当前所在路径
            this.tv.setTextSize(20);
            items = new ArrayList<String>();
            paths = new ArrayList<String>();
            File f = new File(filePath);
            File[] files = f.listFiles(getFileExtensionFilter(".xml"));// 列出所有文件
            // 将所有文件存入list中
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
            setListAdapter(adapter);
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

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        String path = paths.get(position);
        File file = new File(path);
        //如果是文件夹就继续分解
        if (file.isDirectory()) {
            this.getFileDir(path);
        } else {
            Intent getNameIntent = new Intent();
            getNameIntent.putExtra("fileName", file.getName());
            setResult(RESULT_OK, getNameIntent);
            finish();
        }
    }
}
