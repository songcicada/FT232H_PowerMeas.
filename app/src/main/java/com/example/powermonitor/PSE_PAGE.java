package com.example.powermonitor;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentTransaction;

import com.ftdi.j2xx.D2xxManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class PSE_PAGE extends AppCompatActivity{

    private I2C m_ftdi;
    D2xxManager.FtDeviceInfoListNode m_DeviceList;
    private static final int MESSAGE_TIMER_START  = 100;
    private static final int MESSAGE_TIMER_REPEAT = 101;
    private static final int MESSAGE_TIMER_STOP   = 102;
    private static final PSE_VALUE_USER pse_value_user         = new PSE_VALUE_USER();
    private static final PSE_VALUE_ENGINEER pse_value_engineer = new PSE_VALUE_ENGINEER();
    TimerHandler timerHandler = null;
    private int Measure_Count = 0;
    private final int PSE_VALUE_USER     = 1;
    private final int PSE_VALUE_ENGINEER = 2;
    private       int FRAGMENT_SELECTOR  = 1; // Default : pse_value_user

    // PSE 계산식
    double PS1_I   = 0f;
    double PS2_I   = 0f;
    double PS3_I   = 0f;
    double PSE1_I  = 0f;
    double PSE2_I  = 0f;
    double PSE3_I  = 0f;
    double PSE1_V  = 0f;
    double PSE2_V  = 0f;
    double PSE3_V  = 0f;
    double PS3_V   = 0f;
    double PS3_OCP = 0f;
    double PSE_NC  = 0f;
    double[] PS_IV              = new double[12];
    ArrayList<String> CSV_Data  = new ArrayList<String>();


    // 이미지
    private ImageView btn_clear;
    private ImageView btn_start;
    private ImageView btn_stop;
    private TextView  MeasurementLog = null;
    private FloatingActionButton btn_share;

    private String Filename;
    private String strFile_Dir;
    private String strLog_format;

    private Boolean DevOpen;
    private Animation rotate;
    private Animation glitter_btn;
    int Start_Frequency       = 500;
    int Measurement_frequency = 2000; // Engineer Mode에서는 간격 수정 가능

    Intent intent_action_search;
    Intent intent_action_question;
    Intent intent_sharing;
    Bundle bundle;
    File csvFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pse_page);

        // Action Bar 설정
        getSupportActionBar().setTitle("");

        // 초기 화면 세팅
        bundle                    = new Bundle();
        timerHandler              = new TimerHandler();
        m_ftdi                    = new I2C(this);
        intent_action_search      = new Intent(this, Action_Search.class);
        intent_action_question    = new Intent(this, Action_Question.class);
        intent_sharing            = new Intent(Intent.ACTION_SEND);
        m_DeviceList = m_ftdi.Ft232_OpenDevice();
        if (m_DeviceList != null) {DevOpen = true;  m_ftdi.I2C_Init();}
        else                      {DevOpen = false; Intent myintent = new Intent(PSE_PAGE.this, Main.class); startActivity(myintent); finish();}
                                                    // Debug시 Intent ~ finish(); 주석처리
        btn_clear    = (ImageView)findViewById(R.id.btn_Clear);
        btn_start      = (ImageView)findViewById(R.id.btn_start);
        btn_stop       = (ImageView)findViewById(R.id.btn_stop);
        btn_share      = (FloatingActionButton)findViewById(R.id.btn_share);
        MeasurementLog = (TextView)findViewById(R.id.measureLog);
        MeasurementLog.setVerticalScrollBarEnabled(true);
        MeasurementLog.setMovementMethod(new ScrollingMovementMethod());

        final Switch switchButton = (Switch) findViewById(R.id.sb_use_listener);

        defaultView();
        callFragment_Value(PSE_VALUE_USER); // Default는 pse_value_user

        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    FRAGMENT_SELECTOR = PSE_VALUE_ENGINEER;
                    callFragment_Value(FRAGMENT_SELECTOR);
                    btn_clear.setImageResource(R.drawable.clear_engineer);
                    btn_start.setImageResource(R.drawable.start_engineer);
                    btn_stop.setImageResource(R.drawable.stop_engineer);
                    btn_share.setImageResource(R.drawable.share_engineer);
                    updateStatusBarColor(PSE_PAGE.this, "#72d489");
                } else {
                    FRAGMENT_SELECTOR = PSE_VALUE_USER;
                    callFragment_Value(FRAGMENT_SELECTOR);
                    btn_clear.setImageResource(R.drawable.clear_user);
                    btn_start.setImageResource(R.drawable.start_user);
                    btn_stop.setImageResource(R.drawable.stop_user);
                    btn_share.setImageResource(R.drawable.share_user);
                    updateStatusBarColor(PSE_PAGE.this, "#012d86");
                }
            }
        });
    }

    // StatusBar 색상 변경
    public static final void updateStatusBarColor (Activity context, String color) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        Window window = context.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor(color));
    }
}

    // ActionBar
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_search :
                ActionSearchClick();
                break;
            case R.id.action_question:
                ActionQuestionClick();
                break;
                // 기능 추가 가능
        }
        return super.onOptionsItemSelected(item);
    }
    // Action_Search에 데이터 보내기
    public void ActionSearchClick()
    {
        intent_action_search.putExtra("measurement_frequency",Measurement_frequency);
        startActivityForResult(intent_action_search, 1);
    }
    // Action_question 활성화
    public void ActionQuestionClick()
    {
       startActivityForResult(intent_action_question, 1);
    }

    // Action_Search에서 데이터 받기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1)
        {
            if(resultCode == RESULT_OK)
            {
                Measurement_frequency = data.getIntExtra("result_frequency", 2000);
                if(Measurement_frequency < 500)
                {
                    Toast.makeText(getApplicationContext(), "Min Frequency : 500ms", Toast.LENGTH_SHORT).show();
                    Measurement_frequency = 500;
                    MeasurementLog.append("  측정 주기 " + Measurement_frequency + "ms 설정\n");
                }else
                {
                    MeasurementLog.append("  측정 주기 " + Measurement_frequency + "ms 설정\n");
                }
            }
        }
    }


    public void btnStart(View view)
    {
        Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vib.vibrate(250);
        timerHandler.sendEmptyMessage(MESSAGE_TIMER_START);
        SimpleDateFormat timeFormat_btn = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        String time = timeFormat_btn.format(System.currentTimeMillis());
        MeasurementLog.append("  "+time);
        MeasurementLog.append("  측정 시작\n");


        if(DevOpen == true)
        {
            btn_start.setVisibility(View.INVISIBLE); btn_stop.setVisibility(View.VISIBLE);
        }
        else
        {
            btn_start.setVisibility(View.VISIBLE);   btn_stop.setVisibility(View.INVISIBLE);
        }
        btnStart_Calculation();
        PSE_Result_Log();
        btn_share.setVisibility(View.INVISIBLE);
    }



    private void btnStart_Calculation()
    {
        strLog_format = "";  // Log 데이터 초기화
        int i = 0;
        for(int array = 0; array < 12; array++) PS_IV[array] = 0; // 데이터 기록 변수 초기화


        if(DevOpen == true)
        {
            long start = System.currentTimeMillis();
            for(byte tmux = 0; tmux < 4; tmux++)
            {
                m_ftdi.I2C_TMuxSet(tmux);
                m_ftdi.uSleep(500);
                for(byte amux = 1; amux < 4; amux++)
                {
                    m_ftdi.I2C_AMuxSet(amux);
                    m_ftdi.uSleep(500);
                    double dMeas = m_ftdi.I2C_AdcRead();
                    PS_IV[i] = dMeas;
                    i++;
                }
            }
            m_ftdi.uSleep(Start_Frequency);
            PS3_I   = PS_IV[0];
            PS2_I   = PS_IV[1];
            PS1_I   = PS_IV[2];
            PSE3_V  = PS_IV[3];
            PSE2_V  = PS_IV[4];
            PSE1_V  = PS_IV[5];
            PSE3_I  = PS_IV[6];
            PSE2_I  = PS_IV[7];
            PSE1_I  = PS_IV[8];
            PS3_V   = PS_IV[9];
            PS3_OCP = PS_IV[10];
            PSE_NC  = PS_IV[11];
            long end = System.currentTimeMillis();
            // Bundle 형태로 Activity -> Fragment로 Data 전송
            sendDataBundle(PS1_I, PS2_I, PS3_I, PSE1_I, PSE2_I, PSE3_I, PSE1_V, PSE2_V, PSE3_V, PS3_V, PS3_OCP);
            MeasurementLog.append(String.format("  Elapsed Time : %d mS", (end - start)) + "\n\n");
            callFragment_Value(FRAGMENT_SELECTOR);

        }else{
            // 값이 없으므로 Default 값(0f) 전송
            sendDataBundle(PS1_I, PS2_I, PS3_I, PSE1_I, PSE2_I, PSE3_I, PSE1_V, PSE2_V, PSE3_V, PS3_V, PS3_OCP);
            Toast.makeText(getApplicationContext(), "PM Module이 연결되어 있지 않습니다.", Toast.LENGTH_LONG).show();
            timerHandler.sendEmptyMessage(MESSAGE_TIMER_STOP);
            callFragment_Value(FRAGMENT_SELECTOR);
        }
    }


    public void btnStop(View view)
    {
        Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vib.vibrate(250);
        timerHandler.sendEmptyMessage(MESSAGE_TIMER_STOP);
        SimpleDateFormat timeFormat_btn = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        String time = timeFormat_btn.format(System.currentTimeMillis());
        MeasurementLog.append("  " + time);
        btn_start.setVisibility(View.VISIBLE);
        btn_stop.setVisibility(View.INVISIBLE);
        btn_share.setVisibility(View.VISIBLE);
    }



    private void PSE_Result_Log()
    {
        double PSE1_V_total, PSE2_V_total, PSE3_V_total, PS3_V_total, PS1_I_total, PS2_I_total, PS3_I_total, PSE1_I_total, PSE2_I_total, PSE3_I_total;
        if(PSE1_V == 0 ){ PSE1_V_total = 0; } else { PSE1_V_total  = PSE1_V * 6;}
        if(PSE2_V == 0 ){ PSE2_V_total = 0; } else { PSE2_V_total  = PSE2_V * 6;}
        if(PSE3_V == 0 ){ PSE3_V_total = 0; } else { PSE3_V_total  = PSE3_V * 6;}
        if(PS3_V  == 0 ){ PS3_V_total  = 0; } else { PS3_V_total   = PS3_V * 6;}
        if(PS1_I  == 0 ){ PS1_I_total  = 0; } else { PS1_I_total   = (0.606 - PS1_I) * 100;}
        if(PS2_I  == 0 ){ PS2_I_total  = 0; } else { PS2_I_total   = (0.606 - PS2_I) * 100;}
        if(PS3_I  == 0 ){ PS3_I_total  = 0; } else { PS3_I_total   = (0.606 - PS3_I) * 100;}
        if(PSE1_I == 0 ){ PSE1_I_total = 0; } else { PSE1_I_total  = (PSE1_I - 0.606) * 100;}
        if(PSE2_I == 0 ){ PSE2_I_total = 0; } else { PSE2_I_total  = (PSE2_I - 0.606) * 100;}
        if(PSE3_I == 0 ){ PSE3_I_total = 0; } else { PSE3_I_total  = (PSE3_I - 0.606) * 100;}

        MeasurementLog.append("  PS1 전류, PSE1 전류, PSE1 전압 : " + String.format("%.3f",PS1_I_total) + "A , " + String.format("%.3f",PSE1_I_total) + "A , " +String.format("%.3f",PSE1_V_total) + "V\n");
        MeasurementLog.append("  PS2 전류, PSE2 전류, PSE2 전압 : " + String.format("%.3f",PS2_I_total) + "A , " + String.format("%.3f",PSE2_I_total) + "A , " +String.format("%.3f",PSE2_V_total) + "V\n");
        MeasurementLog.append("  PS3 전류, PSE3 전류, PSE3 전압 : " + String.format("%.3f",PS3_I_total) + "A , " + String.format("%.3f",PSE3_I_total) + "A , " +String.format("%.3f",PSE3_V_total) + "V\n");
        MeasurementLog.append("  PS3 전압, OCP 전압 : "             + String.format("%.3f",PS3_V_total) + "V , " + String.format("%.3f",PS3_OCP) + "V\n");


        SimpleDateFormat timeFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        String time = timeFormat.format(System.currentTimeMillis());

        strLog_format = time + "," + String.format("%.3f", PS1_I_total) + "," + String.format("%.3f", PSE1_I_total) + "," + String.format("%.3f", PSE1_V_total)
                             + "," + String.format("%.3f", PS2_I_total) + "," + String.format("%.3f", PSE2_I_total) + "," + String.format("%.3f", PSE2_V_total)
                             + "," + String.format("%.3f", PS3_I_total) + "," + String.format("%.3f", PSE3_I_total) + "," + String.format("%.3f", PSE3_V_total)
                             + "," + String.format("%.3f", PS3_V_total) + "," + String.format("%.3f", PS3_OCP) + "\r\n";

        CSV_Data.add(strLog_format);
        // 자동 스크롤
        int scrollamount = MeasurementLog.getLayout().getLineTop(MeasurementLog.getLineCount()) - MeasurementLog.getHeight();
        if( scrollamount > MeasurementLog.getHeight()) { MeasurementLog.scrollTo(0, scrollamount); }
    }


    // Fragment 변환 함수
    private void callFragment_Value(final int fragment_no)
    {
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Handler mdelay = new Handler();
        mdelay.postDelayed(new Runnable(){
            public void run(){
                switch (fragment_no){
                    case 1 :
                        transaction.replace(R.id.fragment_container, pse_value_user);
                        transaction.detach(pse_value_user).attach(pse_value_user).commit();
                        break;

                    case 2 :
                        transaction.replace(R.id.fragment_container, pse_value_engineer);
                        transaction.detach(pse_value_engineer).attach(pse_value_engineer).commit();
                        break;
                }
            }
        }, 100); // switch 후 100ms 딜레이 -> NullPointerException 차단

    }

    private void defaultView(){
        sendDataBundle(PS1_I, PS2_I, PS3_I, PSE1_I, PSE2_I, PSE3_I, PSE1_V, PSE2_V, PSE3_V, PS3_V, PS3_OCP);
    }


    public void btnClear(View view)
    {
       glitter_btn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.glitter_btn);
       btn_clear.startAnimation(glitter_btn);

       PS1_I   = 0f; PS2_I   = 0f; PS3_I   = 0f;
       PSE1_I  = 0f; PSE2_I  = 0f; PSE3_I  = 0f;
       PSE1_V  = 0f; PSE2_V  = 0f; PSE3_V  = 0f;
       PS3_V   = 0f; PS3_OCP = 0f; PSE_NC  = 0f;
       MeasurementLog.setText("");
       Snackbar.make(getWindow().getDecorView().getRootView(), "     Clear",Snackbar.LENGTH_SHORT).show();
       sendDataBundle(PS1_I, PS2_I, PS3_I, PSE1_I, PSE2_I, PSE3_I, PSE1_V, PSE2_V, PSE3_V, PS3_V, PS3_OCP);
       callFragment_Value(FRAGMENT_SELECTOR);

    }

    private void sendDataBundle(double PS1_I, double PS2_I, double PS3_I, double PSE1_I, double PSE2_I, double PSE3_I, double PSE1_V, double PSE2_V, double PSE3_V, double PS3_V, double PS3_OCP){
        bundle.putDouble("PS1_I",  PS1_I);   bundle.putDouble("PS2_I",   PS2_I);    bundle.putDouble("PS3_I",  PS3_I);
        bundle.putDouble("PSE1_I", PSE1_I);  bundle.putDouble("PSE2_I",  PSE2_I);   bundle.putDouble("PSE3_I", PSE3_I);
        bundle.putDouble("PSE1_V", PSE1_V);  bundle.putDouble("PSE2_V",  PSE2_V);   bundle.putDouble("PSE3_V", PSE3_V);
        bundle.putDouble("PS3_V",  PS3_V);   bundle.putDouble("PS3_OCP", PS3_OCP);  bundle.putDouble("PSE_NC", PSE_NC);
        pse_value_user.setArguments(bundle);
        pse_value_engineer.setArguments(bundle);
    }


    private void saveCSV()
    {
        SimpleDateFormat timeFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        String time                 = timeFormat.format(System.currentTimeMillis());

        Filename       = time + "_PM.csv";
        csvFile = new File(getExternalFilesDir(null), Filename);
        String row_head = "Time," +
                          "PS1_Volt," + "PS1_Current," + "PS1_PeakCurrent," +
                          "PS2_Volt," + "PS2_Current," + "PS2_PeakCurrent," +
                          "PS3_Volt," + "PS3_Current," + "PS3_PeakCurrent," + "PS3_OCP," + "\r\n";
        MeasurementLog.append("  " + Filename + " 파일 생성 성공\n");
        try
        {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile), "MS949"));
            writer.write(row_head);                 // 1행
            for (int index = 0; index < CSV_Data.size(); index++)
            {
                writer.write(CSV_Data.get(index));
            }
            writer.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            MeasurementLog.append("  log 파일 생성 실패 FIleNotFound\n");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            MeasurementLog.append("  log 파일 생성 실패 Output Error\n");
        }
        strFile_Dir = csvFile.getAbsolutePath();
        Toast.makeText(getApplicationContext(), strFile_Dir + "에 저장되었습니다", Toast.LENGTH_SHORT).show();
        CSV_Data.clear();
    }

    // Google Share 기능
    public void btnShare(View view)
    {
        rotate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
        btn_share.startAnimation(rotate);
        // 버튼 누르면 자동으로 csv 저장
        saveCSV();
        Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".fileprovider",csvFile);
        intent_sharing.setType("application/csv");
        intent_sharing.putExtra(Intent.EXTRA_STREAM, contentUri);

        startActivity(Intent.createChooser(intent_sharing, Filename + " 공유"));
    }


    public class TimerHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {

            switch (msg.what)
            {
                case MESSAGE_TIMER_START:
                    Measure_Count = 1;
                    this.removeMessages(MESSAGE_TIMER_REPEAT);
                    this.sendEmptyMessage(MESSAGE_TIMER_REPEAT);
                    break;
                case MESSAGE_TIMER_REPEAT:
                    btnStart_Calculation();
                    PSE_Result_Log();
                    Measure_Count++;
                    this.sendEmptyMessageDelayed(MESSAGE_TIMER_REPEAT, Measurement_frequency);
                    break;
                case MESSAGE_TIMER_STOP:
                    this.removeMessages(MESSAGE_TIMER_REPEAT);
                    MeasurementLog.append("  " + Measure_Count + " 회 측정 종료\n\n");
                    Measure_Count = 0;
                    break;
            }
        }
    }
    @Override
    protected void onStop()
    {
        super.onStop();
        String strMeasurement_frequency = String.valueOf(Measurement_frequency);
        SharedPreferences Saved_Measurement_frequency = getSharedPreferences(strMeasurement_frequency, MODE_PRIVATE);
        SharedPreferences.Editor editor = Saved_Measurement_frequency.edit();
        editor.putString("measurement_frequency", strMeasurement_frequency);
        editor.commit();
    }

    protected void onDestoy()
    {
        m_ftdi.Ft232_CloseDevice();
        super.onDestroy();
        Toast.makeText(getApplicationContext(), "PM Tool이 종료되었습니다.", Toast.LENGTH_LONG).show();
    }
}
