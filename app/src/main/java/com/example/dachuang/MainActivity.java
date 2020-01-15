package com.example.dachuang;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
//test
public class MainActivity extends AppCompatActivity {

    private MapView mapView;
    private AMap aMap;
    private MyLocationStyle myLocationStyle;
    private UiSettings uiSettings;
    private int WRITE_COARSE_LOCATION_REQUEST_CODE=1;
    private ImageButton mButton; //开始/暂停按钮
    private boolean isStart; //声明是否在记录运动轨迹
    private AMapLocationClient mLocationClient; //声明AMapLocationClient类对象
    private AMapLocationListener mLocationListener;  //声明定位回调监听器
    private AMapLocationClientOption mLocationClientOption;
    private int isOK;
    private List<LatLng> latLngs; //行走点标记
    private LatLng latLng;
    private Polyline polyline;
    private SearchView mSearchView;
    private Polyline polyline1,polyline2,polyline3;
    private LinearLayout selectLLayout; //选择路线模块
    private ConstraintLayout operateCLayout; //主操作模块


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView=(MapView)findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mButton=(ImageButton) findViewById(R.id.mButton);
        isStart=false;
        mSearchView=(SearchView)findViewById(R.id.mySearch);
        selectLLayout=findViewById(R.id.selectModel);
        operateCLayout=findViewById(R.id.operateModel);


        init();
        initPoint();
        initLocal();
        quanxian();

        /**
         * 设置控件样式
         */
        uiSettings=aMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(false);
        EditText et = (EditText)mSearchView.findViewById(mSearchView.getContext().getResources()
                .getIdentifier("android:id/search_src_text", null, null));
        et.setFilters(new InputFilter[] { new InputFilter.LengthFilter(1) });

        /**
         * 隐藏状态栏，导航栏，actionbar TODO:可能是无效代码，回头修改
         */
        View decorView=getWindow().getDecorView();
        int option = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(option);


        /**
         * button点击处理
         */
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isStart){
                    mButton.setImageDrawable(getResources().getDrawable(R.drawable.stop));
                    System.out.println("click>>>>>>>>>>>>>>>>>>>");
                    latLngs=new ArrayList<LatLng>();
                    polyline=printRunLine(latLngs);
                }else{
                    mButton.setImageDrawable(getResources().getDrawable(R.drawable.start));
                    System.out.println("click<<<<<<<<<<<<<<<<<<");
                    System.out.println(latLngs.size());
                }
                isStart=!isStart;
            }
        });

        /**
         * searchView点击处理
         */
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                System.out.println("搜索       "+s);
                if(s.equals("s")){
                    List<LatLng> latLngs1=new ArrayList<>();
                    List<LatLng> latLngs2=new ArrayList<>();
                    List<LatLng> latLngs3=new ArrayList<>();

                    latLngs1.add(new LatLng(32.051814,118.782831));
                    latLngs1.add(new LatLng(32.051732,118.782134));
                    latLngs1.add(new LatLng(32.051423,118.782177));
                    latLngs1.add(new LatLng(32.051432,118.782874));
                    latLngs1.add(new LatLng(32.051187,118.782885));
                    latLngs1.add(new LatLng(32.051141,118.782198));

                    latLngs2.add(new LatLng(32.051332,118.781479));
                    latLngs2.add(new LatLng(32.051114,118.781522));
                    latLngs2.add(new LatLng(32.051105,118.782241));
                    latLngs2.add(new LatLng(32.051423,118.782187));
                    latLngs2.add(new LatLng(32.051487,118.782874));
                    latLngs2.add(new LatLng(32.051196,118.782917));

                    polyline1=printLine(latLngs1);
                    polyline2=printAlterLine(latLngs1);
                    TextView op1=findViewById(R.id.option1);
                    op1.setOnClickListener(new ButtonListener());
                    op1.setVisibility(View.VISIBLE);
                    op1.setText(calcuDes(latLngs1)+"m");
                    TextView op2=findViewById(R.id.option2);
                    op2.setOnClickListener(new ButtonListener());
                    op2.setVisibility(View.VISIBLE);
                    op2.setText(calcuDes(latLngs2)+"m");
                    operateCLayout.setVisibility(View.GONE);
                    selectLLayout.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(MainActivity.this,"未搜索到路径，请尝试更换搜索内容或更改位置",Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    /**
     * 初始化aMap对象
     */
    private void init(){
        if(aMap==null){
            aMap=mapView.getMap();
        }
        aMap.moveCamera(CameraUpdateFactory.zoomTo(19));
    }

    /**
     * 显示定位蓝点
     */
    private void initPoint(){
        myLocationStyle=new MyLocationStyle();
        myLocationStyle.interval(2000);
        myLocationStyle.showMyLocation(true);
        myLocationStyle.strokeWidth(0);
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER);
        myLocationStyle.radiusFillColor(Color.TRANSPARENT);
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setMyLocationEnabled(true);
    }

    /**
     * 绘制线条
     */
    private Polyline printLine(List<LatLng> ls){
        Polyline polyline;
        polyline=aMap.addPolyline(new PolylineOptions().addAll(ls).width(15).color(Color.argb(255, 0, 0, 0)).lineJoinType(PolylineOptions.LineJoinType.LineJoinRound));
        System.out.println("printLine success");
        return polyline;
    }
    private Polyline printAlterLine(List<LatLng> ls){
        Polyline polyline;
        polyline=aMap.addPolyline(new PolylineOptions().addAll(ls).width(15).color(Color.argb(208, 0, 0, 0)).lineJoinType(PolylineOptions.LineJoinType.LineJoinRound));
        System.out.println("printLine success");
        return polyline;
    }
    private Polyline printRunLine(List<LatLng> ls){
        Polyline polyline;
        polyline=aMap.addPolyline(new PolylineOptions().addAll(ls).width(15).color(Color.argb(255, 255, 241, 0)).lineJoinType(PolylineOptions.LineJoinType.LineJoinRound));
        System.out.println("printLine success");
        return polyline;
    }

    /**
     * 设置定位
     */
    private void initLocal(){
        mLocationClient = new AMapLocationClient(getApplicationContext());//初始化定位
        mLocationClient.setLocationListener(mLocationListener);//设置定位回调监听
        mLocationListener=new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation!=null){
                    if (aMapLocation.getErrorCode()==0) {
                        if (isOK<3){
                            isOK++;
                            local();
                        }else{
                            if (isStart){
                                latLngs.add(new LatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude()));
                                polyline.setPoints(latLngs);
                            }
                        }
                        latLng=new LatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude());
                    }else{
                        Log.e("AmapError","location Error, ErrCode:"
                                + aMapLocation.getErrorCode() + ", errInfo:"
                                + aMapLocation.getErrorInfo());
                    }
                }
            }
        };
        mLocationClient.setLocationListener(mLocationListener);
//        mLocationClient.enableBackgroundLocation(2019,buildNotification());
        //声明AMapLocationClientOption对象，AMapLocationClientOption用来设置发起定位的模式和相关参数
        mLocationClientOption=new AMapLocationClientOption();
        /**
         * AMapLocationClientOption设置
         */
        //设置定位场景，此处选择运动
        mLocationClientOption.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.Sport);
        //设置定位模式
        mLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationClientOption.setInterval(1000);
//        mLocationClientOption.setOnceLocation(true);
        if (mLocationClient!=null){
            //应用于AMapLocationClient
            mLocationClient.setLocationOption(mLocationClientOption);
            //类似于重启的操作，保证场景生效
            mLocationClient.stopLocation();
            mLocationClient.startLocation();
            mLocationClient.enableBackgroundLocation(2019,buildNotification());
        }
    }

    /**
     * 移动本位置到地图中心
     */
    private void local(){
        aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
    }

    /**
     * 活动状态
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        isOK=0;
        quanxian();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    /**
     *设置按钮监听
     */
    private class ButtonListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.option1:
                    polyline1.setColor(Color.argb(255, 0, 0, 0));
                    if(polyline2!=null){
                        polyline2.setColor(Color.argb(208, 0, 0, 0));
                    }
                    if(polyline3!=null){
                        polyline3.setColor(Color.argb(208, 0, 0, 0));
                    }
                    break;
                case R.id.option2:
                    polyline1.setColor(Color.argb(208, 0, 0, 0));
                    polyline2.setColor(Color.argb(255, 0, 0, 0));
                    if(polyline3!=null){
                        polyline3.setColor(Color.argb(208, 0, 0, 0));
                    }
                    break;
                case R.id.option3:
                    polyline1.setColor(Color.argb(208, 0, 0, 0));
                    polyline2.setColor(Color.argb(208, 0, 0, 0));
                    polyline3.setColor(Color.argb(255, 0, 0, 0));
                    break;
                case R.id.confirmBtn:
                    if (polyline1.getColor()==Color.argb(208, 0, 0, 0)){
                        polyline1.setVisible(false);
                    }
                    if(polyline2!=null){
                        if (polyline2.getColor()==Color.argb(208, 0, 0, 0)){
                            polyline2.setVisible(false);
                        }
                    }
                    if(polyline3!=null){
                        if (polyline3.getColor()==Color.argb(208, 0, 0, 0)){
                            polyline3.setVisible(false);
                        }
                    }
                    selectLLayout.setVisibility(View.GONE);
                    operateCLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 关于定位权限问题
     */
    private static final String NOTIFICATION_CHANNEL_NAME = "BackgroundLocation";
    private NotificationManager notificationManager = null;
    private boolean isCreateChannel = false;
    @SuppressLint("NewApi")
    private Notification buildNotification() {

        Notification.Builder builder = null;
        Notification notification = null;
        if(android.os.Build.VERSION.SDK_INT >= 26) {
            //Android O上对Notification进行了修改，如果设置的targetSDKVersion>=26建议使用此种方式创建通知栏
            if (null == notificationManager) {
                notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            }
            String channelId = getPackageName();
            if(!isCreateChannel) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId,
                        NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.enableLights(true);//是否在桌面icon右上角展示小圆点
                notificationChannel.setLightColor(Color.BLUE); //小圆点颜色
                notificationChannel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
                notificationManager.createNotificationChannel(notificationChannel);
                isCreateChannel = true;
            }
            builder = new Notification.Builder(getApplicationContext(), channelId);
        } else {
            builder = new Notification.Builder(getApplicationContext());
        }
        CharSequence charSequence=new String("Dachuang");
        builder.setSmallIcon(R.drawable.stop)
                .setContentTitle(charSequence)
                .setContentText("正在后台运行")
                .setWhen(System.currentTimeMillis());

        System.out.println("显示后台信息");
        if (android.os.Build.VERSION.SDK_INT >= 16) {
            notification = builder.build();
        } else {
            return builder.getNotification();
        }
        return notification;
    }

    /**
     * 关于定位权限问题2
     */
    private static final int LOCATION_CODE=1;
    private LocationManager locationManager;
    public void quanxian(){
        locationManager=(LocationManager)MainActivity.this.getSystemService(MainActivity.LOCATION_SERVICE);
        boolean ok=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (ok){
            System.out.println("检测是否开启定位");
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                //没有权限，申请WRITE_EXTERNAL_STORAGE权限
                System.out.println("检测到未开启定位");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        WRITE_COARSE_LOCATION_REQUEST_CODE);//自定义的code
            }
        }else{
            Toast.makeText(MainActivity.this,"检测到未开启GPS定位服务，请开启定位服务",Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 距离、配速计算
     */
    private double calcuDes(LatLng latLngA,LatLng latLngB){
        return AMapUtils.calculateLineDistance(latLngA,latLngB);
    }
    private double calcuDes(List<LatLng> ls){
        Double res=0.0;
        for(int i=1;i<ls.size();i++){
            res=res+calcuDes(ls.get(i-1),ls.get(i));
        }
        return res;
    }
    private double calcuSpeed(LatLng latLngA,LatLng latLngB){
        double speed=(1000/calcuDes(latLngA,latLngB))/60;
        BigDecimal bigDecimal=new BigDecimal(speed);
        return bigDecimal.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
    }
    private double calcuSpeed(List<LatLng> ls){
        double speed=calcuDes(ls)/ls.size();
        speed=(1000/speed)/60;
        BigDecimal bigDecimal=new BigDecimal(speed);
        return bigDecimal.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
    }

}