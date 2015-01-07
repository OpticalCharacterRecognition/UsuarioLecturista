package com.fourtails.usuariolecturista.introFragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fourtails.usuariolecturista.DispatchActivity;
import com.fourtails.usuariolecturista.IntroActivity;
import com.fourtails.usuariolecturista.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class IntroLastFragment extends Fragment {

    @OnClick(R.id.buttonIntroFB)
    public void facebookIntroButtonClicked() {
        Intent intent = new Intent(getActivity(), DispatchActivity.class);
        IntroActivity.introBus.post(intent);
    }


    public static IntroLastFragment newInstance() {
        IntroLastFragment fragment = new IntroLastFragment();
        return fragment;
    }


    public IntroLastFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_intro_last, container, false);
        ButterKnife.inject(this, view);
        return view;
    }


}
