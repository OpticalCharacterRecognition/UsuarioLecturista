package com.fourtails.usuariolecturista;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class PayOptionsFragment extends Fragment {

    @OnClick(R.id.buttonAddCreditCard)
    public void addCreditCardClicked() {
        Fragment addCreditCardFragment = new AddCreditCardFragment();
        MainActivity.bus.post(addCreditCardFragment);
    }

    Button addCreditCardButton;


    public PayOptionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pay_options, container, false);

        ButterKnife.inject(this, view);
        return view;
    }

}