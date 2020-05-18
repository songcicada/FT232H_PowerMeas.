package com.example.powermonitor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class PSE_VALUE_USER extends Fragment {
    public PSE_VALUE_USER(){}
    //XML
    private TextView  vPSE1_V,       vPSE2_V,       vPSE3_V      ;
    private TextView  vTotal_PSE1_I, vTotal_PSE2_I, vTotal_PSE3_I;
    private TextView  vPSE1_I_PEAK,  vPSE2_I_PEAK,  vPSE3_I_PEAK ;
    private TextView  vOCP_Warning;
    private ImageView vOCP;
    // Law Value
    private double PS1_I,  PS2_I,   PS3_I;
    private double PSE1_I, PSE2_I,  PSE3_I;
    private double PSE1_V, PSE2_V,  PSE3_V;
    private double PS3_V,  PS3_OCP, PSE_NC;
    // View Value
    private double PS1_I_total,     PS2_I_total,     PS3_I_total    ;
    private double PS1_I_Peaktotal, PS2_I_Peaktotal, PS3_I_Peaktotal;
    private double PS1_V_total,     PS2_V_total,     PS3_V_total    ;
    // Peak Array Value
    private ArrayList<Double> PS1_I_arr = new ArrayList<Double>();
    private ArrayList<Double> PS2_I_arr = new ArrayList<Double>();
    private ArrayList<Double> PS3_I_arr = new ArrayList<Double>();

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
        return inflater.inflate(R.layout.pse_value_user, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        SetView();
        try {
            PSE_Calculate();
            PSE_Value_View();
        }catch(NullPointerException e)
        {
            NullpointerException();
        }
    }

    private void SetView()
    {
        vPSE1_V       = (TextView)getActivity().findViewById(R.id.PSE1_V);
        vPSE2_V       = (TextView)getActivity().findViewById(R.id.PSE2_V);
        vPSE3_V       = (TextView)getActivity().findViewById(R.id.PSE3_V);
        vTotal_PSE1_I = (TextView)getActivity().findViewById(R.id.Total_PSE1_I);
        vTotal_PSE2_I = (TextView)getActivity().findViewById(R.id.Total_PSE2_I);
        vTotal_PSE3_I = (TextView)getActivity().findViewById(R.id.Total_PSE3_I);
        vPSE1_I_PEAK  = (TextView)getActivity().findViewById(R.id.PSE1_I_PEAK);
        vPSE2_I_PEAK  = (TextView)getActivity().findViewById(R.id.PSE2_I_PEAK);
        vPSE3_I_PEAK  = (TextView)getActivity().findViewById(R.id.PSE3_I_PEAK);
        vOCP_Warning  = (TextView)getActivity().findViewById((R.id.OCP_Warning));
        vOCP          = (ImageView)getActivity().findViewById(R.id.ocp);
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
        Comparator<Double> compare = new Comparator<Double>()
        {
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

        vPSE1_V       .setText(String.format(" %.3fV", PS1_V_total));
        vPSE2_V       .setText(String.format(" %.3fV", PS2_V_total));
        vPSE3_V       .setText(String.format(" %.3fV", PS3_V_total));
        vTotal_PSE1_I .setText(String.format(" %.3fA", PS1_I_total));
        vTotal_PSE2_I .setText(String.format(" %.3fA", PS2_I_total));
        vTotal_PSE3_I .setText(String.format(" %.3fA", PS3_I_total));
        vPSE1_I_PEAK  .setText(String.format(" %.3fA", PS1_I_Peaktotal));
        vPSE2_I_PEAK  .setText(String.format(" %.3fA", PS2_I_Peaktotal));
        vPSE3_I_PEAK  .setText(String.format(" %.3fA", PS3_I_Peaktotal));
        OCP();
    }

    private void OCP()
    {
        glitter = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.glitter);
        vOCP.startAnimation(glitter);
        if      (PS3_OCP <= 1.0f && PS3_OCP > 0) { vOCP.setImageResource(R.drawable.ocp_fail);
                                                   vOCP_Warning.setText("PS3 OCP");
                                                   vOCP_Warning.startAnimation(glitter);       }
        else if (PS3_OCP > 1.0)                  { vOCP.setImageResource(R.drawable.ocp_pass); }
        else                                     { vOCP.setImageResource(R.drawable.ocp_no);   }
    }

    public void PSE_VALUE_USER_CLEAR()
    {
        vPSE1_V       .setText(String.format(" %.3fV", 0));
        vPSE2_V       .setText(String.format(" %.3fV", 0));
        vPSE3_V       .setText(String.format(" %.3fV", 0));
        vTotal_PSE1_I .setText(String.format(" %.3fA", 0));
        vTotal_PSE2_I .setText(String.format(" %.3fA", 0));
        vTotal_PSE3_I .setText(String.format(" %.3fA", 0));
        vPSE1_I_PEAK  .setText(String.format(" %.3fA", 0));
        vPSE2_I_PEAK  .setText(String.format(" %.3fA", 0));
        vPSE3_I_PEAK  .setText(String.format(" %.3fA", 0));
        PS3_OCP = 0f;
        OCP();
    }

    private void NullpointerException()
    {
        vPSE1_V       .setText("-");
        vPSE2_V       .setText("-");
        vPSE3_V       .setText("-");
        vTotal_PSE1_I .setText("-");
        vTotal_PSE2_I .setText("-");
        vTotal_PSE3_I .setText("-");
        vPSE1_I_PEAK  .setText("-");
        vPSE2_I_PEAK  .setText("-");
        vPSE3_I_PEAK  .setText("-");
        OCP();
    }
}