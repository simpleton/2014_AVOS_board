package com.sim.board;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVGeoPoint;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.sim.board.model.Club;
import com.sim.board.model.Venue;

import java.util.ArrayList;
import java.util.List;

/**
 * AMapV2地图中简单介绍显示定位小蓝点
 */
public class LocationSourceActivity extends BaseActivity implements LocationSource,
        AMapLocationListener {

    private static final String TAG = "LocationSourceActivity";
	private AMap aMap;
	private MapView mapView;
	private OnLocationChangedListener mListener;
	private LocationManagerProxy mAMapLocationManager;
	private Marker marker;// 定位雷达小图标

    private boolean isResized = false;
    private static final int VENUE_SIZE = 800;

    private static final String MODE_INTENT = "mode_intent";

    private static final int MODE_DEFAUL = 0;
    private static final int MODE_VENUE = 1;
    private static final int MODE_MISSION = 2;

    private int mode = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        AVAnalytics.trackAppOpened(getIntent());
		super.onCreate(savedInstanceState);

		setContentView(R.layout.locationsource_activity);
        getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_background));
        /*
         * 设置离线地图存储目录，在下载离线地图或初始化地图设置;
         * 使用过程中可自行设置, 若自行设置了离线地图存储的路径，
         * 则需要在离线地图下载和使用地图页面都进行路径设置
         * */
	    //Demo中为了其他界面可以使用下载的离线地图，使用默认位置存储，屏蔽了自定义设置
//        MapsInitializer.sdcardDir =OffLineMapUtils.getSdCacheDir(this);
		mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);// 此方法必须重写
		init();
        mode = getIntent().getIntExtra(MODE_INTENT, 0);

    }

	/**
	 * 初始化
	 */
	private void init() {
		if (aMap == null) {
			aMap = mapView.getMap();
			setUpMap();
		}
	}

	/**
	 * 设置一些amap的属性
	 */
	private void setUpMap() {

		ArrayList<BitmapDescriptor> giflist = new ArrayList<BitmapDescriptor>();
		giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.point1));
		giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.point2));
		giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.point3));
		giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.point4));
		giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.point5));
		giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.point6));
		marker = aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
				.icons(giflist).period(50));
		// 自定义系统定位小蓝点
		MyLocationStyle myLocationStyle = new MyLocationStyle();
		myLocationStyle.myLocationIcon(BitmapDescriptorFactory
				.fromResource(R.drawable.location_marker));// 设置小蓝点的图标
		myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
		myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180));// 设置圆形的填充颜色

		// myLocationStyle.anchor(int,int)//设置小蓝点的锚点
		myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细

		aMap.setMyLocationStyle(myLocationStyle);
		aMap.setMyLocationRotateAngle(180);
		aMap.setLocationSource(this);// 设置定位监听
		aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
		aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
		//设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种 
		aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);


	}

	 
	
	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
        isResized = false;
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
		deactivate();
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
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	/**
	 * 此方法已经废弃
	 */
	@Override
	public void onLocationChanged(Location location) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	/**
	 * 定位成功后回调函数
	 */
	@Override
	public void onLocationChanged(AMapLocation aLocation) {
		if (mListener != null && aLocation != null) {
			mListener.onLocationChanged(aLocation);// 显示系统小蓝点
			marker.setPosition(new LatLng(aLocation.getLatitude(), aLocation
					.getLongitude()));// 定位雷达小图标
			float bearing = aMap.getCameraPosition().bearing;
			aMap.setMyLocationRotateAngle(bearing);// 设置小蓝点旋转角度
            if (!isResized) {
                isResized = true;
                changeCamera(CameraUpdateFactory.zoomTo(16f));
                fetchVenue(0, new AVGeoPoint(aLocation.getLatitude(), aLocation.getLongitude()));
            }


        }
	}

    /**
     * 根据动画按钮状态，调用函数animateCamera或moveCamera来改变可视区域
     */
    private void changeCamera(CameraUpdate update) {
        //aMap.animateCamera(update, 1000, null);
        aMap.moveCamera(update);
    }
	/**
	 * 激活定位
	 */
	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;
		if (mAMapLocationManager == null) {
			mAMapLocationManager = LocationManagerProxy.getInstance(this);
			/*
			 * mAMapLocManager.setGpsEnable(false);
			 * 1.0.2版本新增方法，设置true表示混合定位中包含gps定位，false表示纯网络定位，默认是true Location
			 * API定位采用GPS和网络混合定位方式
			 * ，第一个参数是定位provider，第二个参数时间最短是2000毫秒，第三个参数距离间隔单位是米，第四个参数是定位监听者
			 */
			mAMapLocationManager.requestLocationUpdates(
					LocationProviderProxy.AMapNetwork, 2000, 10, this);

        }
	}

	/**
	 * 停止定位
	 */
	@Override
	public void deactivate() {
		mListener = null;
		if (mAMapLocationManager != null) {
			mAMapLocationManager.removeUpdates(this);
			mAMapLocationManager.destory();
		}
		mAMapLocationManager = null;
	}

    public void fetchClub(final int limit) {
        FindCallback<AVObject> clubCallback = new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> avObjects, AVException e) {
                if (e == null) {
                    parserData(avObjects);
                } else {
                    e.printStackTrace();
                }
            }
        };

        fetchFromAVOS(limit, "Club", clubCallback);
    }

    public void fetchVenue(final int limit, AVGeoPoint geopoint) {
        FindCallback<AVObject> venueCallback = new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> avObjects, AVException e) {
                if (e == null) {
                    parserData(avObjects);
                } else {
                    e.printStackTrace();
                }
            }
        };

        AVQuery<AVObject> query = AVQuery.getQuery("Venue");
        query.setLimit(limit);
        query.whereNear("location", geopoint);
        query.include("club");
        query.findInBackground(venueCallback);
    }

    private void fetchFromAVOS(final int limit, final String name, FindCallback<AVObject> callback) {
        AVQuery<AVObject> query = new AVQuery<AVObject>(name);
        query.limit(limit);
        query.findInBackground(callback);
    }

    private void fetchNearDataFromAVOS(final int limit, final String name, AVGeoPoint geoPoint, FindCallback<AVObject> callback) {
        Log.i(TAG, "fetchNear" + name + "geo:" + geoPoint.getLatitude() + ":" + geoPoint.getLongitude() );
        AVQuery<AVObject> query = new AVQuery<AVObject>("PlaceObject");
        query.whereNear("location", geoPoint);
        query.setLimit(limit);
        query.findInBackground(callback);
    }

    public void parseMission(List<AVObject> list) {
        if (list == null) return;
        for (AVObject avObject : list) {
            
        }
    }

    public void parserData(List<AVObject> list) {
        if (list == null) return;
        for (AVObject avobjec : list) {
            Venue venue = new Venue();
            venue.address = (String) avobjec.get("address");
            AVObject clubObject  = avobjec.getAVObject("club");
            venue.location = avobjec.getAVGeoPoint("location");
            venue.name = (String) avobjec.get("name");

            Club club  = new Club();
            club.color = "#" + (String) clubObject.get("color");
            club.name = (String) clubObject.get("name");
            club.logo = (AVFile) clubObject.get("logo");

            venue.club = club;
            Log.i(TAG, venue.toString());

            drawVenue(venue);
        }
    }

    public void drawVenue(Venue venue) {
        int color = Color.parseColor(venue.club.color);
        AVGeoPoint avGeoPoint = venue.location;
        Circle circle = aMap.addCircle(new CircleOptions().center(new LatLng(avGeoPoint.getLatitude(), avGeoPoint.getLongitude()))
                .radius(VENUE_SIZE).strokeColor(Color.BLUE).fillColor(color)
                .strokeWidth(1));
        circle.setFillColor(Color.argb(127, Color.red(color),
                Color.green(color), Color.blue(color)));
        addMarkersToMap(venue);
/*        Bitmap bitmap = null;
        try {
            bitmap = Utils.Bytes2Bimap(venue.club.logo.getData());
        } catch (AVException e) {
            e.printStackTrace();
        }*/
  //      BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);

    }

    private void addMarkersToMap(Venue venue)  {
        AVGeoPoint avGeoPoint = venue.location;
        //声明一个动画帧集合。
        ArrayList giflist = new ArrayList();
        giflist.add(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        giflist.add(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_RED));
        giflist.add(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        //设置远小近大效果,2.1.0版本新增；设置刷新一次图片资源的周期。
        Marker  marker = aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
                .position(new LatLng(avGeoPoint.getLatitude(), avGeoPoint.getLongitude())).title(venue.club.name)
                .snippet(venue.name).icons(giflist)
                .perspective(true).draggable(true).period(50));
        marker.showInfoWindow();// 设置默认显示一个infowinfow
    }
}
