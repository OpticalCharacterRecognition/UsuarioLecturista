package com.fourtails.usuariolecturista.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fourtails.usuariolecturista.MainActivity;
import com.fourtails.usuariolecturista.R;

import butterknife.ButterKnife;


/**
 * Promotions fragment
 * TODO: this should be a list, is now hardcoded
 */
public class PromotionsFragment extends Fragment {


    public PromotionsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_promotions, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set title
        MainActivity.bus.post(getResources().getString(R.string.toolbarTitlePromotionsFragment));
    }

}
