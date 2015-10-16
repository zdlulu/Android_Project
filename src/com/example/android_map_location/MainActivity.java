package com.example.android_map_location;

import com.amap.api.location.AMapLocalWeatherForecast;
import com.amap.api.location.AMapLocalWeatherListener;
import com.amap.api.location.AMapLocalWeatherLive;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements 
	AMapLocationListener,LocationSource,AMapLocalWeatherListener{
	
	private LocationManagerProxy mLocationManagerProxy;
	private String TAG = "MainActivity";
	private OnLocationChangedListener mListener;
    MapView mapView;
	private AMap aMap;
	private LocationManagerProxy mAMapLocationManager;
	private TextView tv_information;
	private String str_information="";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);// 此方法必须重写
		
	}
	
	/**
	 * 初始化定位
	 */
	private void init() {
		// 初始化定位，只采用网络定位
		tv_information = (TextView) findViewById(R.id.tv_information);
		tv_information.setMovementMethod(new ScrollingMovementMethod());
		mLocationManagerProxy = LocationManagerProxy.getInstance(this);
		mLocationManagerProxy.setGpsEnable(false);
		// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
		// 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用removeUpdates()方法来取消定位请求
		// 在定位结束后，在合适的生命周期调用destroy()方法
		// 其中如果间隔时间为-1，则定位只定一次,
		// 在单次定位情况下，定位无论成功与否，都无需调用removeUpdates()方法移除请求，定位sdk内部会移除
		mLocationManagerProxy.requestLocationData(
				LocationProviderProxy.AMapNetwork, 10 * 1000, 15, this);
		if (aMap == null) {
			aMap = mapView.getMap();
			setUpMap();
		}
		mLocationManagerProxy = LocationManagerProxy.getInstance(this);
		//获取实时天气预报
		//如果需要同时请求实时、未来三天天气，请确保定位获取位置后使用,分开调用，可忽略本句。
		mLocationManagerProxy.requestWeatherUpdates(
				LocationManagerProxy.WEATHER_TYPE_LIVE, this);

	}
	
	@Override
	public void onLocationChanged(Location location) {
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		
	}

	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		if (amapLocation != null
				&& amapLocation.getAMapException().getErrorCode() == 0) {
			if(mListener != null){
				mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
			}else{
				Log.i(TAG,"mListener==null");
			}
			set_information("经度="+String.valueOf(amapLocation.getLatitude()));
			set_information("纬度="+String.valueOf(amapLocation.getLongitude()));
			set_information("地点="+amapLocation.getAddress());
			Log.i(TAG,"经度="+amapLocation.getLatitude());
			Log.i(TAG,"纬度="+amapLocation.getLongitude());
			Log.i(TAG,"地点="+amapLocation.getAddress());
		}else{
			Log.e(TAG,"amapLocation==null");
		}
	}
	@Override
	protected void onPause() {
		super.onPause();
		// 移除定位请求
		mLocationManagerProxy.removeUpdates(this);
		// 销毁定位
		mLocationManagerProxy.destroy();
		mapView.onPause();
		deactivate();
	}
	@Override
	protected void onStart(){
		super.onStart();
		init();
	}
	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}
	
	/**
	 * 设置一些amap的属性
	 */
	private void setUpMap() {
		aMap.setLocationSource(this);// 设置定位监听
		aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
		aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
		// 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
		aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
	}
	
	
    /*LocationSource*************************************************/
	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;
		if (mAMapLocationManager == null) {
			mAMapLocationManager = LocationManagerProxy.getInstance(this);
			// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
			// 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用removeUpdates()方法来取消定位请求
			// 在定位结束后，在合适的生命周期调用destroy()方法
			// 其中如果间隔时间为-1，则定位只定一次
			// 在单次定位情况下，定位无论成功与否，都无需调用removeUpdates()方法移除请求，定位sdk内部会移除
			mAMapLocationManager.requestLocationData(
					LocationProviderProxy.AMapNetwork, 60 * 1000, 10, this);
		}
	}

	@Override
	public void deactivate() {
		mListener = null;
		if (mAMapLocationManager != null) {
			mAMapLocationManager.removeUpdates(this);
			mAMapLocationManager.destroy();
		}
		mAMapLocationManager = null;
		
	}
	/*LocationSource*************************************************/
	
	/*AMapLocalWeatherListener************************************/
	@Override
	public void onWeatherForecaseSearched(AMapLocalWeatherForecast arg0) {
		
	}
	@Override
	public void onWeatherLiveSearched(AMapLocalWeatherLive aMapLocalWeatherLive) {
		if (aMapLocalWeatherLive!=null&&aMapLocalWeatherLive.getAMapException().getErrorCode() == 0) {
			// 天气预报成功回调 设置天气信息
			Log.i(TAG,"城市="+aMapLocalWeatherLive.getCity());
			Log.i(TAG,"天气="+aMapLocalWeatherLive.getWeather());
			Log.i(TAG,"温度="+aMapLocalWeatherLive.getTemperature()+"℃");
			Log.i(TAG,"风向="+aMapLocalWeatherLive.getWindDir()+"风");
			Log.i(TAG,"风力="+aMapLocalWeatherLive.getWindPower()+"级");
			Log.i(TAG,"湿度="+aMapLocalWeatherLive.getHumidity()+"%");
			Log.i(TAG,"时间="+aMapLocalWeatherLive.getReportTime());
		} else {
			// 获取天气预报失败
			Log.i(TAG,"onWeatherLiveSearched else");
			Toast.makeText(
					this,
					"获取天气预报失败:"
							+ aMapLocalWeatherLive.getAMapException()
									.getErrorMessage(), Toast.LENGTH_SHORT)
					.show();
		 
		}
		
	}
	/*AMapLocalWeatherListener************************************/
	int i=1;
	public void set_information(String s){
		str_information = str_information+s+"\n";
		tv_information.setText(str_information);
		tv_information.setMaxLines(i++);
	}
	/*************************************************************/
}
