package com.fourtails.usuariolecturista.introFragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fourtails.usuariolecturista.R;

import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class IntroMeterFragment extends Fragment {

    public static IntroMeterFragment newInstance() {
        IntroMeterFragment fragment = new IntroMeterFragment();
        return fragment;
    }


    public IntroMeterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_intro_meter, container, false);
        ButterKnife.bind(this, view);
        return view;
    }


}
