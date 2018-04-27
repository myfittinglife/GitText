package com.jikexueyuan.com.baidulbs;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class MainActivity extends AppCompatActivity {
    public  LocationClient mLocationClient;     //定位服务的客户端
    private TextView latitude_text,longitude_text;
    private EditText title_text,content_text,location_text,tel_text;
    private Button uploadBtn,renovateBtn;
    private MapView mapView;    //呈现地图
    private BaiduMap baiduMap;//地图总控制类 建立
    private boolean isFirstLocate=true;//防止多次调用animateMapStatus()方法




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocationClient=new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());//注册定位监听器
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("信息上传");
        Bmob.initialize(this,"12a244a082bdc4511edeaf7f98a79c56");
        latitude_text= (TextView) findViewById(R.id.latitude);
        longitude_text= (TextView) findViewById(R.id.longitude);
        title_text= (EditText) findViewById(R.id.title_text);
        content_text= (EditText) findViewById(R.id.content_text);
        location_text= (EditText) findViewById(R.id.location_text);
        tel_text= (EditText) findViewById(R.id.tel_text);
        uploadBtn= (Button) findViewById(R.id.upload_btn);
        renovateBtn= (Button) findViewById(R.id.renovate);

        mapView=(MapView) findViewById(R.id.bmapView);
        baiduMap=mapView.getMap();//获取到BaiduMap实例
        baiduMap.setMyLocationEnabled(true);//获取自己的位置 让我显示在地图上



        List<String> permissionList=new ArrayList<>();//进行运行时权限 三个权限的申请
        if(ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(android.Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String []permissions=permissionList.toArray(new String[permissionList.size()]);//将List集合转换为数组，再调用requestPermissions方法一次性申请权限
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);//一次性申请list中添加的所有权限
        }else{
            requestLocation();//自定义方法   开始地理定位
        }

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(title_text.length()==0||content_text.length()==0||location_text.length()==0||tel_text.length()==0){
                    Toast.makeText(getApplicationContext(),"请填写完整的信息",Toast.LENGTH_SHORT).show();
                }
                else{
                    BMap bMap=new BMap();
                    bMap.setTitle(title_text.getText().toString());
                    bMap.setContent(content_text.getText().toString());
                    bMap.setLocation(location_text.getText().toString());
                    bMap.setTel(tel_text.getText().toString());
                    bMap.setLatitude(latitude_text.getText().toString());
                    bMap.setLongitude(longitude_text.getText().toString());
                    bMap.save(new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                            if(e==null){
                                Toast.makeText(getApplicationContext(),"上传成功",Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(getApplicationContext(),"发生未知错误"+e.getMessage()+e.getErrorCode(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        renovateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                requestLocation();
//                poiSearch();
//                Toast.makeText(getApplicationContext(),"查找完成",Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(getApplication(),BusLineSearchDemo.class);
                startActivity(intent);

            }
        });
        baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                latitude_text.setText(String.valueOf(latLng.latitude));
                longitude_text.setText(String.valueOf(latLng.longitude));
                Toast.makeText(getApplicationContext(), "经度：" + latLng.latitude + "纬度" + latLng.longitude, Toast.LENGTH_LONG).show();
                //定义Maker坐标点
                LatLng point = new LatLng(latLng.latitude, latLng.longitude);
                //构建Marker图标
                BitmapDescriptor bitmap = BitmapDescriptorFactory
                        .fromResource(R.drawable.marker);
                //构建MarkerOption，用于在地图上添加Marker
                OverlayOptions option = new MarkerOptions()
                        .position(point)
                        .icon(bitmap)
                        .title("管理学院");
                //在地图上添加Marker，并显示
                baiduMap.addOverlay(option);
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }

        });
        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.getExtraInfo();
                return false;
            }
        });
    }
    private void requestLocation(){
        initLocation();
        mLocationClient.start();
    }
    private void initLocation(){         //动态定位，每隔5秒刷新定位
        LocationClientOption option=new LocationClientOption();
        option.setScanSpan(1000);//如果设置非0，需设置1000ms以上才有效
        option.setCoorType("BD09ll");      //设置此才会在自己的位置中来，才会准确
        option.setIsNeedAddress(true);      //获取详细位置信息
        option.setOpenGps(true);
        option.setIgnoreKillProcess(false);
        //可选，定位SDK内部是一个service，并放到了独立进程。
        //设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)
        mLocationClient.setLocOption(option);

    }
    //将地图移动自己的位置上来
    private void navigateTo(BDLocation location) {
        if (isFirstLocate) {//源代码此处有时会导致再次打开应用不移到此位置
            LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());       //存放经纬度
            MapStatus.Builder builder=new MapStatus.Builder();                  //以下部分地理信息的存储和地图的更新是和课本不一样的地方
            builder.target(latLng).zoom(16f);
            baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            isFirstLocate=false;
        }
        //书中此处正确
        //让我显示在地图中  MyLocationData.Builder用来存放当前自己的位置信息
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData = locationBuilder.build();//build方法用来生成一个MyLocationData实例
        baiduMap.setMyLocationData(locationData);//将其实例传入就可已在地图上显示自己
    }
    //获取权限申请的结果  权限不够则直接退出应用
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length>0){
                    for(int result:grantResults){
                        if (result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"必须同意所有权限才能使用本程序",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }else{
                    Toast.makeText(this,"发生未知错误",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }
    //以下三个方法保证资源能即使释放
    @Override
    protected void onResume(){
        super.onResume();
        mapView.onResume();
    }
    @Override
    protected void onPause(){
        super.onPause();
        mapView.onResume();

    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        mLocationClient.stop();         //停止定位，防止后台消耗电量
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }
    private void poiSearch(){

        //创建POI检索实例
        PoiSearch poiSearch=PoiSearch.newInstance();
        poiSearch.searchInCity((new PoiCitySearchOption())
                .city("保定")
                .keyword("27"));
        OnGetPoiSearchResultListener poiListener=new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                String busLineId ;
                if (poiResult == null || poiResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    return;
                }
                //遍历所有POI，找到类型为公交线路的POI
                for (PoiInfo poi : poiResult.getAllPoi()) {
                    if (poi.type == PoiInfo.POITYPE.BUS_LINE ||poi.type == PoiInfo.POITYPE.SUBWAY_LINE) {
                        //说明该条POI为公交信息，获取该条POI的UID
                        break;
                    }
                }
            }
            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
            }
            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
            }
        };



//        OnGetBusLineSearchResultListener  poiListener =new OnGetBusLineSearchResultListener() {
//            @Override
//            public void onGetBusLineResult(BusLineResult busLineResult) {
//                if (busLineResult == null || busLineResult.error != SearchResult.ERRORNO.NO_ERROR) {
//                    return;
//                }
//                else{
//                    //遍历所有POI，找到类型为公交线路的POI
//                    for (PoiInfo poi : busLineResult.getAllPoi()) {
//
//                        if (poi.type == PoiInfo.POITYPE.BUS_LINE ||poi.type == PoiInfo.POITYPE.SUBWAY_LINE) {
//
//                            //说明该条POI为公交信息，获取该条POI的UID
//                            busLineId = poi.uid;
//                            break;
//                        }
//                    }
//                }
//
//            }
//        };





    }
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (bdLocation.getLocType() == BDLocation.TypeGpsLocation || bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                        navigateTo(bdLocation);//当定位到当前位置时，将BDLocation对象传给navigateTo方法，用于将地图移动到此处
                    }
                    latitude_text.setText(String.valueOf(bdLocation.getLatitude()));
                    longitude_text.setText(String.valueOf(bdLocation.getLongitude()));
                }
            });
        }
    }
}
