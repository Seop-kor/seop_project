package com.one.geoar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.one.geoar.util.PermissionUtil;
import com.one.geoar.util.SampleData;
import com.wikitude.architect.ArchitectView;
import com.wikitude.common.permission.PermissionManager;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class NaviActivity extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.OnInfoWindowClickListener {
    private GoogleMap mGoogleMap = null;
    private Marker currentMarker = null;
    private final PermissionManager permissionManager = ArchitectView.getPermissionManager();

    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초


    // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;


    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소


    Location mCurrentLocatiion;
    LatLng currentPosition;
    LatLng ll;
    public CheckBox korea, china, japan, meat, buffet, snack;
    public Button button, button2;
    MarkerOptions makerOptions;

    String skorea = null; // 문자열 초기화는 빈문자열로 하자
    String schina = null; // 문자열 초기화는 빈문자열로 하자
    String sjapan = null; // 문자열 초기화는 빈문자열로 하자
    String smeat = null; // 문자열 초기화는 빈문자열로 하자
    String sbuffet = null; // 문자열 초기화는 빈문자열로 하자
    String ssnack = null; // 문자열 초기화는 빈문자열로 하자


    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    public Location location;


    private View mLayout;  // Snackbar 사용하기 위해서는 View가 필요합니다.
    // (참고로 Toast에서는 Context가 필요했습니다.)

    public double lat, lon;

    boolean initem = false, inName = false, inY = false, inX = false, inbizcnd = false, incode = false, asynctask = true;


    String upso = null, Y = null, X = null, bizcnd = null, scode = null;

    int code = 0;
    int i=0;

    double Lat, Lon;

    DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_navi);
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        mLayout = findViewById(R.id.layout_main);
        korea = findViewById(R.id.korea);
        china = findViewById(R.id.china);
        japan = findViewById(R.id.japan);
        meat = findViewById(R.id.meat);
        buffet = findViewById(R.id.buffet);
        snack = findViewById(R.id.snack);
        drawer = findViewById(R.id.navi_drawer);
        NavigationView navigationView = findViewById(R.id.navi_drawer_view);
        Intent it = getIntent();
        if(!it.hasExtra("sampleData")){
            throw new IllegalStateException(getClass().getSimpleName()+"sampleData");
        }
        final SampleData sampleData = (SampleData) it.getSerializableExtra("sampleData");

//        try{
//            getSupportActionBar().setDisplayShowTitleEnabled(false);
//            toggle = new ActionBarDrawerToggle(this,drawer,R.string.drawer_open,R.string.drawer_close);
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            toggle.syncState();
//        }catch (NullPointerException e){
//            e.printStackTrace();
//        }

        final String[] permissions = PermissionUtil.getPermissionsForArFeatures(sampleData.getArFeatures());
        SettingActivity.change(navigationView);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if(id == R.id.menu_drawer_navi){
                    Intent it = new Intent(getApplicationContext(),NaviActivity.class);
                    it.putExtra("sampleData",sampleData);
                    startActivity(it);
                    finish();
                }else if(id == R.id.menu_drawer_info){
                    permissionManager.checkPermissions(NaviActivity.this, permissions, PermissionManager.WIKITUDE_PERMISSION_REQUEST, new PermissionManager.PermissionManagerCallback() {
                        @Override
                        public void permissionsGranted(int i) {
                            Intent it = new Intent(NaviActivity.this, sampleData.getActivityClass());
                            it.putExtra("sampleData",sampleData);
                            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(it);
                            finish();
                        }

                        @Override
                        public void permissionsDenied(@NonNull String[] strings) {
                            Toast.makeText(NaviActivity.this,getString(R.string.permissions_denied)+ Arrays.toString(strings),Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void showPermissionRationale(int i, @NonNull String[] strings) {
                            android.app.AlertDialog.Builder alertBuilder = new android.app.AlertDialog.Builder(NaviActivity.this);
                            alertBuilder.setCancelable(true);
                            alertBuilder.setTitle(R.string.permission_rationale_title);
                            alertBuilder.setMessage(getString(R.string.permission_rationale_text)+ Arrays.toString(permissions));
                            alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    permissionManager.positiveRationaleResult(i,permissions);
                                }
                            });

                            android.app.AlertDialog alert = alertBuilder.create();
                            alert.show();
                        }
                    });
                }else if(id == R.id.menu_drawer_trans){
                    Intent it = new Intent(getApplicationContext(),TransActivity.class);
                    it.putExtra("sampleData",sampleData);
                    startActivity(it);
                    finish();
                }else if(id == R.id.menu_drawer_setting){
                    Intent it = new Intent(getApplicationContext(),SettingActivity.class);
                    it.putExtra("sampleData",sampleData);
                    startActivity(it);
                    finish();
                }
                return false;
            }
        });

        Log.d(TAG, "onCreate");


        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        StrictMode.enableDefaults();

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGoogleMap.clear();
            }
        });

    }


    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                //location = locationList.get(0);

                currentPosition
                        = new LatLng(location.getLatitude(), location.getLongitude());

                Lat = location.getLatitude();
                Lon = location.getLongitude();

                String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "위도:" + String.valueOf(Lat)
                        + " 경도:" + String.valueOf(Lon);

                ll = new LatLng(Lat,Lon);
                if(i == 0){
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(ll, 16);
                    mGoogleMap.moveCamera(cameraUpdate);
                    i=1;
                }


                Log.d(TAG, "onLocationResult : " + markerSnippet);




                //현재 위치에 마커 생성하고 이동

                mCurrentLocatiion = location;

            }

        }


    };


    private void startLocationUpdates() {

        if (!checkLocationServicesStatus()) {

            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        } else {

            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);


            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {

                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }


            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            if (checkPermission())
                mGoogleMap.setMyLocationEnabled(true);

        }

    }

    private boolean isInstallApp() {
        Intent intent = getPackageManager().getLaunchIntentForPackage("net.daum.android.map");

        if (intent == null) {
            //미설치
            return false;
        } else {
            //설치
            return true;
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (isInstallApp()) {
            double u_lon = currentPosition.longitude;
            double u_lat = currentPosition.latitude;
            double a_lon = marker.getPosition().longitude;
            double a_lat = marker.getPosition().latitude;
            String url = "daummaps://route?sp=" + u_lat + "," + u_lon + "&ep=" + a_lat + "," + a_lon + "&by=FOOT";

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } else {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=net.daum.android.map")));
        }

    }

    public class XmlAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {

        }

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... strings) {
            if (asynctask == true) {
                int i;
                for(i=1;i<=9000;i+=1000){
                    try {
                        String URL = "http://openapi.seoul.go.kr:8088/414f7473677332643132374452676c78/xml/CrtfcUpsoInfo/"+ i + "/" + (i + 999) + "/";
                        java.net.URL url = new URL(URL); //검색 URL부분

                        XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
                        XmlPullParser parser = parserCreator.newPullParser();

                        parser.setInput(url.openStream(), null);

                        if (korea.isChecked()) skorea = korea.getText().toString();
                        if (china.isChecked()) schina = china.getText().toString();
                        if (japan.isChecked()) sjapan = japan.getText().toString();
                        if (meat.isChecked()) smeat = meat.getText().toString();
                        if (buffet.isChecked()) sbuffet = buffet.getText().toString();
                        if (snack.isChecked()) ssnack = snack.getText().toString();

                        int parserEvent = parser.getEventType();
                        System.out.println("파싱시작합니다.");


                        while (parserEvent != XmlPullParser.END_DOCUMENT) {
                            switch (parserEvent) {
                                case XmlPullParser.START_TAG://parser가 시작 태그를 만나면 실행
                                    if (parser.getName().equals("UPSO_NM")) { //title 만나면 내용을 받을수 있게 하자
                                        inName = true;
                                    }
                                    if (parser.getName().equals("Y_DNTS")) { //address 만나면 내용을 받을수 있게 하자
                                        inY = true;
                                    }
                                    if (parser.getName().equals("X_CNTS")) { //mapx 만나면 내용을 받을수 있게 하자
                                        inX = true;
                                    }
                                    if (parser.getName().equals("BIZCND_CODE_NM")) { //message 태그를 만나면 에러 출력
                                        inbizcnd = true;
                                    }
                                    if (parser.getName().equals("CGG_CODE")) { //message 태그를 만나면 에러 출력
                                        incode = true;
                                    }
                                    if (parser.getName().equals("message")) { //message 태그를 만나면 에러 출력
                                        //여기에 에러코드에 따라 다른 메세지를 출력하도록 할 수 있다.
                                    }
                                    break;

                                case XmlPullParser.TEXT://parser가 내용에 접근했을때
                                    if (inName) { //isTitle이 true일 때 태그의 내용을 저장.
                                        upso = parser.getText();
                                        inName = false;
                                    }
                                    if (inY) { //isAddress이 true일 때 태그의 내용을 저장.
                                        Y = parser.getText();
                                        lat = Double.valueOf(Y);
                                        inY = false;
                                    }
                                    if (inX) { //isMapx이 true일 때 태그의 내용을 저장.
                                        X = parser.getText();
                                        lon = Double.valueOf(X);
                                        inX = false;
                                    }
                                    if (inbizcnd) {
                                        bizcnd = parser.getText();
                                        inbizcnd = false;
                                    }
                                    if (incode) {
                                        scode = parser.getText();
                                        code = Integer.parseInt(scode);
                                        incode = false;
                                    }
                                    break;
                                case XmlPullParser.END_TAG:
                                    if (parser.getName().equals("row")) {
                                        if (code == 3220000 || code == 3210000 || code == 3230000 || code == 3160000 || code == 3180000 || code == 3140000 || code == 3170000) {
                                            if (bizcnd.equals(skorea)) {
                                                publishProgress();
                                                Thread.sleep(100);

                                            }
                                            if (bizcnd.equals(schina)) {
                                                publishProgress();
                                                Thread.sleep(100);

                                            }
                                            if (bizcnd.equals(sjapan)) {
                                                publishProgress();
                                                Thread.sleep(100);

                                            }
                                            if (bizcnd.equals(smeat)) {
                                                publishProgress();
                                                Thread.sleep(100);

                                            }
                                            if (bizcnd.equals(sbuffet)) {
                                                publishProgress();
                                                Thread.sleep(100);

                                            }
                                            if (bizcnd.equals(ssnack)) {
                                                publishProgress();
                                                Thread.sleep(100);

                                            }
                                        }
                                    }
                                    break;
                            }
                            parserEvent = parser.next();
                        }

                    } catch (Exception e) {

                    }
                    skorea = null;
                    schina = null;
                    sjapan = null;
                    smeat = null;
                    sbuffet = null;
                    ssnack = null;
                }
                if(i==9001){
                    asynctask = false;
                }

            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            makerOptions = new MarkerOptions();
            makerOptions // LatLng에 대한 어레이를 만들어서 이용할 수도 있다.
                    .position(new LatLng(lat, lon))
                    .title(upso); // 타이틀.

            BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.marker);
            Bitmap b = bitmapdraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, 80, 90, false);
            makerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
            mGoogleMap.addMarker(makerOptions);

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mGoogleMap = googleMap;

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                asynctask = true;
                XmlAsyncTask task = new XmlAsyncTask();
                task.execute();
            }
        });

        mGoogleMap.setOnInfoWindowClickListener(this);

        Log.d(TAG, "onMapReady :");


        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
        //지도의 초기위치를 서울로 이동
        setDefaultLocation();


        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            startLocationUpdates(); // 3. 위치 업데이트 시작


        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                        ActivityCompat.requestPermissions(NaviActivity.this, REQUIRED_PERMISSIONS,
                                PERMISSIONS_REQUEST_CODE);
                    }
                }).show();


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }


        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                Log.d(TAG, "onMapClick :");
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        if (checkPermission()) {

            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

            if (mGoogleMap != null)
                mGoogleMap.setMyLocationEnabled(true);

        }


    }


    @Override
    protected void onStop() {

        super.onStop();

        if (mFusedLocationClient != null) {

            Log.d(TAG, "onStop : call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }


    public String getCurrentAddress(LatLng latlng) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }


        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }

    }


    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {


        if (currentMarker != null) currentMarker.remove();


        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);

        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.marker);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 80, 90, false);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18));

        currentMarker = mGoogleMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        mGoogleMap.moveCamera(cameraUpdate);

    }


    public void setDefaultLocation() {


        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(Lon, Lat);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요";

        if (currentMarker != null) currentMarker.remove();

//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(DEFAULT_LOCATION);
//        markerOptions.title(markerTitle);
//        markerOptions.snippet(markerSnippet);
//        markerOptions.draggable(true);



    }


    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
    private boolean checkPermission() {

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        return false;

    }


    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if (check_result) {

                // 퍼미션을 허용했다면 위치 업데이트를 시작합니다.
                startLocationUpdates();
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {


                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();

                } else {


                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();
                }
            }

        }
    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(NaviActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d(TAG, "onActivityResult : GPS 활성화 되있음");


                        needRequest = true;

                        return;
                    }
                }

                break;
        }
    }

}