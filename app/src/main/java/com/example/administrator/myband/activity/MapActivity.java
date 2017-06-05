package com.example.administrator.myband.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.example.administrator.myband.R;
import com.example.administrator.myband.fragment.ExitFragment;
import com.example.administrator.myband.fragment.SaveMapFragment;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MapActivity extends AppCompatActivity implements View.OnClickListener, ExitFragment
        .ExitDialogOnClickListener, SaveMapFragment.SaveMapDialogOnClickListener {

    private static final String TAG = "MapActivity";
    private static final int REQUEST_MAP_RECORD = 1;

    private MapView mMapView;
    private BaiduMap mBaiduMap;

    //定位相关
    public LocationClient mLocationClient;
    public BDLocationListener myListener = new MyLocationListener();
    private boolean isFirstLoc = true;

    private Button mStartButton, mRecordButton;
    private TextView mLocTypeTextView, mPrecisionTextView, mLatitudeTextView, mLongitudeTextView,
            mAltitudeTextView, mSpeedTextView, mDistanceTextView, mDirectionTextView;
    private String mPrecision, mLatitude, mLongitude,
            mAltitude, mSpeed, mDistance = "0.0", mDirection;

    private List<LatLng> lines = new ArrayList<>();
    private List<LatLng> mRecordLatLngs = null;
    private List<BitmapDescriptor> textureList = new ArrayList<>();
    private List<Integer> textureIndexs = new ArrayList<>();
    Polyline mPolyline;

    BitmapDescriptor mBlueTexture = BitmapDescriptorFactory.fromAsset("icon_road_blue_arrow.png");
    private int polyWidth;
    private String mDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //初始化TextView
        mLocTypeTextView = (TextView) findViewById(R.id.tv_location_type);
        mPrecisionTextView = (TextView) findViewById(R.id.tv_precision);
        mLatitudeTextView = (TextView) findViewById(R.id.tv_latitude);
        mLongitudeTextView = (TextView) findViewById(R.id.tv_longitude);
        mAltitudeTextView = (TextView) findViewById(R.id.tv_altitude);
        mSpeedTextView = (TextView) findViewById(R.id.tv_speed);
        mDistanceTextView = (TextView) findViewById(R.id.tv_distance);
        mDirectionTextView = (TextView) findViewById(R.id.tv_direction);

        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.baidu_map_view);
        mBaiduMap = mMapView.getMap();

        //开启交通图
//        mBaiduMap = mMapView.getMap();
//        mBaiduMap.setTrafficEnabled(true);
        //开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        //进行配置
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(MyLocationConfiguration
                .LocationMode.FOLLOWING, true, null));
        //定位初始化
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);
        option.setCoorType("bd09ll");//设置定位坐标系
        option.setScanSpan(3500);//设置发起定位请求的间隔需要大于等于1000ms才是有效的
        mLocationClient.setLocOption(option);
        mLocationClient.start();

        mBaiduMap.setOnPolylineClickListener(new BaiduMap.OnPolylineClickListener() {
            @Override
            public boolean onPolylineClick(Polyline polyline) {
                if (polyline == mPolyline) {
                    polyline.setWidth(polyWidth + 12);
                } else {
                    polyline.setWidth(polyWidth);
                }
                return false;
            }
        });

        /*
        路径纹理设置
         */
        textureList.add(mBlueTexture);
        textureIndexs.add(0);
        mStartButton = (Button) findViewById(R.id.btn_start);
        mStartButton.getBackground().setAlpha(100);
        mStartButton.setAlpha(0);
        mStartButton.setOnClickListener(this);
        mRecordButton = (Button) findViewById(R.id.btn_map_record);
        mRecordButton.getBackground().setAlpha(120);
        mRecordButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                if (mStartButton.getText().equals("开始")) {
                    mStartButton.setText("停止");
                } else if (mStartButton.getText().equals("停止")) {
                    //如果跑步距离小于300米，则提示用户数据无法保存，是否继续跑
                    //距离大于300米的时候则询问是否保存本次跑步数据
                    /**
                     * 当距离不够的时候，不进行保存，在距离超过300米后，才保存记录
                     * 使用XmlSerializer将数据保存起来，保存的格式为xml
                     */
                    if (Double.parseDouble(mDistance) < 300) {
                        new ExitFragment().show(getSupportFragmentManager(), "ExitFragment");
                    } else {
                        new SaveMapFragment().show(getSupportFragmentManager(), "SaveMapFragment");
                    }



                    /**
                     * 解析xml文件
                     */
                    /*try {
                        File file = new File(getFilesDir(), mDate + ".xml");
                        Toast.makeText(this, "file", Toast.LENGTH_SHORT).show();
                        FileInputStream fileInputStream = new FileInputStream(file);
                        XmlPullParser xmlPullParser = Xml.newPullParser();
                        Toast.makeText(this, "xpp", Toast.LENGTH_SHORT).show();
                        xmlPullParser.setInput(fileInputStream, "utf-8");
                        int type = xmlPullParser.getEventType();
                        Toast.makeText(this, "type", Toast.LENGTH_SHORT).show();
                        while (type != XmlPullParser.END_DOCUMENT) {

                            if (xmlPullParser.getEventType() == XmlPullParser.START_TAG) {
                                if ("maps".equals(xmlPullParser.getName())) {
                                    Toast.makeText(this, String.valueOf(xmlPullParser.getAttributeCount()),
                                            Toast.LENGTH_SHORT).show();
                                    Toast.makeText(this, xmlPullParser.getAttributeValue(0), Toast
                                            .LENGTH_SHORT).show();
                                }
                            }

                            type = xmlPullParser.next();
                        }
                    } catch (XmlPullParserException | IOException e) {
                        e.printStackTrace();
                    }*/
                }
                break;
            case R.id.btn_map_record:

                Intent recordIntent = new Intent(MapActivity.this, MapRecordListActivity.class);
                startActivityForResult(recordIntent, REQUEST_MAP_RECORD);
                /**
                 * 读取sd卡的内存大小
                 */
               /* Toast.makeText(this, "总" + Formatter.formatFileSize(MapActivity.this, Environment
                        .getExternalStorageDirectory().getTotalSpace()) + "\n可用" + Formatter.formatFileSize
                        (MapActivity.this, Environment.getExternalStorageDirectory().getUsableSpace()) +
                        "\n空闲" + Formatter.formatFileSize(MapActivity.this, Environment
                        .getExternalStorageDirectory().getFreeSpace()), Toast.LENGTH_SHORT).show();*/

//                try {
//                    /**
//                     * 方式一
//                     */
//                    /*Toast.makeText(this, "xie", Toast.LENGTH_SHORT).show();
//                    String file = Environment.getExternalStorageDirectory().getPath()+"/test2.txt";
//                    FileOutputStream fileOutputStream = new FileOutputStream(file);
//                    fileOutputStream.write("sds".getBytes());
//                    fileOutputStream.flush();
//                    fileOutputStream.close();
//                    Toast.makeText(this, "chenggong", Toast.LENGTH_SHORT).show();*/
//                    /**
//                     * 数据保存到sd卡中
//                     */
//                    File file = new File(Environment.getExternalStorageDirectory().getPath(), "11111.txt");
//                    FileOutputStream fileOutputStream = new FileOutputStream(file);
//                    fileOutputStream.write("11111".getBytes());
//                    fileOutputStream.close();
//                    Toast.makeText(this, "chenggong", Toast.LENGTH_SHORT).show();
//
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_MAP_RECORD:
                if (resultCode == RESULT_OK) {
                    String fileName = data.getStringExtra("fileName");
                    mRecordLatLngs = new ArrayList<>();
                    double lat = 0, lng = 0;
                    try {
                        File file = new File(getFilesDir(), fileName);
                        FileInputStream fileInputStream = new FileInputStream(file);
                        XmlPullParser xmlPullParser = Xml.newPullParser();
                        xmlPullParser.setInput(fileInputStream, "utf-8");
                        int type = xmlPullParser.getEventType();
                        while (type != XmlPullParser.END_DOCUMENT) {

                            switch (type) {
                                case XmlPullParser.START_TAG:
                                    if ("maps".equals(xmlPullParser.getName())) {
                                        Toast.makeText(this, "距离" + String.format("%.2f", Double
                                                .parseDouble(xmlPullParser.getAttributeValue(0))), Toast
                                                .LENGTH_SHORT).show();
                                    } else if ("map".equals(xmlPullParser.getName())) {

                                    } else if ("Lat".equals(xmlPullParser.getName())) {
                                        lat = Double.parseDouble(xmlPullParser.nextText());
                                    } else if ("Lng".equals(xmlPullParser.getName())) {
                                        lng = Double.parseDouble(xmlPullParser.nextText());
                                    }
                                    break;
                                case XmlPullParser.END_TAG:
                                    if ("map".equals(xmlPullParser.getName())) {
                                        if (lat != 0 & lng != 0) {
                                            LatLng latLng = new LatLng(lat, lng);
                                            mRecordLatLngs.add(latLng);
                                        }
                                    }
                                    break;
                            }

                            type = xmlPullParser.next();


                        }
                    } catch (XmlPullParserException | IOException e) {
                        e.printStackTrace();
                    }

                    if (mRecordLatLngs.size() > 4) {
                        mBaiduMap.clear();
                        OverlayOptions overlayOptions = new PolylineOptions()
                                .width(18)
                                .points(mRecordLatLngs)
                                .customTextureList(textureList)
                                .textureIndex(textureIndexs);
                        mBaiduMap.addOverlay(overlayOptions);
                        mRecordLatLngs.clear();
                    }

                }
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 将Fragment点击事件传递到Activity中进行处理，距离不到300米，选择了继续跑
     *
     * @param fragment 传递的是使用的Fragment
     */
    @Override
    public void onExitPositiveClick(DialogFragment fragment) {
        Toast.makeText(this, "请继续跑~", Toast.LENGTH_SHORT).show();
        mStartButton.setText("停止");
    }

    /**
     * 距离小于300米，选择了结束跑步
     *
     * @param fragment
     */
    @Override
    public void onExitNegativeClick(DialogFragment fragment) {
        mStartButton.setText("开始");
        mDistance = "0.0";
        lines.clear();
    }

    /**
     * 将Fragment点击事件传递到Activity中进行处理，距离超过了300米，对轨迹进行保存
     *
     * @param fragment 传递的是SaveMapFragment
     */
    @Override
    public void onSaveMapPositiveOnClick(SaveMapFragment fragment) {
        try {
            mDate = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM", new Date());
            Toast.makeText(this, mDate, Toast.LENGTH_SHORT).show();
            XmlSerializer xmlSerializer = Xml.newSerializer();
            File file = new File(getFilesDir(), mDate + ".xml");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            xmlSerializer.setOutput(fileOutputStream, "utf-8");
            //start文档标签
            xmlSerializer.startDocument("utf-8", true);
            //最外层标签以及属性
            xmlSerializer.startTag(null, "maps");
            xmlSerializer.attribute(null, "distance", mDistance);
            for (int i = 0; i < lines.size(); i++) {
                //第二层标签
                xmlSerializer.startTag(null, "map");

                xmlSerializer.startTag(null, "Lat");
                xmlSerializer.text(String.valueOf(lines.get(i).latitude));
                xmlSerializer.endTag(null, "Lat");

                xmlSerializer.startTag(null, "Lng");
                xmlSerializer.text(String.valueOf(lines.get(i).longitude));
                xmlSerializer.endTag(null, "Lng");

                xmlSerializer.endTag(null, "map");
            }

            //最外层计数标签
            xmlSerializer.endTag(null, "maps");
            xmlSerializer.endDocument();
            fileOutputStream.close();
            Toast.makeText(this, "记录保存成功", Toast.LENGTH_SHORT).show();
            lines.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mStartButton.setText("开始");
        mDistance = "0.0";
        lines.clear();
    }

    /**
     * 距离超过300米，不保存轨迹数据
     *
     * @param fragment
     */
    @Override
    public void onSaveMapNegativeOnClick(SaveMapFragment fragment) {
        lines.clear();
        mDistance = "0.0";
        mStartButton.setText("开始");
    }

    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            switch (location.getLocType()) {
                //GPS精度定位
                case BDLocation.TypeGpsLocation:
                    mStartButton.setClickable(true);
                    mStartButton.setAlpha(1);

                    mPrecision = String.valueOf(location.getRadius());
                    mAltitude = String.valueOf(location.getAltitude());
                    mDirection = String.valueOf(location.getDirection());
                    mLatitude = String.valueOf(location.getLatitude());
                    mLongitude = String.valueOf(location.getLongitude());
                    mSpeed = String.valueOf(location.getSpeed());

                    switch (mStartButton.getText().toString()) {
                        case "开始":
                            mStartButton.setAlpha(1);
                            mStartButton.setClickable(true);
                            break;
                        case "停止":
                            LatLng p1 = new LatLng(location.getLatitude(), location.getLongitude());

                            if (lines.size() >=2) {
                                polyWidth = 12;
                                double distance = DistanceUtil.getDistance(p1, lines.get(lines.size() - 1));

                                /**
                                 * 当两个点之间的距离超过1M时，可以才进行记录
                                 */
                                if (distance > 1.0) {
                                    mDistance = String.valueOf(Double.parseDouble(mDistance) + distance);

                                    /**
                                     * 在地图上画出轨迹
                                     */
                                    mBaiduMap.clear();
                                    OverlayOptions overlayOptions = new PolylineOptions()
                                            .width(polyWidth)
                                            .points(lines)
                                            .customTextureList(textureList)
                                            .textureIndex(textureIndexs);
                                    mPolyline = (Polyline) mBaiduMap.addOverlay(overlayOptions);
                                    lines.add(p1);
                                }
                            } else {
                                lines.add(p1);
                            }

                            MyLocationData locData = new MyLocationData.Builder()
                                    .accuracy(location.getRadius())
                                    // 此处设置开发者获取到的方向信息，顺时针0-360
                                    .direction(location.getDirection())
                                    .latitude(location.getLatitude())
                                    .longitude(location.getLongitude())
                                    .build();
                            mBaiduMap.setMyLocationData(locData);
                            break;
                    }


                    mLocTypeTextView.setText("定位类型：GPS定位" + "  " + location.getSatelliteNumber());
                    mPrecisionTextView.setText("定位精度：" + mPrecision);
//                    mDistanceTextView.setText("距离：" + mDistance);
                    mDistanceTextView.setText("距离：" + String.format("%.2f", Double.parseDouble(mDistance)));
                    mAltitudeTextView.setText("高度：" + mAltitude);
                    mDirectionTextView.setText("方向信息：" + mDirection);
                    mLatitudeTextView.setText("纬度：" + mLatitude);
                    mLongitudeTextView.setText("经度：" + mLongitude);
                    mSpeedTextView.setText("速度：" + mSpeed);
                    break;

                //网络定位
                case BDLocation.TypeNetWorkLocation:
                    mStartButton.setText("开始");
                    mStartButton.setClickable(false);
                    mStartButton.setAlpha(0);
                    mLocTypeTextView.setText("定位类型：网络定位");
                    mPrecisionTextView.setText("定位精度：" + location.getRadius());
                    mDistanceTextView.setText("距离：" + location.getAddrStr());
                    mAltitudeTextView.setText("高度：" + "网络定位无高度信息");
                    mDirectionTextView.setText("方向信息：" + location.getDirection());
                    mLatitudeTextView.setText("纬度：" + location.getLatitude());
                    mLongitudeTextView.setText("经度：" + location.getLongitude());
                    mSpeedTextView.setText("速度：" + location.getSpeed());
                    break;

                default:
                    break;
            }

            if (isFirstLoc) {
                isFirstLoc = false;
                //第一次定位的时候，先确定目前的位置
                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(location.getRadius())
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(location.getDirection())
                        .latitude(location.getLatitude())
                        .longitude(location.getLongitude())
                        .build();
                mBaiduMap.setMyLocationData(locData);

                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();


    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
    }


}
