package com.fourtails.usuariolecturista;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fourtails.usuariolecturista.model.CreditCard;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Main Payment fragment, the CreditCard will be shown and then will call our servers
 */
public class PayFragment extends Fragment {

    @InjectView(R.id.textViewPayCreditCardNumber)
    TextView textViewPayCreditCardNumber;

    @InjectView(R.id.textViewPayCCType)
    TextView textViewPayCCType;

    @InjectView(R.id.textViewPayCreditCardExpirationDate)
    TextView textViewPayCreditCardExpirationDate;

    @InjectView(R.id.textViewPayCreditCardName)
    TextView textViewPayCreditCardName;

    @InjectView(R.id.editTextPayCCV)
    EditText editTextPayCCV;

    @InjectView(R.id.textViewSubTotalPay)
    TextView textViewSubTotalPay;

    @InjectView(R.id.checkBoxPayAgreedTAC)
    CheckBox checkBoxPayAgreedTAC;

    @OnClick(R.id.buttonPayFragmentPay)
    public void payClicked() {
        if (checkBoxPayAgreedTAC.isChecked()) {
            // TODO: add CCV verification
            Double payAmount = BillsFragment.selectedBill;
            MainActivity.bus.post(payAmount);
        } else {
            Toast.makeText(getActivity(), "Tiene que estar de acuerdo con los terminos y condiciones", Toast.LENGTH_SHORT).show();
        }
    }


    public PayFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pay, container, false);
        ButterKnife.inject(this, view);

        CreditCard creditCard = MainActivity.checkForSavedCreditCard();

        textViewSubTotalPay.setText(String.valueOf(BillsFragment.selectedBill));

        textViewPayCCType.setText(creditCard.type);
        textViewPayCreditCardNumber.setText(String.valueOf(creditCard.number));
        textViewPayCreditCardName.setText(creditCard.name);
        textViewPayCreditCardExpirationDate.setText(creditCard.date);


        return view;

    }


    @Override
    public void onResume() {
        super.onResume();
        // Set title
        MainActivity.bus.post(getResources().getString(R.string.toolbarTitlePayFragment));
    }


}
