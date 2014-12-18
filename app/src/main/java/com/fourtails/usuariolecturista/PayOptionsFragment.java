package com.fourtails.usuariolecturista;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.fourtails.usuariolecturista.model.CreditCard;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PayOptionsFragment extends Fragment {

    @InjectView(R.id.containerPayOptionsAddCard)
    LinearLayout containerPayOptionsAddCard;

    @InjectView(R.id.containerPayOptionsSavedCard)
    LinearLayout containerPayOptionsSavedCard;

    @InjectView(R.id.buttonSavedCreditCard)
    Button buttonSavedCreditCard;

    @OnClick(R.id.buttonSavedCreditCard)
    public void savedCreditCardClicked() {
        Fragment payFragment = new PayFragment();
        MainActivity.bus.post(payFragment);
    }


    @OnClick(R.id.imageViewAddCreditCard)
    public void addCreditCardClicked() {
        Fragment addCreditCardFragment = new PayAddCreditCardFragment();
        MainActivity.bus.post(addCreditCardFragment);
    }


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

        CreditCard creditCard = MainActivity.checkForSavedCreditCard();
        if (creditCard != null) {
            containerPayOptionsAddCard.setVisibility(View.GONE);
            containerPayOptionsSavedCard.setVisibility(View.VISIBLE);

            buttonSavedCreditCard.setText("Tarjeta \n" + creditCard.number);
        } else {
            containerPayOptionsAddCard.setVisibility(View.VISIBLE);
            containerPayOptionsSavedCard.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set title
        MainActivity.bus.post(getResources().getString(R.string.toolbarTitlePayOptions));
    }

}
