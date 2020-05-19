package com.example.powermonitor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

public class Action_Search extends Activity
{
    EditText measurement_frequency;
    int frequency;
    String data;
    Intent intent = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.action_search);

        //UI 객체생성
        measurement_frequency = (EditText) findViewById(R.id.Measurement_frequency);

        //데이터 가져오기
        intent = getIntent();
        frequency = intent.getIntExtra("measurement_frequency",2000);
        data = String.valueOf(frequency);
        measurement_frequency.setText(data);
    }

    //확인 버튼 클릭
    public void mOnClose(View v)
    {
        //데이터 전달하기
        try
        {
        frequency = Integer.parseInt(""+measurement_frequency.getText());
        intent.putExtra("result_frequency", frequency);
        setResult(RESULT_OK, intent);
        } catch(NumberFormatException e)
        {
        frequency = 2000;
        intent.putExtra("result_frequency", frequency);
        setResult(RESULT_OK, intent);
        } catch(Exception e)
        {
        finish();
        }

        //액티비티(팝업) 닫기
        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        //바깥 레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE)
        {
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed()
    {

        //안드로이드 백버튼 막기
        return;
    }
}


