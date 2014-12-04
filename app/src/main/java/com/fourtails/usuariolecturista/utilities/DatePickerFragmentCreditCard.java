package com.fourtails.usuariolecturista.utilities;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.View;

/**
 * A simple {@link Fragment} subclass.
 */
public class DatePickerFragmentCreditCard extends DialogFragment {

    DatePickerDialog.OnDateSetListener ondateSet;

    public DatePickerFragmentCreditCard() {
        // Required empty public constructor
    }

    public void setCallBack(DatePickerDialog.OnDateSetListener ondate) {
        ondateSet = ondate;
    }

    private int year, month, day;
    private long currentDateInMillis;

    @SuppressLint("NewApi")
    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        year = args.getInt("year");
        month = args.getInt("month");
        day = args.getInt("day");
        currentDateInMillis = args.getLong("currentDateInMillis");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), ondateSet,
                year,
                month,
                day) {
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                int year = getContext().getResources()
                        .getIdentifier("android:id/day", null, null);
                if (year != 0) {
                    View yearPicker = findViewById(year);
                    if (yearPicker != null) {
                        yearPicker.setVisibility(View.GONE);
                    }
                }
                int calendarView = getContext().getResources()
                        .getIdentifier("android:id/calendar_view", null, null);
                if (calendarView != 0) {
                    View calendarViewPicker = findViewById(calendarView);
                    if (calendarViewPicker != null) {
                        calendarViewPicker.setVisibility(View.GONE);
                        calendarViewPicker.setEnabled(false);
                    }
                }
            }
        };
        datePickerDialog.getDatePicker().setMinDate(currentDateInMillis);
        datePickerDialog.setTitle("");
        return datePickerDialog;
    }

}