package com.example.powermonitor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ftdi.j2xx.D2xxManager;


public class Main extends AppCompatActivity
{
    D2xxManager.FtDeviceInfoListNode m_DeviceList;
    private I2C m_ftdi;
    private TextView information_devSerialNo;
    private TextView information_devname;
    private TextView information_description;
    private String m_strLog;
    private Button btnfirst_page;
    private Button btnfirst_page_yet;
    private Button btnOpen;
    private boolean openPage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);

        information_devSerialNo = (TextView) findViewById(R.id.device_serialNo);
        information_devname     = (TextView) findViewById(R.id.dev_name);
        information_description = (TextView) findViewById(R.id.dev_description);
        btnfirst_page           = (Button) findViewById(R.id.btn_first_page);
        btnfirst_page_yet       = (Button) findViewById(R.id.btn_first_page_yet);
        btnOpen                 = (Button) findViewById(R.id.btnOpen);
        m_ftdi                  = new I2C(this);


        btnfirst_page.setVisibility(View.INVISIBLE);
        btnfirst_page_yet.setVisibility(View.VISIBLE);

        btnfirst_page.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v) {
//                Toast.makeText(getApplicationContext(), m_DeviceList.serialNumber+" 연결", Toast.LENGTH_LONG).show();
                Intent myintent = new Intent(Main.this, User_First.class);
                FTDevCloseFunction();
                startActivity(myintent);
                finish();
            }
        });
        btnfirst_page_yet.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                Toast.makeText(getApplicationContext(), "Device 미연결", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void ChangeBtn()
    {
        if (openPage == false) { btnfirst_page.setVisibility(View.INVISIBLE); btnfirst_page_yet.setVisibility(View.VISIBLE); }
        else                   { btnfirst_page.setVisibility(View.VISIBLE); btnfirst_page_yet.setVisibility(View.INVISIBLE); }
    }

    public void btnOpenClicked(View view)
    {
        DevOpenFunction();
    }

    public void btnCloseClicked(View view)
    {
        DevCloseFunction();
    }

    public void DevOpenFunction()
    {
        m_DeviceList = m_ftdi.Ft232_OpenDevice();
        if (m_DeviceList != null)
                                    {
                                        m_strLog = m_DeviceList.description + " OPEN PASS";
                                        information_devSerialNo.setText(m_DeviceList.serialNumber);
                                        information_devname.setText(m_strLog);
                                        information_description.setText("Connected");
                                        openPage = true;
                                        m_ftdi.I2C_Init();
                                        ChangeBtn();
                                        btnOpen.setVisibility(View.GONE);
                                    }
        else
                                    {
                                        m_strLog = "OPEN FAIL";
                                        information_devname.setText(m_strLog);
                                        information_devSerialNo.setText("");
                                        information_description.setText("Disconnected");
                                        openPage = false;                         // Debug 용은 true
                                        ChangeBtn();
                                        btnOpen.setVisibility(View.GONE);
                                    }
    }

    public void DevCloseFunction()
    {
        m_ftdi.Ft232_CloseDevice();
        m_strLog = "장비 종료";
        information_devname.setText(m_strLog);
        information_devSerialNo.setText("");
        information_description.setText("Disconnected");
        openPage = false;
        ChangeBtn();
        btnOpen.setVisibility(View.VISIBLE);
    }

    public void FTDevCloseFunction()
    {
        m_ftdi.Ft232_CloseDevice();
    }
}