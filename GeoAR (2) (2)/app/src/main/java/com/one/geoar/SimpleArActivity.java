package com.one.geoar;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.one.geoar.util.SampleData;
import com.wikitude.architect.ArchitectStartupConfiguration;
import com.wikitude.architect.ArchitectView;

import java.io.IOException;

public class SimpleArActivity extends AppCompatActivity {
    private String SAMPLES_ROOT = "samples/";

    protected ArchitectView architectView;
    private String arExperience;
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geoar);
        Intent intent = getIntent();
        if(!intent.hasExtra("sampleData")){
            throw new IllegalStateException(getClass().getSimpleName()+"sampleData");
        }
        final SampleData sampleData = (SampleData) intent.getSerializableExtra("sampleData");

        architectView = findViewById(R.id.architectview);
        ArchitectStartupConfiguration config = new ArchitectStartupConfiguration();
        config.setLicenseKey(getString(R.string.wikitude_license_key));
        config.setCameraPosition(sampleData.getCameraPosition());
        config.setCameraResolution(sampleData.getCameraResolution());
        config.setCameraFocusMode(sampleData.getCameraFocusMode());
        config.setCamera2Enabled(sampleData.isCamera2Enabled());
        architectView.onCreate(config);
        arExperience = sampleData.getPath();
        drawer = findViewById(R.id.ar_drawer);

        WebView.setWebContentsDebuggingEnabled(true);

        try{
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toggle = new ActionBarDrawerToggle(this, drawer,R.string.drawer_open,R.string.drawer_close);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toggle.syncState();
        }catch (NullPointerException e){
            e.printStackTrace();
        }



        NavigationView navigationView = findViewById(R.id.ar_drawer_view);
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

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        architectView.onPostCreate();

        try{
            architectView.load(SAMPLES_ROOT+arExperience);

        }catch (IOException e){
            Toast.makeText(this,getString(R.string.error_loading_ar_experience),Toast.LENGTH_LONG).show();
            Log.e(SimpleArActivity.class.getSimpleName(),"Exception while loading arExperience"+arExperience+"."+e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        architectView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        architectView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        architectView.clearCache();
        architectView.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(toggle.onOptionsItemSelected(item)){
            return false;
        }
        return super.onOptionsItemSelected(item);
    }
}
