package com.example.powermonitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ftdi.j2xx.D2xxManager;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class User_First extends AppCompatActivity
{
    private I2C m_ftdi;
    private static final int MESSAGE_TIMER_START  = 100;
    private static final int MESSAGE_TIMER_REPEAT = 101;
    private static final int MESSAGE_TIMER_STOP   = 102;
    D2xxManager.FtDeviceInfoListNode m_DeviceList;
    TimerHandler timerHandler = null;
    private int Measure_Count = 0;

    // PSE 계산식
    double PS1_I            = 0f;
    double PS2_I            = 0f;
    double PS3_I            = 0f;
    double PSE1_I           = 0f;
    double PSE2_I           = 0f;
    double PSE3_I           = 0f;
    double PSE1_V           = 0f;
    double PSE2_V           = 0f;
    double PSE3_V           = 0f;
    double PS3_V            = 0f;
    double PS3_OCP          = 0f;
    double PSE_NC           = 0f;
    // PSE 결과 값
    double PS1_I_total      = 0f;
    double PS2_I_total      = 0f;
    double PS3_I_total      = 0f;
    double PS1_I_Peaktotal  = 0f;
    double PS2_I_Peaktotal  = 0f;
    double PS3_I_Peaktotal  = 0f;
    double PS1_V_total      = 0f;
    double PS2_V_total      = 0f;
    double PS3_V_total      = 0f;
    double[] PS_IV              = new double[12];
    ArrayList<Double> PS1_I_arr = new ArrayList<Double>();
    ArrayList<Double> PS2_I_arr = new ArrayList<Double>();
    ArrayList<Double> PS3_I_arr = new ArrayList<Double>();
    ArrayList<String> CSV_Data  = new ArrayList<String>();
    private TextView PS1_Volt;
    private TextView PS2_Volt;
    private TextView PS3_Volt;
    private TextView PS1_Curr;
    private TextView PS2_Curr;
    private TextView PS3_Curr;
    private TextView PS1_PeakCurr;
    private TextView PS2_PeakCurr;
    private TextView PS3_PeakCurr;
    private TextView PS3_OCP_Volt;

    private ImageView OCP;
    private Button Log_Save         = null;
    private Button Email_send       = null;
    private Button Mode_Change      = null;
    private Button Start_UserMode   = null;
    private Button Stop_UserMode    = null;
    private Button Clear_UserMode   = null;
    private TextView MeasurementLog = null;
    private EditText Email_Address  = null;
    private Boolean DevOpen;

    private String strEmail_Address = "Email_Address";
    private String Filename;
    private String Filename_Email;
    private String strFile_Dir;
    private String strEmail_Dir;
    private String strLog_format;
    int Start_Frequency = 500;      // User Mode에서는 간격 500ms로 고정


    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_first_page);

        timerHandler = new TimerHandler();

        m_ftdi = new I2C(this);
        m_DeviceList = m_ftdi.Ft232_OpenDevice();

        if (m_DeviceList != null) { m_ftdi.I2C_Init(); DevOpen = true; }
        else                      { DevOpen = false; }

        PS1_Volt       = (TextView) findViewById(R.id.PSE1_V);
        PS2_Volt       = (TextView) findViewById(R.id.PSE2_V);
        PS3_Volt       = (TextView) findViewById(R.id.PSE3_V);
        PS1_Curr       = (TextView) findViewById(R.id.PSE1_I);
        PS2_Curr       = (TextView) findViewById(R.id.PSE2_I);
        PS3_Curr       = (TextView) findViewById(R.id.PSE3_I);
        PS1_PeakCurr   = (TextView) findViewById(R.id.PSE1_I_PEAK);
        PS2_PeakCurr   = (TextView) findViewById(R.id.PSE2_I_PEAK);
        PS3_PeakCurr   = (TextView) findViewById(R.id.PSE3_I_PEAK);
        PS3_OCP_Volt   = (TextView) findViewById(R.id.PS3_OCP_Volt);
        MeasurementLog = (TextView) findViewById(R.id.measureLog);
        Email_Address  = (EditText) findViewById(R.id.Email_address);

        OCP            = (ImageView) findViewById(R.id.ocp);
        Log_Save       = (Button) findViewById(R.id.Log_btn);
        Email_send     = (Button) findViewById(R.id.Email_btn);
        Mode_Change    = (Button) findViewById(R.id.EnginnerMode_btn);
        Start_UserMode = (Button) findViewById(R.id.btn_start_UserMode);
        Stop_UserMode  = (Button) findViewById(R.id.btn_stop_UserMode);
        Clear_UserMode = (Button)findViewById(R.id.btn_Clear_UserMode);

        SharedPreferences Saved_Email_Address = getSharedPreferences(strEmail_Address, MODE_PRIVATE);
        String str                            = Saved_Email_Address.getString("Email_Address", "");
        final String transfer_str             = Saved_Email_Address.getString("Email_Address", "");
        Email_Address.setText(str);

        Email_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent email = new Intent(Intent.ACTION_SEND);
                email.setType("plain/text");
                String[] address = {Email_Address.getText().toString()};
                email.putExtra(Intent.EXTRA_EMAIL, address);
                email.putExtra(Intent.EXTRA_SUBJECT, "T-PSE Power Monitor(Android_UserMode)");
                email.putExtra(Intent.EXTRA_TEXT, Filename_Email);
                email.putExtra(Intent.EXTRA_STREAM, Uri.parse(strEmail_Dir));
                startActivity(email);
            }
        });

        Mode_Change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(getApplicationContext(), "Engineer Mode 실행", Toast.LENGTH_SHORT).show();
                Intent myintent = new Intent(User_First.this, Engineer_Second.class);
                myintent.putExtra("Email_Address", transfer_str);
                FTDevCloseFunction();
                startActivity(myintent);
                finish();
            }
        });
        Log_Save.setAlpha((float) 0.5);
        Log_Save.setClickable(false);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        SharedPreferences Saved_Email_Address = getSharedPreferences(strEmail_Address, MODE_PRIVATE);
        SharedPreferences.Editor editor = Saved_Email_Address.edit();
        String str = Email_Address.getText().toString();
        editor.putString("Email_Address", str);
        editor.commit();
    }



    public void btnStart_UserMode(View view)
    {
        timerHandler.sendEmptyMessage(MESSAGE_TIMER_START);
        SimpleDateFormat timeFormat_btn = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        String time = timeFormat_btn.format(System.currentTimeMillis());
        MeasurementLog.append(time);
        MeasurementLog.append(" 사용자 모드 시작");
        MeasurementLog.append("\n");

        if (DevOpen == true)
                                {
                                    Toast.makeText(getApplicationContext(), "FT232h 연결", Toast.LENGTH_SHORT).show();
                                    Start_UserMode.setVisibility(View.INVISIBLE); Stop_UserMode.setVisibility(View.VISIBLE);
                                }
        else
                                {
                                    Toast.makeText(getApplicationContext(), "FT232h 연결 상태 확인이 필요합니다.", Toast.LENGTH_SHORT).show();
                                    Start_UserMode.setVisibility(View.VISIBLE);
                                    Stop_UserMode.setVisibility(View.INVISIBLE);
                                }
        btnStart_UserMode_Function();
        Email_send.setVisibility(View.INVISIBLE);
        Log_Save.setVisibility(View.VISIBLE);
        Log_Save.setAlpha((float) 0.5);
        Log_Save.setClickable(false); // 시작 동안에는 Log 저장 금지, 중지 되어야 Log 한번에 저장
    }

    public void btnStart_UserMode_Function()
    {
        strEmail_Dir = "";  // 이메일에 첨부할 파일 디렉토리 초기화
        strLog_format = "";  // Log 데이터 초기화
        int i = 0;
        for (int array = 0; array < 12; array++) PS_IV[array] = 0;  // 데이터 기록하는 변수값 초기화

        if (DevOpen == true)
        {
            long start = System.currentTimeMillis();
            for (byte tmux = 0; tmux < 4; tmux++)
            {
                m_ftdi.I2C_TMuxSet(tmux);
                m_ftdi.uSleep(500);
                for (byte amux = 1; amux < 4; amux++)
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
            PSE_Calculate();
            PSE_Value_View();
            OCP_LED();
            long end = System.currentTimeMillis();
            MeasurementLog.append(String.format("Elapsed Time : %d mS", (end - start)) + "\n\n");
        }
        else
        {
        PSE_Calculate();
        PSE_Value_View();
        OCP_LED();
        MeasurementLog.append("FT232h가 연결되어 있지 않습니다.\n");
        timerHandler.sendEmptyMessage(MESSAGE_TIMER_STOP);
        }
    }

    public void btnStop_UserMode(View view)
    {
        timerHandler.sendEmptyMessage(MESSAGE_TIMER_STOP);
        SimpleDateFormat timeFormat_btn = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        String time = timeFormat_btn.format(System.currentTimeMillis());
        MeasurementLog.append(time);
        MeasurementLog.append(" 중지\n");
        MeasurementLog.append("총 " + Measure_Count + "회 측정\n");
        PSE_Value_View();
        Toast.makeText(getApplicationContext(), "측정이 중지되었습니다.", Toast.LENGTH_SHORT).show();
        Start_UserMode.setVisibility(View.VISIBLE);
        Stop_UserMode.setVisibility(View.INVISIBLE);
        Log_Save.setAlpha((float) 1.0);
        Log_Save.setClickable(true);
    }

    private void PSE_Calculate()
    {
        PS1_V_total = PSE1_V * 6;
        PS2_V_total = PSE2_V * 6;
        PS3_V_total = PSE3_V * 6;
        PS1_I_total = (PSE1_I - PS1_I) * 100;
        PS2_I_total = (PSE2_I - PS2_I) * 100;
        if (PSE3_I == 0) { PS3_I_total = 0; } else { PS3_I_total = (PSE3_I - 0.606) * 100; }
        PS1_I_arr.add(PS1_I_total);
        PS2_I_arr.add(PS2_I_total);
        PS3_I_arr.add(PS3_I_total);

    }

    private void PSE_Value_View()
    {
        // 내림차순
        Comparator<Double> compare = new Comparator<Double>() {
            @Override
            public int compare(Double o1, Double o2) {
                return o2.compareTo(o1);
            }
        };
        Collections.sort(PS1_I_arr, compare);
        Collections.sort(PS2_I_arr, compare);
        Collections.sort(PS3_I_arr, compare);
        PS1_I_Peaktotal = PS1_I_arr.get(0);
        PS2_I_Peaktotal = PS2_I_arr.get(0);
        PS3_I_Peaktotal = PS3_I_arr.get(0);

        PS1_Volt.setText(String.format("%.3fV", PS1_V_total));
        PS2_Volt.setText(String.format("%.3fV", PS2_V_total));
        PS3_Volt.setText(String.format("%.3fV", PS3_V_total));
        PS1_Curr.setText(String.format("%.3fA", PS1_I_total));
        PS2_Curr.setText(String.format("%.3fA", PS2_I_total));
        PS3_Curr.setText(String.format("%.3fA", PS3_I_total));
        PS1_PeakCurr.setText(String.format("%.3fA", PS1_I_Peaktotal));
        PS2_PeakCurr.setText(String.format("%.3fA", PS2_I_Peaktotal));
        PS3_PeakCurr.setText(String.format("%.3fA", PS3_I_Peaktotal));
        PS3_OCP_Volt.setText(String.format("%.3fV", PS3_OCP));
    }

    private void PSE_Result_Log()
    {
        // View log
        MeasurementLog.append(String.format("PS1 전압, PS1 전류, PS1 Peak전류") + " : " + PS1_Volt.getText() + " , " + PS1_Curr.getText() + " , " + PS1_PeakCurr.getText() + "\n");
        MeasurementLog.append(String.format("PS2 전압, PS2 전류, PS2 Peak전류") + " : " + PS2_Volt.getText() + " , " + PS2_Curr.getText() + " , " + PS2_PeakCurr.getText() + "\n");
        MeasurementLog.append(String.format("PS3 전압, PS3 전류, PS3 Peak전류") + " : " + PS3_Volt.getText() + " , " + PS3_Curr.getText() + " , " + PS3_PeakCurr.getText() + "\n");
        // .CSV log
        SimpleDateFormat timeFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        String time = timeFormat.format(System.currentTimeMillis());

        strLog_format = time + "," + String.format("%.3f", PS1_V_total) + "," + String.format("%.3f", PS1_I_total) + "," + String.format("%.3f", PS1_I_Peaktotal)
                             + "," + String.format("%.3f", PS2_V_total) + "," + String.format("%.3f", PS2_I_total) + "," + String.format("%.3f", PS2_I_Peaktotal)
                             + "," + String.format("%.3f", PS3_V_total) + "," + String.format("%.3f", PS3_I_total) + "," + String.format("%.3f", PS3_I_Peaktotal)
                             + "," + String.format("%.3f", PS3_OCP) + "\r\n";

        CSV_Data.add(strLog_format);
    }

    private void OCP_LED()
    {
        if      (PS3_OCP <= 1.0f && PS3_OCP > 0) { OCP.setImageResource(R.drawable.ocp_fail); }
        else if (PS3_OCP > 1.0) { OCP.setImageResource(R.drawable.ocp_pass); }
        else                    { OCP.setImageResource(R.drawable.ocp_no); }
    }

    protected void onDestoy()
    {
        FTDevCloseFunction();
        super.onDestroy();
        Toast.makeText(getApplicationContext(), "PM Tool이 종료되었습니다.", Toast.LENGTH_LONG).show();
    }

    public void FTDevCloseFunction()
    {
        m_ftdi.Ft232_CloseDevice();
    }

    public void btnLog_Save_User(View view)
    {
        saveCSV();
        Log_Save.setVisibility(View.INVISIBLE);
        Email_send.setVisibility(View.VISIBLE);
    }

    private void saveCSV()
    {
        SimpleDateFormat timeFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        String time                 = timeFormat.format(System.currentTimeMillis());

        Filename       = time + "_PM_User.csv";
        Filename_Email = Filename;

        File csvFile = new File(getExternalFilesDir(null), Filename);
        String row_head = "Time," +
                          "PS1_Volt," + "PS1_Curr," + "PS1_PeakCurr," +
                          "PS2_Volt," + "PS2_Curr," + "PS2_PeakCurr," +
                          "PS3_Volt," + "PS3_Curr," + "PS3_PeakCurr," + "PS3_OCP," + "\r\n";
        MeasurementLog.append(Filename + " 파일 생성 성공\n");
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
                MeasurementLog.append(".csv 파일 생성 실패 FIleNotFound\n");
            }
        catch (IOException e)
            {
                e.printStackTrace();
                MeasurementLog.append(".csv 파일 생성 실패 Output Error\n");
            }
        strFile_Dir = csvFile.getAbsolutePath();
        Toast.makeText(getApplicationContext(), strFile_Dir + "에 저장되었습니다", Toast.LENGTH_SHORT).show();
        strEmail_Dir = strFile_Dir;
        CSV_Data.clear();
    }


    public class TimerHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MESSAGE_TIMER_START:
                    MeasurementLog.append("측정 시작\n");
                    this.removeMessages(MESSAGE_TIMER_REPEAT);
                    this.sendEmptyMessage(MESSAGE_TIMER_REPEAT);
                    break;
                case MESSAGE_TIMER_REPEAT:
                    btnStart_UserMode_Function();
                    PSE_Result_Log();
                    Measure_Count++;
                    this.sendEmptyMessageDelayed(MESSAGE_TIMER_REPEAT, 2000); // 측정 딜레이 2초로 고정
                    break;
                case MESSAGE_TIMER_STOP:
                    this.removeMessages(MESSAGE_TIMER_REPEAT);
                    MeasurementLog.append("측정 종료\n");
                    break;
            }
        }
    }
    public void btnClear(View view)
    {
        MeasurementLog.setText("");
        PS1_Volt.setText("");
        PS2_Volt.setText("");
        PS3_Volt.setText("");
        PS1_Curr.setText("");
        PS2_Curr.setText("");
        PS3_Curr.setText("");
        PS1_PeakCurr.setText("");
        PS2_PeakCurr.setText("");
        PS3_PeakCurr.setText("");
        PS3_OCP_Volt.setText("");
        Email_Address.setText("");
        Snackbar.make(getWindow().getDecorView().getRootView(), "Clear",Snackbar.LENGTH_SHORT).show();
    }
}
