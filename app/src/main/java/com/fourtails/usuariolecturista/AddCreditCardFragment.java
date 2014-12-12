package com.fourtails.usuariolecturista;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import com.fourtails.usuariolecturista.utilities.DatePickerFragmentCreditCard;

import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class AddCreditCardFragment extends Fragment {

    @InjectView(R.id.editTextCreditCardExpirationDate)
    TextView textViewCreditCardExpirationDate;

    @OnClick(R.id.editTextCreditCardExpirationDate)
    public void clickedDate() {
        showDatePicker();
    }


    public AddCreditCardFragment() {
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
        View view = inflater.inflate(R.layout.fragment_add_credit_card, container, false);
        ButterKnife.inject(this, view);
        return view;
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

    DatePickerDialog.OnDateSetListener ondate = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {

            textViewCreditCardExpirationDate.setText(String.valueOf(monthOfYear + 1)
                    + "-" + String.valueOf(year));
            //Toast.makeText(getActivity(), String.valueOf(dayOfMonth) + "-" + String.valueOf(monthOfYear + 1) + "-" + String.valueOf(year), Toast.LENGTH_LONG).show();

        }
    };

}
