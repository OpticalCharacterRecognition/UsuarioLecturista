package com.fourtails.usuariolecturista;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.widget.TextView;
import android.widget.Toast;

import com.fourtails.usuariolecturista.utilities.DatePickerFragmentCreditCard;
import com.melnykov.fab.FloatingActionButton;
import com.stripe.android.model.Card;

import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


/**
 * Prepaid fragment
 */
public class PrepaidFragment extends Fragment {

    public final String TAG = "PrepaidFragment";

    // TODO: get this from the backend
    public static double litersToPesoConvertionRatio = 1.0;

    /**
     * Dialog views *
     */
    TextView textViewCreditCardExpirationDate;
    EditText editTextCreditCardNumber;
    EditText editTextCreditCardName;
    EditText editTextCCV;
    TextView textViewSubTotalPay;
    CheckBox checkBoxAgreedTAC;
    Button payButton;

    private AlertDialog payDialog = null;

    int selectedMonth;
    int selectedYear;

    /**
     * ****************
     */


    @InjectView(R.id.editTextPrepaidInput)
    public EditText litersToBuy;

    @OnClick(R.id.keyboardButton_0)
    public void keyboard0Clicked() {
        keyboardClicked(0);
    }

    @OnClick(R.id.keyboardButton_1)
    public void keyboard1Clicked() {
        keyboardClicked(1);
    }

    @OnClick(R.id.keyboardButton_2)
    public void keyboard2Clicked() {
        keyboardClicked(2);
    }

    @OnClick(R.id.keyboardButton_3)
    public void keyboard3Clicked() {
        keyboardClicked(3);
    }

    @OnClick(R.id.keyboardButton_4)
    public void keyboard4Clicked() {
        keyboardClicked(4);
    }

    @OnClick(R.id.keyboardButton_5)
    public void keyboard5Clicked() {
        keyboardClicked(5);
    }

    @OnClick(R.id.keyboardButton_6)
    public void keyboard6Clicked() {
        keyboardClicked(6);
    }

    @OnClick(R.id.keyboardButton_7)
    public void keyboard7Clicked() {
        keyboardClicked(7);
    }

    @OnClick(R.id.keyboardButton_8)
    public void keyboard8Clicked() {
        keyboardClicked(8);
    }

    @OnClick(R.id.keyboardButton_9)
    public void keyboard9Clicked() {
        keyboardClicked(9);
    }

    @OnClick(R.id.keyboardButton_backspace)
    public void keyboardBackspaceClicked() {
        // delete one character
        String passwordStr = litersToBuy.getText().toString();
        if (passwordStr.length() > 0) {
            String newPasswordStr = new StringBuilder(passwordStr)
                    .deleteCharAt(passwordStr.length() - 1).toString();
            litersToBuy.setText(newPasswordStr);
            if (newPasswordStr.length() == 0) {
                hideFab();
                hidePayButton();
            }
        } else {
            hideFab();
            hidePayButton();
        }
    }

    @OnClick(R.id.keyboardButton_clear)
    public void keyboardClearClicked() {
        // clear password field
        hideFab();
        hidePayButton();
        litersToBuy.setText(null);
    }

    public void keyboardClicked(int key) {
        litersToBuy.append(String.valueOf(key));
        showFab();
    }

    @InjectView(R.id.textViewPrepaidPrice)
    TextView textViewPrepaidPrice;

    @InjectView(R.id.fabCalculate)
    FloatingActionButton fabCalculate;

    @OnClick(R.id.fabCalculate)
    public void calculateClicked() {
        if (litersToBuy.getText().length() > 0) {
            int liters = Integer.parseInt(litersToBuy.getText().toString());
            if (liters > 0) {
                showPayButton();
                double peso = liters * litersToPesoConvertionRatio;
                textViewPrepaidPrice.setText(String.valueOf(peso));
            }
        }
    }

    @OnClick(R.id.buttonPrePay)
    public void creditCardClicked() {
        inflateCreditCardDialog();
    }

    @InjectView(R.id.buttonPrePay)
    Button buttonPrePay;


    public PrepaidFragment() {
        // Required empty public constructor
    }

    public boolean isFabButtonShowing = false;
    public boolean isPayButtonShowing = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_prepaid, container, false);
        ButterKnife.inject(this, view);

        fabCalculate.hide();
        fabCalculate.setVisibility(View.INVISIBLE);
        buttonPrePay.setVisibility(View.INVISIBLE);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set title
        MainActivity.bus.post(getResources().getString(R.string.toolbarTitlePrepaidOptions));
    }

    /**
     * Show the calculate button
     */
    private void showFab() {
        if (!isFabButtonShowing) {
            isFabButtonShowing = true;
            // Set the content view to 0% opacity but visible, so that it is visible
            // (but fully transparent) during the animation.
            fabCalculate.setAlpha(0f);
            fabCalculate.setVisibility(View.VISIBLE);

            // Animate the content view to 100% opacity, and clear any animation
            // listener set on the view.
            fabCalculate.animate()
                    .alpha(1f)
                    .setDuration(MainActivity.mShortAnimationDuration)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            fabCalculate.show();
                        }
                    });
        }
    }

    /**
     * Hide the calculate button
     */
    private void hideFab() {
        if (isFabButtonShowing) {

            isFabButtonShowing = false;

            fabCalculate.hide();

            // Set the content view to 0% opacity but visible, so that it is visible
            // (but fully transparent) during the animation.
            fabCalculate.setAlpha(1f);
            fabCalculate.setVisibility(View.VISIBLE);

            // Animate the content view to 100% opacity, and clear any animation
            // listener set on the view.
            fabCalculate.animate()
                    .alpha(0f)
                    .setDuration(MainActivity.mShortAnimationDuration)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            fabCalculate.setVisibility(View.INVISIBLE);
                        }
                    });
        }
    }

    private void showPayButton() {
        if (!isPayButtonShowing) {
            isPayButtonShowing = true;
            buttonPrePay.setAlpha(0f);
            buttonPrePay.setVisibility(View.VISIBLE);

            buttonPrePay.animate()
                    .alpha(1f)
                    .setDuration(MainActivity.mShortAnimationDuration)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            buttonPrePay.setVisibility(View.VISIBLE);
                        }
                    });
        }

    }

    private void hidePayButton() {
        if (isPayButtonShowing) {
            isPayButtonShowing = false;
            buttonPrePay.animate()
                    .alpha(0f)
                    .setDuration(MainActivity.mShortAnimationDuration)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            buttonPrePay.setVisibility(View.INVISIBLE);
                        }
                    });
        }
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
        textViewSubTotalPay.setText(String.valueOf(textViewPrepaidPrice.getText()));

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
                Double payAmount = Double.parseDouble(textViewPrepaidPrice.getText().toString());
                MainActivity.bus.post(payAmount);
                if (PrepaidFragment.this.payDialog != null) {
                    PrepaidFragment.this.payDialog.dismiss();
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
