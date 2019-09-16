package com.one.geoar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
    static String selectedlan = "en";
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

        try{
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toggle = new ActionBarDrawerToggle(this,drawer,R.string.drawer_open,R.string.drawer_close);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toggle.syncState();
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        final String[] permissions = PermissionUtil.getPermissionsForArFeatures(sampleData.getArFeatures());

        NavigationView navigationView = findViewById(R.id.setting_drawer_view);
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
        arrayList.add("영어");
        arrayList.add("일본어");
        arrayList.add("중국어");
        arrayList.add("스페인어");
        arrayList.add("베트남어");

        arrayAdapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item,
                arrayList);

        spinner2 = findViewById(R.id.spinner2);
        spinner2.setAdapter(arrayAdapter);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ArchitectView architectView = new ArchitectView(getApplicationContext());

                switch (arrayList.get(i)){
                    case "영어":
                        selectedlan = "en";
                        architectView.callJavascript("World.getLang(en)");
                        break;
                    case "일본어":
                        selectedlan = "ja";
                        architectView.callJavascript("World.getLang(ja)");
                        break;
                    case "중국어":
                        selectedlan = "zh-TW";
                        architectView.callJavascript("World.getLang(zh-TW)");
                        break;
                    case "스페인어":
                        selectedlan = "es";
                        architectView.callJavascript("World.getLang(es)");
                        break;
                    case "베트남어":
                        selectedlan = "vi";
                        architectView.callJavascript("World.getLang(vi)");
                        break;
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedlan = "en";
            }
        });
    }

}
