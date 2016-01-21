package com.fourtails.usuariolecturista.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.fourtails.usuariolecturista.MainActivity;
import com.fourtails.usuariolecturista.R;
import com.fourtails.usuariolecturista.camera.CameraScreenActivity;
import com.fourtails.usuariolecturista.model.ChartPrepay;
import com.fourtails.usuariolecturista.ottoEvents.AndroidBus;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Prepaid Mode fragment will be shown instead of readings fragment
 */
public class PrepayModeFragment extends Fragment {

//    @Bind(R.id.textViewPrepaidDays)
//    TextView textViewPrepaidDays;

    @Bind(R.id.textViewPrepaidMeters)
    TextView textViewPrepaidMeters;

    @Bind(R.id.textViewPrepaidBeginDate)
    TextView textViewPrepaidBeginDate;

//    @Bind(R.id.textViewPrepaidEndDate)
//    TextView textViewPrepaidEndDate;

    @Bind(R.id.progressBarPrepaid)
    ProgressBar progressBarPrepaid;

//    @Bind(R.id.progressBarPrepaidLoading)
//    ProgressBar progressBarPrepaidLoading;

    @OnClick(R.id.fabScanPrepay)
    public void scanButtonClicked() {
        Intent cameraActivity = new Intent(getActivity(), CameraScreenActivity.class);
        MainActivity.bus.post(cameraActivity);
    }

    public static Bus bus;

    public PrepayModeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_prepay_mode, container, false);
        ButterKnife.bind(this, view);

        bus = new AndroidBus();
        bus.register(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set title
        MainActivity.bus.post(getResources().getString(R.string.toolbarTitlePrepaidMode));
        updatePrepay(1);
    }

    @Subscribe
    public void updatePrepay(Integer option) {
        List<ChartPrepay> prepays = getCurrentPrepay();
        String beginDate = "";
        String meters = "";
        Time time = new Time();
        if (prepays != null) {
            for (ChartPrepay i : prepays) {
                meters = String.valueOf(i.prepay);
                time.set(i.timeInMillis);
                beginDate = time.format("%d/%m/%Y");
            }
            textViewPrepaidMeters.setText(meters);
            textViewPrepaidBeginDate.setText(beginDate);
        }
    }

    private List<ChartPrepay> getCurrentPrepay() {
        Time time = new Time(Time.getCurrentTimezone());
        time.setToNow();
        return new Select()
                .from(ChartPrepay.class)
                .orderBy("timeInMillis ASC")
//                .where("month >= ?", time.month - range)
//                .and("year = ?", time.year)
                .execute();
    }

}
