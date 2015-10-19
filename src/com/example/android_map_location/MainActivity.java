package com.example.android_map_location;

import java.util.ArrayList;
import java.util.List;

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
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.amap.api.services.poisearch.PoiSearch.Query;
import com.amap.api.services.poisearch.PoiSearch.SearchBound;
import com.example.message.Common;
import com.example.message.MessageShowActivity;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements 
	AMapLocationListener,LocationSource,AMapLocalWeatherListener,
	OnPoiSearchListener, OnItemClickListener {
	
	private LocationManagerProxy mLocationManagerProxy;
	private String TAG = "MainActivity";
	private OnLocationChangedListener mListener;
    MapView mapView;
	private AMap aMap;
	private LocationManagerProxy mAMapLocationManager;
	private String str="";
	private PoiSearch search;
	private PoiSearch.Query query;
	private Button btn_intent;
	private ListView mLvResult;
	private EditText et_start;
	private double geoLat,geoLng;
	private Common common= new Common();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);// 此方法必须重写

	}
	/*******************************************************/ 
	public void init_widget(){
		btn_intent = (Button) findViewById(R.id.btn_intent);
		mLvResult = (ListView) findViewById(R.id.lv_result);  
		mLvResult.setOnItemClickListener(this);  
		et_start = (EditText) findViewById(R.id.et_start);
		et_start.addTextChangedListener(textWatcher);  
	}
	/**
	 * 初始化定位
	 */
	private void init() {
		init_widget();
		btn_intent.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this,MessageShowActivity.class);
				startActivityForResult(intent, 1);
			}
			
		});
		// 初始化定位，只采用网络定位
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
	/*******************************************************/ 
	@Override
	public void onLocationChanged(Location location) {
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
	}
	/*******************************************************/ 
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
			geoLat = amapLocation.getLatitude();
			geoLng = amapLocation.getLongitude();
			common.set_string("经度="+String.valueOf(amapLocation.getLatitude()));
			common.set_string("纬度="+String.valueOf(amapLocation.getLongitude()));
			common.set_string("地点="+amapLocation.getAddress());
			Log.i(TAG,"经度="+amapLocation.getLatitude());
			Log.i(TAG,"纬度="+amapLocation.getLongitude());
			Log.i(TAG,"地点="+amapLocation.getAddress());
		}else{
			Log.e(TAG,"amapLocation==null");
		}
	}
	/*******************************************************/ 
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
	/*******************************************************/ 
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
	/*************************************************************/
	public void search(String keyword){
		query = new Query(keyword, null, "天津市");  
		query.setPageSize(10);  
	    query.setPageNum(1);  
	    // 查询兴趣点  
	    search = new PoiSearch(this, query);  
	    // 异步搜索  
	    search.searchPOIAsyn();  
	    search.setOnPoiSearchListener(this); 
//	    search.setBound(new SearchBound(new LatLonPoint(geoLat, geoLng),5000));
	}

	@Override
	public void onPoiItemDetailSearched(PoiItemDetail arg0, int arg1) {
		
	}
	
	ArrayList<PoiItem> items = new ArrayList<PoiItem>();
	@Override
	public void onPoiSearched(PoiResult poiResult, int rCode) {
		List<String> strs = new ArrayList<String>();
		items = poiResult.getPois();
//		Log.i("="+items.size(), "201510");
		if (items != null && items.size() > 0) {
			PoiItem item = null;
			for (int i = 0, count = items.size(); i < count; i++) {
				item = items.get(i);
				strs.add(item.getTitle());
//				Log.i("="+item.getTitle(), "201510");
			}
			ArrayAdapter<String> array = new ArrayAdapter<String>(this,  
	                android.R.layout.simple_list_item_1, strs);  
	        mLvResult.setAdapter(array);  
		}
	}
	/*listview点击获取某一条的内容***************************************************/
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		String listview_str = (String) ((TextView) view).getText();
		et_start.setText(listview_str);
		mLvResult.setAdapter(null);  
	}
	/*对editText的监听************************************************************/
	private TextWatcher textWatcher = new TextWatcher() {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
//			Log.i(TAG,"beforeTextChanged--------------->");  
		}
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
//			Log.i(TAG,"onTextChanged--------------->");    
            str = et_start.getText().toString();  
            
		}
		@Override
		public void afterTextChanged(Editable s) {
//			Log.i(TAG,"afterTextChanged--------------->"); 
			if(!str.equals("")){
            	search(str);
            }else{
            	mLvResult.setAdapter(null);  
            }
			
		}
	};
	/*************************************************************/
	/*************************************************************/
	/*************************************************************/
	/*************************************************************/
}
