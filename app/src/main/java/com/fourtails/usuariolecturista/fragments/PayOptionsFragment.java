package com.fourtails.usuariolecturista.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fourtails.usuariolecturista.MainActivity;
import com.fourtails.usuariolecturista.R;
import com.fourtails.usuariolecturista.ottoEvents.BillPaymentAttemptEvent;
import com.fourtails.usuariolecturista.utilities.DatePickerFragmentCreditCard;
import com.stripe.android.model.Card;

import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PayOptionsFragment extends Fragment {

    /**
     * Dialog Views *
     */

    TextView textViewCreditCardExpirationDate;
    Spinner spinnerCreditCardSelector;
    EditText editTextCreditCardNumber;
    EditText editTextCreditCardName;
    EditText editTextCCV;
    TextView textViewSubTotalPay;
    CheckBox checkBoxAgreedTAC;
    Button payButton;

    int selectedMonth;
    int selectedYear;

    private AlertDialog payDialog = null;

    /**
     * **************************
     */

    @InjectView(R.id.buttonCreditCard)
    Button buttonAddCreditCard;

    @InjectView(R.id.textViewTotalBillToPay)
    TextView textViewTotalBillToPay;


    @OnClick(R.id.buttonCreditCard)
    public void creditCardClicked() {
        inflateCreditCardDialog();
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

        textViewTotalBillToPay.setText(String.valueOf(BillsFragment.selectedBill));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set title
        MainActivity.bus.post(getResources().getString(R.string.toolbarTitlePayOptions));
    }


    /**
     * Inflates the credit card alert dialog, can't be injected
     */
    private void inflateCreditCardDialog() {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_add_credit_card, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        payDialog = builder.show();

        textViewCreditCardExpirationDate = (TextView) view.findViewById(R.id.editTextCreditCardExpirationDate);
//        spinnerCreditCardSelector = (Spinner) view.findViewById(R.id.spinnerCreditCardSelector);
        editTextCreditCardNumber = (EditText) view.findViewById(R.id.editTextCreditCardNumber);
        editTextCreditCardName = (EditText) view.findViewById(R.id.editTextCreditCardName);
        editTextCCV = (EditText) view.findViewById(R.id.editTextCCV);
        checkBoxAgreedTAC = (CheckBox) view.findViewById(R.id.checkBoxAgreedTAC);
        textViewSubTotalPay = (TextView) view.findViewById(R.id.textViewSubTotalPay);
        payButton = (Button) view.findViewById(R.id.buttonPayWithCC);
        textViewSubTotalPay.setText(String.valueOf(BillsFragment.selectedBill));

        textViewCreditCardExpirationDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payButtonClicked();
            }
        });
    }

    /**
     * Checks if TAC is accepted and the credit card is valid, if so, then goes to main Activity
     * to initiate the payment process
     */
    private void payButtonClicked() {
        if (checkBoxAgreedTAC.isChecked()) {
            boolean validCard = validateCard();
            if (validCard) {
                Double payAmount = BillsFragment.selectedBill;
                MainActivity.bus.post(new BillPaymentAttemptEvent(payAmount));
                if (PayOptionsFragment.this.payDialog != null) {
                    PayOptionsFragment.this.payDialog.dismiss();
                }
            }
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.TACMustAgreedMessage), Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Square Credit card validation
     *
     * @return
     */
    private boolean validateCard() {
        Card card = new Card(
                editTextCreditCardNumber.getText().toString(),
                selectedMonth,
                selectedYear,
                editTextCCV.getText().toString()
        );
        boolean validation = card.validateCard();
        if (validation) {
            return true;
        } else if (!card.validateNumber()) {
            Toast.makeText(getActivity(), getResources().getString(R.string.creditCardErrorNumber), Toast.LENGTH_SHORT).show();
            return false;
        } else if (!card.validateExpiryDate()) {
            Toast.makeText(getActivity(), getResources().getString(R.string.creditCardErrorExpiryDate), Toast.LENGTH_SHORT).show();
            return false;
        } else if (!card.validateCVC()) {
            Toast.makeText(getActivity(), getResources().getString(R.string.creditCardErrorCCV), Toast.LENGTH_SHORT).show();
            return false;
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.creditCardError), Toast.LENGTH_SHORT).show();
            return false;
        }
    }


    /**
     * This will call for an special date picker without the "day"
     */
    private void showDatePicker() {
        DatePickerFragmentCreditCard date = new DatePickerFragmentCreditCard();
        /**
         * Set Up Current Date Into dialog
         */
        Calendar calendar = Calendar.getInstance();
        Bundle args = new Bundle();
        args.putInt("year", calendar.get(Calendar.YEAR));
        args.putInt("month", calendar.get(Calendar.MONTH));
        args.putInt("day", calendar.get(Calendar.DAY_OF_MONTH));
        args.putLong("currentDateInMillis", calendar.getTimeInMillis());
        date.setArguments(args);
        /**
         * Set Call back to capture selected date
         */
        date.setCallBack(ondate);
        date.show(getFragmentManager(), "Date Picker");

    }


    /**
     * Dialog Listener
     */
    DatePickerDialog.OnDateSetListener ondate = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {

            selectedMonth = monthOfYear + 1;
            selectedYear = year;

            textViewCreditCardExpirationDate.setText(String.valueOf(monthOfYear + 1)
                    + "-" + String.valueOf(year));

        }
    };

}
