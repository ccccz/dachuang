package com.example.dachuang;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;


import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView=(MapView)findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mButton=(ImageButton) findViewById(R.id.mButton);
        isStart=false;


        init();
        initPoint();
        initLocal();

        /**
         * 设置控件样式
         */
        uiSettings=aMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(false);

        /**
         * 获取定位权限
         */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    WRITE_COARSE_LOCATION_REQUEST_CODE);//自定义的code
        }

        /**
         * 隐藏状态栏，导航栏，actionbar TODO:目前失效中
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
        //声明AMapLocationClientOption对象，AMapLocationClientOption用来设置发起定位的模式和相关参数
        mLocationClientOption=new AMapLocationClientOption();
        /**
         * AMapLocationClientOption设置
         */
        //设置定位场景，此处选择运动
        mLocationClientOption.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.Sport);
        //设置定位模式
        mLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationClientOption.setInterval(3000);
//        mLocationClientOption.setOnceLocation(true);
        if (mLocationClient!=null){
            //应用于AMapLocationClient
            mLocationClient.setLocationOption(mLocationClientOption);
            //类似于重启的操作，保证场景生效
            mLocationClient.stopLocation();
            mLocationClient.startLocation();
        }
    }

    /**
     * 移动本位置到地图中心
     */
    private void local(){
        aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
    }
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        isOK=0;
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


}
