package com.example.powermonitor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ftdi.j2xx.D2xxManager;


public class Main extends AppCompatActivity
{
    D2xxManager.FtDeviceInfoListNode m_DeviceList;
    private I2C       m_ftdi;
    private TextView  information_devSerialNo;
    private TextView  information_devname;
    private Button    btnfirst_page;
    private ImageView Logo;
    private ImageView btn_ft232h_connected;
    private ImageView btn_ft232h_disconnected;
    private Animation mbtn_ft232h_disconnected;
    private Animation mbtnfirst_page;
    private boolean   openPage = false;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);

        information_devSerialNo = (TextView)  findViewById(R.id.dev_serialNo);
        information_devname     = (TextView)  findViewById(R.id.dev_name);
        Logo                    = (ImageView) findViewById(R.id.ablee_logo);
        btnfirst_page           = (Button)    findViewById(R.id.btn_first_page);
        btn_ft232h_disconnected = (ImageView) findViewById(R.id.ft232h_disconnected);
        btn_ft232h_connected    = (ImageView) findViewById(R.id.ft232h_connected);
        m_ftdi                  = new I2C(this);

        btnfirst_page.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v) {
              //  Toast.makeText(getApplicationContext(), "T-PSE PM Module Activate", Toast.LENGTH_SHORT).show();
                Intent myintent = new Intent(Main.this, PSE_PAGE.class);
                m_ftdi.Ft232_CloseDevice();
                startActivity(myintent);
                finish();
            }
        });
    }
    public static final void updateStatusBarColor (Activity context, String color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = context.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(color));
        }
    }

    public void btn_disconnected(View view) { DevOpenFunction(); }

    public void btn_connected(View view)    { DevCloseFunction();
                                              updateStatusBarColor(Main.this,"#737375");
                                              mbtnfirst_page = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.disconnection);
                                              btnfirst_page.startAnimation(mbtnfirst_page);}

    @SuppressLint("MissingPermission")
    public void DevOpenFunction()
    {
        m_DeviceList = m_ftdi.Ft232_OpenDevice();
        if (m_DeviceList != null)
                                    {
                                        openPage = true;
                                        m_ftdi.I2C_Init();
                                        ChangeBtn();
                                        Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                                        vib.vibrate(500);
                                        updateStatusBarColor(Main.this,"#012d86");
                                    }
        else
                                    {
                                        openPage = false;                         // Debug 용은 true
                                        ChangeBtn();
                                        Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                                        vib.vibrate(500);
                                        mbtn_ft232h_disconnected = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.vibrate);
                                        btn_ft232h_disconnected.startAnimation(mbtn_ft232h_disconnected);
                                        updateStatusBarColor(Main.this,"#737375");
                                    }
    }

    public void DevCloseFunction()
    {
        m_ftdi.Ft232_CloseDevice();
        openPage = false;
        ChangeBtn();
    }

    public void ChangeBtn()
    {
        if      (openPage == false) { when_disconnected();}
        else if (openPage == true)  { when_connedcted();}
    }

    public void when_disconnected()
    {
        btn_ft232h_disconnected.setVisibility(View.VISIBLE);
        btn_ft232h_connected.setVisibility(View.GONE);
        btnfirst_page.setVisibility(View.INVISIBLE);
        information_devSerialNo.setText("");
        information_devname.setText("");
        Logo.setImageResource(R.drawable.ablee_logo_not);

    }
    public void when_connedcted()
    {
        btn_ft232h_disconnected.setVisibility(View.GONE);
        btn_ft232h_connected.setVisibility(View.VISIBLE);
        btnfirst_page.setVisibility(View.VISIBLE);
        Logo.setImageResource(R.drawable.ablee_logo);
        if(openPage == true) { mbtnfirst_page = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.connection); btnfirst_page.startAnimation(mbtnfirst_page); }
        information_devSerialNo.setText(m_DeviceList.serialNumber);              // Debug 시 주석
        information_devname.setText(m_DeviceList.description);                   // Debug 시 주석
    }


}