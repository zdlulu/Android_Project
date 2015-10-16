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
		mapView.onCreate(savedInstanceState);// �˷���������д
		
	}
	
	/**
	 * ��ʼ����λ
	 */
	private void init() {
		// ��ʼ����λ��ֻ�������綨λ
		tv_information = (TextView) findViewById(R.id.tv_information);
		tv_information.setMovementMethod(new ScrollingMovementMethod());
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
				mListener.onLocationChanged(amapLocation);// ��ʾϵͳС����
			}else{
				Log.i(TAG,"mListener==null");
			}
			set_information("����="+String.valueOf(amapLocation.getLatitude()));
			set_information("γ��="+String.valueOf(amapLocation.getLongitude()));
			set_information("�ص�="+amapLocation.getAddress());
			Log.i(TAG,"����="+amapLocation.getLatitude());
			Log.i(TAG,"γ��="+amapLocation.getLongitude());
			Log.i(TAG,"�ص�="+amapLocation.getAddress());
		}else{
			Log.e(TAG,"amapLocation==null");
		}
	}
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
	/*LocationSource*************************************************/
	
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
	/*AMapLocalWeatherListener************************************/
	int i=1;
	public void set_information(String s){
		str_information = str_information+s+"\n";
		tv_information.setText(str_information);
		tv_information.setMaxLines(i++);
	}
	/*************************************************************/
}
