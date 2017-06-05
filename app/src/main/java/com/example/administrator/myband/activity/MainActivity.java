package com.example.administrator.myband.activity;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.administrator.myband.R;
import com.example.administrator.myband.fragment.ECGFragment;
import com.example.administrator.myband.fragment.HelpFragment;
import com.example.administrator.myband.fragment.SleepFragment;
import com.example.administrator.myband.fragment.SphyFragment;
import com.example.administrator.myband.fragment.SportFragment;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements NavigationView
        .OnNavigationItemSelectedListener, SphyFragment.SphyFragmentOnClickListener, ECGFragment
        .ECGFragmentOnClickListener {


    /**
     * 其他Fragment需要用到的数据
     */
    public static String SPHY_CONCEN = "0";        //这个定义用来引用血氧浓度的值，先假定为0
    public static String PULSECOUNT = "0";         //这个是脉搏的次数
    public static int SPHY_CONCEN_COUNT = 0;

    public static int PULSECOUNT_COUNT = 0;

    public static int DREAM = 0;
    private static final int HISTORY2_OK = 12;


    public static int RECEIVER_MODE = 31;
    private static final int RECEIVE_MODE_SPHY = 32;
    private static final int RECEIVE_MODE_ECG = 31;
    private static final int RECEIVE_MODE_SLEEP = 34;
    private static final int RECEIVE_MODE_SPORT = 33;
    /**
     * 连接状态
     */
    public static boolean CONNECTED = false;
    public static boolean ABLE_WRITE = false;


    public static boolean CONVEYING = false;
    /**
     * ENABLE用于请求打开蓝牙
     * CONNECT用于连接蓝牙
     */
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_CONNECT_DEVICE = 1;

    private static final int CONNECT_OK = 6;
    private static final int CONNECT_FAILURE = 7;

    private static final int BLUETOOTH_DISCONNECTED = 9;

    private static final String TAG = "MainActivity";

    //一个UUID用于蓝牙连接
    private static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    //用于蓝牙连接
    private BluetoothAdapter mBluetoothAdapter;
    private ConnectThread mConnectThread;


    private static ConnectedThread mConnectedThread;
    //蓝牙传输的类型
    private final int ASCII = 2;


    private int mCodeType = ASCII;
    private static final int MSG_NEW_MAINDATA = 21;
    private static final int MSG_NEW_SPHY = 22;
    private static final int MSG_NEW_SPORT = 23;
    private static final int MSG_NEW_SLEEP = 24;


    private static final int MSG_NEW_ECG = 3;

    public static final String EXTRA_RECEIVED_MODE = "com.jarek.wechatdemo.receive";




    SportFragment mf_sport = SportFragment.newInstance(1);
    SleepFragment mf_sleep = SleepFragment.newInstance(2);
    ECGFragment mf_ecg = ECGFragment.newInstance(3);
    SphyFragment mf_sphy = SphyFragment.newInstance(4);

    // 定义一个变量，来标识是否退出
    private static boolean isExit = false;
    private static boolean isFirstToSphy = true;

    List<Integer> mWaveIntegers;
    List<Integer> mECGIntegers;

    int mWaveTimes = 0;
    int mECGWaveTimes = 0;


    public static StringBuffer sSleepBuffer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //查看本机是否支持蓝牙设备，不支持则mBluetoothAdapter=null
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "本机未找到蓝牙设备", Toast.LENGTH_LONG).show();
            finish();
        }


        mWaveIntegers = new ArrayList<>();
        mECGIntegers = new ArrayList<>();
        sSleepBuffer = new StringBuffer();
    }

    /**
     * 使用SpannableString设置样式——字体颜色
     */
    /*public void setTvECG(String data) {
        SpannableString spannableString = new SpannableString("心电数据：" + data);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#009ad6"));
        spannableString.setSpan(colorSpan, 0, 5, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        ((TextView) findViewById(R.id.tv)).setText(spannableString);
    }

    public void setTvSPHY(String data) {
        SpannableString spannableString = new SpannableString("血氧数据：" + data);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#009ad6"));
        spannableString.setSpan(colorSpan, 0, 5, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        ((TextView) findViewById(R.id.tv_homepage)).setText(spannableString);
    }

    public void setTvSleep(String data) {
        SpannableString spannableString = new SpannableString("睡眠数据：" + data);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#009ad6"));
        spannableString.setSpan(colorSpan, 0, 5, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        ((TextView) findViewById(R.id.tv_homepage)).setText(spannableString);
    }

    public void setTvSport(String data) {

        SpannableString spannableString = new SpannableString("运动数据：\n" + data);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#009ad6"));
        spannableString.setSpan(colorSpan, 0, 5, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        ((TextView) findViewById(R.id.tv_homepage)).setText(spannableString);
    }*/
    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, mf_ecg)
                .commit();
        RECEIVER_MODE = RECEIVE_MODE_ECG;//心电模式


    }

    /**
     * 后面的蓝牙连接与传输，使用的是Socket
     * This thread runs while attempting to make an outgoing connection with a
     * device. It runs straight through; the connection either succeeds or
     * fails.
     */
    @Override
    protected void onStart() {
        super.onStart();

        if (!mBluetoothAdapter.isEnabled()) {//应用程序开启时检测手机是否开启蓝牙
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);//申请开启蓝牙权限
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                break;
            case R.id.action_help:
                new HelpFragment().show(getSupportFragmentManager(), "HelpFragment");
                break;
            case R.id.action_map_view:
                Intent mapIntent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(mapIntent);
                break;
            case R.id.action_open_bluetooth:
                if (!mBluetoothAdapter.isEnabled()) {//未打开蓝牙，用intent打开蓝牙
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                if (mBluetoothAdapter.enable()) {//用系统API打开蓝牙
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_sport:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, mf_sport)
                        .commit();
                if (!CONNECTED) {
                    Toast.makeText(this, "请先连接蓝牙", Toast.LENGTH_SHORT).show();
                } else {
                    RECEIVER_MODE = RECEIVE_MODE_SPORT;//运动模式
                    sendFlag('a');//发送a
                }
                isFirstToSphy = true;
                break;

            case R.id.nav_sleep:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, mf_sleep)
                        .commit();
                if (!CONNECTED) {
                    Toast.makeText(this, "请先连接蓝牙", Toast.LENGTH_SHORT).show();
                } else {
                    RECEIVER_MODE = RECEIVE_MODE_SLEEP;//34为睡眠模式

                }
                isFirstToSphy = true;
                break;
            case R.id.nav_ecg:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, mf_ecg)
                        .commit();
                if (!CONNECTED) {
                    Toast.makeText(this, "请先连接蓝牙", Toast.LENGTH_SHORT).show();
                } else {
                    RECEIVER_MODE = RECEIVE_MODE_ECG;//31为心电模式
                    //sendFlag('a');//3代表发送a
                }
                isFirstToSphy = true;
                break;

            case R.id.nav_sphy:
                isFirstToSphy = true;
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, mf_sphy)
                        .commit();
                if (!CONNECTED) {
                    Toast.makeText(this, "请先连接蓝牙", Toast.LENGTH_SHORT).show();
                } else {
                    RECEIVER_MODE = RECEIVE_MODE_SPHY;//32为血氧模式
                    sendFlag('d');//发送d，让运动模式的数据停止发送。此时会回传120个波形数据。
                }
                break;

            default:
                break;
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * 查看连接蓝牙后返回的请求码和结果码
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT://请求打开蓝牙的result
                if (resultCode == Activity.RESULT_OK) {//已经成功打开蓝牙
                    // Bluetooth is now enabled Launch the DeviceListActivity to see devices and do scan
                    Intent serverIntent = new Intent(this, DeviceListActivity.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);//跳转到蓝牙设备列表，请求码设置为
                    // REQUEST_CONNECT_DEVICE
                } else {
                    //没有打开蓝牙
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "蓝牙打开失败，请打开蓝牙后重启程序", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case REQUEST_CONNECT_DEVICE://从蓝牙连接的activity返回mainactivity
                if (resultCode != Activity.RESULT_OK) {//如没有连接成功则返回
                    return;
                } else {//配对蓝牙设备成功，得到蓝牙设备地址并开启后台线程连接蓝牙
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    connect(device);
                }
                break;
        }
    }


    //用于处理接收到的有关心电数据————mECGHandler
    private Handler mECGHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_NEW_ECG:      //这里是心电的接收数据
                    try {
                        ArrayList<Integer> ecglist = (ArrayList<Integer>) msg.obj;
                        String str = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", new Date());
                        FileOutputStream outputStream = openFileOutput(str + "-xin.txt", MODE_PRIVATE);
                        for (int i = 0; i < ecglist.size(); i++) {
                            outputStream.write((ecglist.get(i) + "\n").getBytes());
                        }
                        outputStream.close();
                        Toast.makeText(MainActivity.this, "数据保存成功", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "文件写入成功");
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "数据传输失败，请稍后重试", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "文件写入失败");
                    } finally {
                        mECGIntegers.clear();
                    }
                    break;

                case CONNECT_OK:
                    CONNECTED = true;
                    Toast.makeText(MainActivity.this, "蓝牙连接成功", Toast.LENGTH_SHORT).show();
                    break;
                case CONNECT_FAILURE:
                    if (CONNECTED) {
                        Toast.makeText(MainActivity.this, "蓝牙已经连接,毋须再次连接", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "蓝牙连接失败，请稍后重启蓝牙", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case BLUETOOTH_DISCONNECTED:
                    CONNECTED = false;
                    NotificationManager manager = (NotificationManager) getSystemService
                            (NOTIFICATION_SERVICE);
                    NotificationCompat.Builder notification = new NotificationCompat.Builder(MainActivity
                            .this);
                    notification.setSmallIcon(R.mipmap.ic_launcher);
                    notification.setContentTitle("连接异常");
                    notification.setContentText("蓝牙连接已断开");
                    notification.setAutoCancel(true);        //点击自动消失
                    notification.setDefaults(Notification.DEFAULT_ALL);            //铃声,振动,呼吸灯
                    manager.notify(2, notification.build());
                    break;
                default:
                    break;
            }
        }

    };


    //用于处理血氧脉搏的数据
    private Handler mSphyHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_NEW_SPHY:          //下面是接收血氧脉搏的数据
                    String data = (String) msg.obj;
                    StringBuffer mSphyBuffer = new StringBuffer(data);
                    synchronized (getApplicationContext()) {
                        Log.i(TAG, "handleMessage: sphy" + mSphyBuffer.toString());
                        //这里的思路是：在buf2中存放有收到的所有数据，在后面的内容中，将对应的数据取出来
                        //@XX@，@里面的数据存放在buf3中
                        for (int i = 0, j = 0; i < mSphyBuffer.length(); i++) {            //@后面带的数据是脉搏波形数据
                            if ('@' == mSphyBuffer.charAt(i) & i != (mSphyBuffer.length() - 1)) {
                                j = i;
                                j = mSphyBuffer.indexOf("@", j + 1);
                                if (j == -1) {
                                    Log.i(TAG, "脉搏波形：j = -1,退出");
                                    break;
                                }
                                Log.i(TAG, "@后面:脉搏波形" + mSphyBuffer.substring(i + 1, j));
                                mWaveIntegers.add(Integer.valueOf(mSphyBuffer.substring(i + 1, j)));
                                mWaveTimes++;
                                break;
                            }

                            if ('#' == mSphyBuffer.charAt(i) & i != (mSphyBuffer.length() - 1)) {
                                //#后面带的数据是血氧浓度的值
                                j = i;
                                j = mSphyBuffer.indexOf("#", j + 1);
                                if (j == -1) {
                                    Log.i(TAG, "j = -1,退出");
                                    break;
                                }
                                Log.i(TAG, "#后面：血氧浓度" + mSphyBuffer.substring(i + 1, j));
                                SPHY_CONCEN = mSphyBuffer.substring(i + 1, j);
                                break;
                            }

                            if ('$' == mSphyBuffer.charAt(i) & i != (mSphyBuffer.length() - 1)) {
                                //$后面带的数据是脉搏的次数
                                j = i;
                                j = mSphyBuffer.indexOf("$", j + 1);
                                if (j == -1) {
                                    Log.i(TAG, "j = -1,退出");
                                    break;
                                }
                                Log.i(TAG, "$后面：脉搏次数" + mSphyBuffer.substring(i + 1, j));
                                PULSECOUNT = mSphyBuffer.substring(i + 1, j);
                                break;
                            }
                        }
                    }

                    if (mWaveTimes == 120) {
                        mWaveTimes = 0;
                        if (isFirstToSphy) {
                            isFirstToSphy = false;
                            mWaveIntegers.clear();
                            break;
                        } else {
                            try {
                                String str = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", new Date());
                                FileOutputStream fos = openFileOutput(str + "-mai.txt",
                                        MODE_PRIVATE);
                                for (int i = 0; i < mWaveIntegers.size(); i++) {
                                    fos.write((mWaveIntegers.get(i) + "\n").getBytes());
                                }
                                fos.close();
                                Toast.makeText(MainActivity.this, "数据接收成功", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(MainActivity.this, "数据接收失败，请稍后重试", Toast.LENGTH_SHORT).show();
                            } finally {
                                mWaveIntegers.clear();
                            }
                        }
                    }
                    break;
            }
        }
    };


    //用于处理运动模式下的数据
    private Handler mSportHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_NEW_SPORT:             //这里是在运动模式中，处理收到的数据
                    StringBuffer buf6 = new StringBuffer((String) msg.obj);
                    synchronized (getApplicationContext()) {
                        //用*后面的参数来存运动时的数据，如*120*
                        for (int i = 0, j = 0; i < buf6.length(); i++) {
                            if ('*' == buf6.charAt(i) && i != (buf6.length() - 1)) {
                                j = i;
                                j = buf6.indexOf("*", j + 1);
                                if (j == -1) {
                                    Log.i(TAG, "运动时步数.j = -1,*");
                                    break;
                                }
                                Log.i(TAG, "*后面:运动时步数：" + buf6.substring(i + 1, j));
                                mf_sport.setSportStepsTV(buf6.substring(i + 1, j));
                                break;
                            }

                            //#123#     这里#后面带的数据为所走的总步数
                            if ('#' == buf6.charAt(i) && i != (buf6.length() - 1)) {
                                j = i;
                                j = buf6.indexOf("#", j + 1);
                                if (j == -1) {
                                    Log.i(TAG, "总步数.j = -1,#");
                                    break;
                                }
                                Log.i(TAG, "#后面：总步数：" + buf6.substring(i + 1, j));
                                mf_sport.setSportTotalTV(buf6.substring(i + 1, j));
                                break;
                            }

                            // 感叹号后面的数据用于显示温度
                            if ('!' == buf6.charAt(i) && i != (buf6.length() - 1)) {
                                j = i;
                                j = buf6.indexOf("!", j + 1);
                                if (j == -1) {
                                    Log.i(TAG, "温度.j = -1,%");
                                    break;
                                }
                                Log.i(TAG, "！后面：温度：" + buf6.substring(i + 1, j));
                                try {
                                    float temp = (float) (Float.parseFloat(buf6.substring(i + 1, j)) * (3.3
                                            / 4096));
                                    temp = (float) (61.88 - 14.989 * temp);
                                    NumberFormat numberFormat = NumberFormat.getNumberInstance();
                                    numberFormat.setMaximumFractionDigits(2);
                                    mf_sport.setSportTemperatureTV(numberFormat.format(temp));
                                } catch (Exception e) {
                                    Log.i(TAG, "handleMessage: 温度解析失败");
                                }
                                break;
                            }

                            //数据用于显示运动模式下的紫外线强度
                            if ('|' == buf6.charAt(i) && i != (buf6.length() - 1)) {
                                j = i;
                                j = buf6.indexOf("|", j + 1);
                                if (j == -1) {
                                    Log.i(TAG, "紫外线强度j = -1,|");
                                    break;
                                }
                                Log.i(TAG, "|后面：紫外线强度：" + buf6.substring(i + 1, j));
                                mf_sport.setSportUVTV(buf6.substring(i + 1, j));
                                break;

                            }

                            //?后面的数据用于显示运动模式下的速度
                            if ('?' == buf6.charAt(i) && i != (buf6.length() - 1)) {
                                j = i;
                                j = buf6.indexOf("?", j + 1);
                                if (j == -1) {
                                    Log.i(TAG, "速度j = -1,?");
                                    break;
                                }
                                Log.i(TAG, "?后面：" + buf6.substring(i + 1, j));
                                try {
                                    float temp = Float.parseFloat(buf6.substring(i + 1, j)) / 10;
                                    mf_sport.setSportSpeedTV(String.valueOf(temp));
                                } catch (Exception e) {
                                    Log.i(TAG, "handleMessage: 温度解析失败");
                                }
                                break;
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }

    };


    //用于处理睡眠模式下的数据
    private Handler mSleepHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_NEW_SLEEP:         //睡眠模式中对接收到的数据进行处理
                    CONVEYING = true;
                    sSleepBuffer.append(((String) msg.obj).charAt(0));
                    Log.i(TAG, "handleMessage: static buffer" + sSleepBuffer);
                    break;
                default:
                    break;
            }
        }

    };


    @Override
    public void onSendWaveButtonClickListener(Fragment fragment) {
        isFirstToSphy = false;
        if (CONNECTED) {
            MainActivity.sendFlag('d');//发送字母d，回传血氧波形数据
        } else {
            Toast.makeText(this, "请先连接好蓝牙，以便传输数据", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void OnSendECGWaveButtonClickListener(Fragment fragment) {
        if (CONNECTED) {
            MainActivity.sendFlag('c');//发送字母c，硬件回传心电的数据
        } else {
            Toast.makeText(this, "请先连接好蓝牙，以便传输数据", Toast.LENGTH_SHORT).show();
        }
    }


    public static void sendFlag(int number) {
        switch (number) {
            case 'c':
                if (CONNECTED) {
                    byte[] c = new byte[]{99};
                    mConnectedThread.write(c);
                }
                break;
            case 'd':
                if (CONNECTED) {
                    byte[] d = new byte[]{100};
                    mConnectedThread.write(d);
                }
                break;
            case 'a':
                if (CONNECTED) {
                    byte[] a = new byte[]{97};
                    mConnectedThread.write(a);
                }
                break;
            case 'b':
                if (CONNECTED) {
                    byte[] b = new byte[]{98};
                    mConnectedThread.write(b);
                }
                break;
            case 'e':
                if (CONNECTED) {
                    byte[] e = new byte[]{101};
                    mConnectedThread.write(e);
                }
                break;
        }
    }

    /**
     * 连接蓝牙设备
     * 先启动一个线程去连接，连接成功之后，再启动一个线程去接收与传输数据
     */

    public void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);
        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }


    public class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");
            // Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();
            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                mECGHandler.sendEmptyMessage(CONNECT_FAILURE);
                Log.e(TAG, "unable to connect() socket", e);
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                return;
            }

            mConnectThread = null;
            // Start the connected thread
            // Start the thread to manage the connection and perform transmissions
            mConnectedThread = new ConnectedThread(mmSocket);
            mConnectedThread.start();


        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device. It handles all
     * incoming and outgoing transmissions.
     * 已连接之后，传输数据用到
     */
    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        private BufferedReader mBufferedReader;

        public ConnectedThread(BluetoothSocket socket) {
            Log.i(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // Get the BluetoothSocket input and output streams
            //获得输入流和输出流
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();

            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mBufferedReader = new BufferedReader(new InputStreamReader(mmInStream));
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            //Toast.makeText(MainActivity.this, "蓝牙连接成功", Toast.LENGTH_SHORT).show();
            //这里使用Toast会使程序错误，后台线程无法修改UI
            mECGHandler.sendEmptyMessage(CONNECT_OK);//回传OK说明连接正常
            String data;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    data = mBufferedReader.readLine();
                    if (TextUtils.isEmpty(data)) {
                        continue;
                    }

                    switch (RECEIVER_MODE) {
                        case RECEIVE_MODE_ECG:        //31是心电数据的内容

                            for (int i = 0; i < data.length(); i++) {
                                if (data.charAt(i) == '@') {
                                    mECGIntegers.add(Integer.valueOf(data.substring(i + 1, data.length())));
                                    mECGWaveTimes++;
                                }
                            }

                            if (mECGIntegers.size() == 120) {
                                Message message = mECGHandler.obtainMessage(MSG_NEW_ECG, new
                                        ArrayList<Integer>(mECGIntegers));
                                mECGHandler.sendMessage(message);
                                mECGIntegers.clear();
                            }

                            if (mECGWaveTimes == 120) {
                                mECGWaveTimes = 0;
                            }

                            break;

                        case RECEIVE_MODE_SPHY:        //这里是进入血氧脉搏模式后的接收情况
                            if (data.charAt(0) == '*') {
                                break;
                            } else if (data.charAt(0) == '!') {
                                break;
                            } else if (data.charAt(0) == '?') {
                                break;
                            } else if (data.charAt(0) == '|') {
                                break;
                            }
                            mSphyHandler.sendMessage(mSphyHandler.obtainMessage(MSG_NEW_SPHY, data));
                            break;

                        case RECEIVE_MODE_SPORT:        //这里是进入运动模式后的接收情况
                            mSportHandler.sendMessage(mSportHandler.obtainMessage(MSG_NEW_SPORT, data));
                            break;

                        case RECEIVE_MODE_SLEEP:        //进入睡眠模式
                            mSleepHandler.sendMessage(mSleepHandler.obtainMessage(MSG_NEW_SLEEP, data));
                            break;

                    }


                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    mECGHandler.sendEmptyMessage(BLUETOOTH_DISCONNECTED);
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * 发送数据流
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }

    }
}
