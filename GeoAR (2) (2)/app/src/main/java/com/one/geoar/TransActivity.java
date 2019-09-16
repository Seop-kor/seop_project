package com.one.geoar;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.one.geoar.util.PermissionUtil;
import com.one.geoar.util.SampleData;
import com.wikitude.architect.ArchitectView;
import com.wikitude.common.permission.PermissionManager;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;


public class TransActivity extends AppCompatActivity {
    EditText translationText;//번역할텍스트
    ImageView translationButton;//번역하기버튼
    ImageView voiceButton;//음성인식버튼
    TextView resultText;//번역결과창
    Intent intent;
    SpeechRecognizer mRecognizer;
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;
    private final PermissionManager permissionManager = ArchitectView.getPermissionManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trans);
        drawer = findViewById(R.id.trans_drawer);
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

        NavigationView navigationView = findViewById(R.id.trans_drawer_view);
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
                    permissionManager.checkPermissions(TransActivity.this, permissions, PermissionManager.WIKITUDE_PERMISSION_REQUEST, new PermissionManager.PermissionManagerCallback() {
                        @Override
                        public void permissionsGranted(int i) {
                            Intent it = new Intent(TransActivity.this, sampleData.getActivityClass());
                            it.putExtra("sampleData",sampleData);
                            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(it);
                            finish();
                        }

                        @Override
                        public void permissionsDenied(@NonNull String[] strings) {
                            Toast.makeText(TransActivity.this,getString(R.string.permissions_denied)+ Arrays.toString(strings),Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void showPermissionRationale(int i, @NonNull String[] strings) {
                            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(TransActivity.this);
                            alertBuilder.setCancelable(true);
                            alertBuilder.setTitle(R.string.permission_rationale_title);
                            alertBuilder.setMessage(getString(R.string.permission_rationale_text)+Arrays.toString(permissions));
                            alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    permissionManager.positiveRationaleResult(i,permissions);
                                }
                            });

                            AlertDialog alert = alertBuilder.create();
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
        if (Build.VERSION.SDK_INT >= 23)//permission체크 android23이상이면 퍼미션 체크 알림창
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.RECORD_AUDIO)) {

                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_RECORD_AUDIO
                    );
                }
            }
        //음성이 인식될 때 사용되는 INTENT 설정
        //사용자에게 음성을 요구, 인식기를 통하여 전송하는 역할시작
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //음성 인식을 위한 음성 인식기 의도에 사용되는 여분의 키
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        //음성을 번역할 언어 설정(한국어 ko-KR, 영어 en-US 프랑스어 fr-FR)
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR");

        //음성이 인식되는 부분
        //새로운 SpeechRecognizer를 만드는 메소드
        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        //모든 콜백을 수신하는 리스너 설정
        mRecognizer.setRecognitionListener(recognitionListener);


        translationText = findViewById(R.id.translationText);
        translationButton = findViewById(R.id.translationButton);
        resultText = findViewById(R.id.resultText);
        voiceButton = findViewById(R.id.voiceButton);
        voiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //음성 듣기 시작
                mRecognizer.startListening(intent);
            }
        });
        translationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Trans translateTask = new Trans(translationText.getText().toString(), SettingActivity.selectedlan);
                translateTask.execute();

            }
        });
    }

    private RecognitionListener recognitionListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
            //사용자가 말하기 시작할 준비가 되면 호출됨
        }

        @Override
        public void onBeginningOfSpeech() {
            //사용자가 말하기 시작할 때 호출됨
        }

        @Override
        public void onRmsChanged(float v) {
            //입력받는 소리크기 알려줌
        }

        @Override
        public void onBufferReceived(byte[] bytes) {
            //사용자가 말을 시작한 후 인식된 단어를 버퍼에 담음
        }

        @Override
        public void onEndOfSpeech() {
            //사용자가 말하는 것이 중지되면 호출됨
        }

        @Override
        public void onError(int i) {
            //인식 오류, 네트워크 오류가 발생될 때 호출됨
            translationText.setText("음성이 인식되지 않습니다. 다시 시도해주십시오");

        }

        @Override
        public void onResults(Bundle bundle) {
            String key = "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = bundle.getStringArrayList(key);

            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);

            translationText.setText(rs[0]);
        }

        @Override
        public void onPartialResults(Bundle bundle) {
            //부분 인식 결과를 사용할 때 호출됨
        }

        @Override
        public void onEvent(int i, Bundle bundle) {
            //이후 이벤트를 추가하기 위해 예약
        }
    };

    class Trans extends AsyncTask<String, Void, String> {
        String text;
        String source;

        public Trans(String text, String source) {
            this.text = text;
            this.source = source;
        }

        @Override
        protected String doInBackground(String... strings) {
            String clientId = "mvcOoMvo1rUtD3e1fNDW";//애플리케이션 클라이언트 아이디값";
            String clientSecret = "B7oKMS1pj8";//애플리케이션 클라이언트 시크릿값";
            try {
                String text = URLEncoder.encode(this.text, "UTF-8");
                String apiURL = "https://openapi.naver.com/v1/papago/n2mt";
                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("X-Naver-Client-Id", clientId);
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
                // post request
                String postParams = "source=" + source + "&target=ko&text=" + text;
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(postParams);
                wr.flush();
                wr.close();
                int responseCode = con.getResponseCode();
                BufferedReader br;
                if (responseCode == 200) { // 정상 호출
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                } else {  // 에러 발생
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();
                String tt = response.toString();
                tt = tt.replace("{", "").replace("}", "").replace("\"", "").replace("\n", "");
                String[] textPairs = tt.split(",");

                for (String pair : textPairs) {
                    String[] keyValuePair = pair.split(":", 2);

                    if (keyValuePair.length == 2) {
                        if (keyValuePair[0].equals("translatedText"))
                            resultText.setText(keyValuePair[1]);
                    }
                }
            } catch (Exception e) {
                Log.w("에러", e);
            }
            return null;
        }
    }
}

