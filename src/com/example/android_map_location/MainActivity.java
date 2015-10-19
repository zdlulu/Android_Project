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
		mapView.onCreate(savedInstanceState);// �˷���������д

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
	 * ��ʼ����λ
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
		// ��ʼ����λ��ֻ�������綨λ
		mLocationManagerProxy = LocationManagerProxy.getInstance(this);
		mLocationManagerProxy.setGpsEnable(false);
		// �˷���Ϊÿ���̶�ʱ��ᷢ��һ�ζ�λ����Ϊ�˼��ٵ������Ļ������������ģ�
		// ע�����ú��ʵĶ�λʱ��ļ������С���֧��Ϊ2000ms���������ں���ʱ�����removeUpdates()������ȡ����λ����
		// �ڶ�λ�������ں��ʵ��������ڵ���destroy()����
		// ����������ʱ��Ϊ-1����λֻ��һ��,
		// �ڵ��ζ�λ����£���λ���۳ɹ���񣬶��������removeUpdates()�����Ƴ����󣬶�λsdk�ڲ����Ƴ�
		mLocationManagerProxy.requestLocationData(
				LocationProviderProxy.AMapNetwork, 10 * 1000, 15, this);
		if (aMap == null) {
			aMap = mapView.getMap();
			setUpMap();
		}
		mLocationManagerProxy = LocationManagerProxy.getInstance(this);
		//��ȡʵʱ����Ԥ��
		//�����Ҫͬʱ����ʵʱ��δ��������������ȷ����λ��ȡλ�ú�ʹ��,�ֿ����ã��ɺ��Ա��䡣
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
				mListener.onLocationChanged(amapLocation);// ��ʾϵͳС����
			}else{
				Log.i(TAG,"mListener==null");
			}
			geoLat = amapLocation.getLatitude();
			geoLng = amapLocation.getLongitude();
			common.set_string("����="+String.valueOf(amapLocation.getLatitude()));
			common.set_string("γ��="+String.valueOf(amapLocation.getLongitude()));
			common.set_string("�ص�="+amapLocation.getAddress());
			Log.i(TAG,"����="+amapLocation.getLatitude());
			Log.i(TAG,"γ��="+amapLocation.getLongitude());
			Log.i(TAG,"�ص�="+amapLocation.getAddress());
		}else{
			Log.e(TAG,"amapLocation==null");
		}
	}
	/*******************************************************/ 
	@Override
	protected void onPause() {
		super.onPause();
		// �Ƴ���λ����
		mLocationManagerProxy.removeUpdates(this);
		// ���ٶ�λ
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
	 * ����������д
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}
	/*******************************************************/ 
	/**
	 * ����һЩamap������
	 */
	private void setUpMap() {
		aMap.setLocationSource(this);// ���ö�λ����
		aMap.getUiSettings().setMyLocationButtonEnabled(true);// ����Ĭ�϶�λ��ť�Ƿ���ʾ
		aMap.setMyLocationEnabled(true);// ����Ϊtrue��ʾ��ʾ��λ�㲢�ɴ�����λ��false��ʾ���ض�λ�㲢���ɴ�����λ��Ĭ����false
		// ���ö�λ������Ϊ��λģʽ �������ɶ�λ��������ͼ������������ת����
		aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
	}
	
	
    /*LocationSource*************************************************/
	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;
		if (mAMapLocationManager == null) {
			mAMapLocationManager = LocationManagerProxy.getInstance(this);
			// �˷���Ϊÿ���̶�ʱ��ᷢ��һ�ζ�λ����Ϊ�˼��ٵ������Ļ������������ģ�
			// ע�����ú��ʵĶ�λʱ��ļ������С���֧��Ϊ2000ms���������ں���ʱ�����removeUpdates()������ȡ����λ����
			// �ڶ�λ�������ں��ʵ��������ڵ���destroy()����
			// ����������ʱ��Ϊ-1����λֻ��һ��
			// �ڵ��ζ�λ����£���λ���۳ɹ���񣬶��������removeUpdates()�����Ƴ����󣬶�λsdk�ڲ����Ƴ�
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
			// ����Ԥ���ɹ��ص� ����������Ϣ
			Log.i(TAG,"����="+aMapLocalWeatherLive.getCity());
			Log.i(TAG,"����="+aMapLocalWeatherLive.getWeather());
			Log.i(TAG,"�¶�="+aMapLocalWeatherLive.getTemperature()+"��");
			Log.i(TAG,"����="+aMapLocalWeatherLive.getWindDir()+"��");
			Log.i(TAG,"����="+aMapLocalWeatherLive.getWindPower()+"��");
			Log.i(TAG,"ʪ��="+aMapLocalWeatherLive.getHumidity()+"%");
			Log.i(TAG,"ʱ��="+aMapLocalWeatherLive.getReportTime());
		} else {
			// ��ȡ����Ԥ��ʧ��
			Log.i(TAG,"onWeatherLiveSearched else");
			Toast.makeText(
					this,
					"��ȡ����Ԥ��ʧ��:"
							+ aMapLocalWeatherLive.getAMapException()
									.getErrorMessage(), Toast.LENGTH_SHORT)
					.show();
		 
		}
		
	}
	/*************************************************************/
	public void search(String keyword){
		query = new Query(keyword, null, "�����");  
		query.setPageSize(10);  
	    query.setPageNum(1);  
	    // ��ѯ��Ȥ��  
	    search = new PoiSearch(this, query);  
	    // �첽����  
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
	/*listview�����ȡĳһ��������***************************************************/
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		String listview_str = (String) ((TextView) view).getText();
		et_start.setText(listview_str);
		mLvResult.setAdapter(null);  
	}
	/*��editText�ļ���************************************************************/
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
