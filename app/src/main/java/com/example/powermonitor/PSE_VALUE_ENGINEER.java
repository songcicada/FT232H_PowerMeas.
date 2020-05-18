package com.example.powermonitor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;


public class PSE_VALUE_ENGINEER extends Fragment {
    public PSE_VALUE_ENGINEER(){}
    //XML
    private TextView  vPS1_I , vPS2_I  , vPS3_I;
    private TextView  vPSE1_I, vPSE2_I , vPSE3_I;
    private TextView  vPSE1_V, vPSE2_V , vPSE3_V;
    private TextView  vPS3_V , vPS3_OCP;
    private ImageView vOCP   ;
    // Law Value
    private double PS1_I,  PS2_I,   PS3_I = 0;
    private double PSE1_I, PSE2_I,  PSE3_I= 0;
    private double PSE1_V, PSE2_V,  PSE3_V= 0;
    private double PS3_V,  PS3_OCP, PSE_NC= 0;
    // View Value
    private double PS1_I_total , PS2_I_total , PS3_I_total  ;
    private double PSE1_I_total, PSE2_I_total, PSE3_I_total ;
    private double PSE1_V_total, PSE2_V_total, PSE3_V_total ;
    private double PS3_V_total , OCP_total   ;

    private Animation glitter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        PS1_I   = getArguments().getDouble("PS1_I");
        PS2_I   = getArguments().getDouble("PS2_I");
        PS3_I   = getArguments().getDouble("PS3_I");
        PSE1_I  = getArguments().getDouble("PSE1_I");
        PSE2_I  = getArguments().getDouble("PSE2_I");
        PSE3_I  = getArguments().getDouble("PSE3_I");
        PSE1_V  = getArguments().getDouble("PSE1_V");
        PSE2_V  = getArguments().getDouble("PSE2_V");
        PSE3_V  = getArguments().getDouble("PSE3_V");
        PS3_V   = getArguments().getDouble("PS3_V");
        PS3_OCP = getArguments().getDouble("PS3_OCP");

        return inflater.inflate(R.layout.pse_value_engineer, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        SetView();
        try {
            PSE_Calculate();
            PSE_Value_View();
        }catch(NullPointerException e){
            NullpointerException();
        }

    }


    private void SetView()
    {
        vPS1_I   = (TextView)getActivity().findViewById(R.id.PS1_I);
        vPS2_I   = (TextView)getActivity().findViewById(R.id.PS2_I);
        vPS3_I   = (TextView)getActivity().findViewById(R.id.PS3_I);
        vPSE1_I  = (TextView)getActivity().findViewById(R.id.PSE1_I);
        vPSE2_I  = (TextView)getActivity().findViewById(R.id.PSE2_I);
        vPSE3_I  = (TextView)getActivity().findViewById(R.id.PSE3_I);
        vPSE1_V  = (TextView)getActivity().findViewById(R.id.PSE1_V);
        vPSE2_V  = (TextView)getActivity().findViewById(R.id.PSE2_V);
        vPSE3_V  = (TextView)getActivity().findViewById(R.id.PSE3_V);
        vPS3_V   = (TextView)getActivity().findViewById(R.id.PS3_V);
        vPS3_OCP = (TextView)getActivity().findViewById(R.id.PS3_OCP);
        vOCP     = (ImageView)getActivity().findViewById(R.id.ocp);
    }

    private void PSE_Calculate()
    {
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
        OCP_total = PS3_OCP;
    }



    private void PSE_Value_View()
    {
        vPS1_I  .setText(String.format(" %.3fA", PS1_I_total));
        vPS2_I  .setText(String.format(" %.3fA", PS2_I_total));
        vPS3_I  .setText(String.format(" %.3fA", PS3_I_total));
        vPSE1_I .setText(String.format(" %.3fA", PSE1_I_total));
        vPSE2_I .setText(String.format(" %.3fA", PSE2_I_total));
        vPSE3_I .setText(String.format(" %.3fA", PSE3_I_total));
        vPSE1_V .setText(String.format(" %.3fV", PSE1_V_total));
        vPSE2_V .setText(String.format(" %.3fV", PSE2_V_total));
        vPSE3_V .setText(String.format(" %.3fV", PSE3_V_total));
        vPS3_V  .setText(String.format(" %.3fV", PS3_V_total));
        vPS3_OCP.setText(String.format(" %.3fV", OCP_total));
        OCP();
    }

    private void OCP()
    {
        glitter = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.glitter);
        vOCP.startAnimation(glitter);
        if      (PS3_OCP <= 1.0f && PS3_OCP > 0) { vOCP.setImageResource(R.drawable.ocp_fail); }
        else if (PS3_OCP > 1.0)                  { vOCP.setImageResource(R.drawable.ocp_pass); }
        else                                     { vOCP.setImageResource(R.drawable.ocp_no);   }
    }

    public void PSE_VALUE_ENGINEER_CLEAR()
    {
        vPS1_I  .setText(String.format(" %.3fA", 0));
        vPS2_I  .setText(String.format(" %.3fA", 0));
        vPS3_I  .setText(String.format(" %.3fA", 0));
        vPSE1_I .setText(String.format(" %.3fA", 0));
        vPSE2_I .setText(String.format(" %.3fA", 0));
        vPSE3_I .setText(String.format(" %.3fA", 0));
        vPSE1_V .setText(String.format(" %.3fV", 0));
        vPSE2_V .setText(String.format(" %.3fV", 0));
        vPSE3_V .setText(String.format(" %.3fV", 0));
        vPS3_V  .setText(String.format(" %.3fV", 0));
        vPS3_OCP.setText(String.format(" %.3fV", 0));
        PS3_OCP = 0f;
        OCP();
    }

    private void NullpointerException()
    {
        vPS1_I  .setText("-");
        vPS2_I  .setText("-");
        vPS3_I  .setText("-");
        vPSE1_I .setText("-");
        vPSE2_I .setText("-");
        vPSE3_I .setText("-");
        vPSE1_V .setText("-");
        vPSE2_V .setText("-");
        vPSE3_V .setText("-");
        vPS3_V  .setText("-");
        vPS3_OCP.setText("-");
        OCP();
    }
}