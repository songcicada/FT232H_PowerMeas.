package com.example.powermonitor;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;

public class Action_Question extends Activity
{
    ImageView quesiton_imageview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.action_question);
        quesiton_imageview = (ImageView)findViewById(R.id.action_question_image);

    }
    //확인 버튼 클릭
    public void mOnClose(View v)
    {

     //액티비티(팝업) 닫기
        finish();
    }
}


