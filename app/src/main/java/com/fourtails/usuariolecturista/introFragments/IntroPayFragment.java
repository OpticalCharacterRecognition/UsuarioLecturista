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
public class IntroPayFragment extends Fragment {

    public static IntroPayFragment newInstance() {
        IntroPayFragment fragment = new IntroPayFragment();
        return fragment;
    }


    public IntroPayFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_intro_pay, container, false);
        ButterKnife.inject(this, view);
        return view;
    }


}
