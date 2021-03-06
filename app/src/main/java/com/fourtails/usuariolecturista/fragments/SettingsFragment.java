package com.fourtails.usuariolecturista.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fourtails.usuariolecturista.MainActivity;
import com.fourtails.usuariolecturista.R;
import com.fourtails.usuariolecturista.ottoEvents.CreateNewBillEvent;

import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {


    @OnClick(R.id.logout_button)
    public void logoutButtonClicked() {
        MainActivity.bus.post(true);
    }

    @OnClick(R.id.createBillButton)
    public void createBillButtonClicked() {
        MainActivity.bus.post(new CreateNewBillEvent(CreateNewBillEvent.Type.STARTED, 1));
    }

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set title
        MainActivity.bus.post(getResources().getString(R.string.toolbarTitleSettingsFragment));
    }


}
