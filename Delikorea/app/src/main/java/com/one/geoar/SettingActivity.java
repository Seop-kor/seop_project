package com.one.geoar;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.one.geoar.util.PermissionUtil;
import com.one.geoar.util.SampleData;
import com.wikitude.architect.ArchitectView;
import com.wikitude.common.permission.PermissionManager;

import java.util.ArrayList;
import java.util.Arrays;

public class SettingActivity extends AppCompatActivity {

    private Spinner spinner2;
    ArrayList<String> arrayList;
    ArrayAdapter<String> arrayAdapter;
    static String selectedlan = "ko";
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    private final PermissionManager permissionManager = ArchitectView.getPermissionManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        drawer = findViewById(R.id.setting_drawer);

        Intent it = getIntent();
        if(!it.hasExtra("sampleData")){
            throw new IllegalStateException(getClass().getSimpleName()+"sampleData");
        }
        final SampleData sampleData = (SampleData) it.getSerializableExtra("sampleData");

        final NavigationView navigationView = findViewById(R.id.setting_drawer_view);
//        try{
//            getSupportActionBar().setDisplayShowTitleEnabled(false);
//            toggle = new ActionBarDrawerToggle(this,drawer,R.string.drawer_open,R.string.drawer_close);
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            toggle.syncState();
//        }catch (NullPointerException e){
//            e.printStackTrace();
//        }

        final String[] permissions = PermissionUtil.getPermissionsForArFeatures(sampleData.getArFeatures());

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
                    permissionManager.checkPermissions(SettingActivity.this, permissions, PermissionManager.WIKITUDE_PERMISSION_REQUEST, new PermissionManager.PermissionManagerCallback() {
                        @Override
                        public void permissionsGranted(int i) {
                            Intent it = new Intent(SettingActivity.this, sampleData.getActivityClass());
                            it.putExtra("sampleData",sampleData);
                            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(it);
                            finish();
                        }

                        @Override
                        public void permissionsDenied(@NonNull String[] strings) {
                            Toast.makeText(SettingActivity.this,getString(R.string.permissions_denied)+ Arrays.toString(strings),Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void showPermissionRationale(int i, @NonNull String[] strings) {
                            android.app.AlertDialog.Builder alertBuilder = new android.app.AlertDialog.Builder(SettingActivity.this);
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
                    startActivity(it);
                    finish();
                }
                return false;
            }
        });

        arrayList = new ArrayList<>();
        arrayList.add("한국어");
        arrayList.add("English");
        arrayList.add("日本語");
        arrayList.add("中國語");
        arrayList.add("Español");
        arrayList.add("tiếng Việt");

        arrayAdapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item,
                arrayList);

        spinner2 = findViewById(R.id.spinner2);
        spinner2.setAdapter(arrayAdapter);
        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        int a = pref.getInt("aa",-1);
        spinner2.setSelection(a);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int a = i;
                SharedPreferences prefs = getSharedPreferences("pref",Activity.MODE_PRIVATE);
                SharedPreferences.Editor prefse = prefs.edit();
                prefse.putInt("aa",a);
                prefse.commit();
                switch (arrayList.get(i)){
                    case "한국어":
                        selectedlan = "ko";
                        break;
                    case "English":
                        selectedlan = "en";
                        break;
                    case "日本語":
                        selectedlan = "ja";
                        break;
                    case "中國語":
                        selectedlan = "zh-TW";
                        break;
                    case "Español":
                        selectedlan = "es";
                        break;
                    case "tiếng Việt":
                        selectedlan = "vi";
                        break;
                }

                change(navigationView);

            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedlan = "ko";
            }
        });
    }

    static public void change(NavigationView navigationView){
        Menu menu = navigationView.getMenu();
        MenuItem navigation = menu.findItem(R.id.menu_drawer_navi);
        MenuItem translate = menu.findItem(R.id.menu_drawer_trans);
        MenuItem setting = menu.findItem(R.id.menu_drawer_setting);
        switch (selectedlan){
            case "ko":
                navigation.setTitle("길 찾기");
                translate.setTitle("번역");
                setting.setTitle("설정");
                break;
            case "en":
                navigation.setTitle("Navigation");
                translate.setTitle("Translate");
                setting.setTitle("Setting");
                break;
            case "ja":
                navigation.setTitle("道を探すこと");
                translate.setTitle("翻訳機");
                setting.setTitle("設定");
                break;
            case "zh-TW":
                navigation.setTitle("尋路");
                translate.setTitle("翻譯");
                setting.setTitle("設定");
                break;
            case "es":
                navigation.setTitle("hallazgo");
                translate.setTitle("Traductor");
                setting.setTitle("establecimiento");
                break;
            case "vi":
                navigation.setTitle("sự tìm đường");
                translate.setTitle("máy biên dịch");
                setting.setTitle("sự thiết lập");
                break;
        }
    }

}
