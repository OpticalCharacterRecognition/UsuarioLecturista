package com.fourtails.usuariolecturista;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fourtails.usuariolecturista.ocr.CaptureActivity;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.melnykov.fab.FloatingActionButton;

import java.util.Date;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


/**
 * This is the balance fragment where it shows your metrics for the last reading, this month etc,
 * also calls for a facebook publish
 */
public class BalanceFragment extends Fragment {

    public static final String TAG = "BalanceFragment";

    public static final String PREF_LAST_READING = "lastReadingPref";
    public static final String PREF_LAST_READING_DATE = "lastReadingDatePref";
    public static final String PREF_TOTAL_LITERS_FOR_CYCLE = "totalLitersForCyclePref";
    public static final String PREF_FIRST_READING_FOR_CYCLE = "firstReadingZeroValue";

    private int lastReadingValue;
    private int totalLitersForCycleValue;

    private String lastReadingDateValue;

    // Injected views and clicklisteners
    @InjectView(R.id.textViewLastReadingDate)
    TextView lastReading;
    //@InjectView(R.id.textViewLastReadingDate) TextView lastReadingDate;
    @InjectView(R.id.textViewTotalBalance)
    TextView totalLitersForCycle;

    @InjectView(R.id.fabScan)
    FloatingActionButton fabScan;

    @OnClick(R.id.fabScan)
    public void scanButtonClicked() {
        Intent ocrCaptureActivity = new Intent(getActivity(), CaptureActivity.class);
        MainActivity.bus.post(ocrCaptureActivity);
    }

    @InjectView(R.id.fabPay)
    FloatingActionButton fabPay;

    @OnClick(R.id.fabPay)
    public void payButtonClicked() {
        Fragment payOptionsFragment = new PayOptionsFragment();
        MainActivity.bus.post(payOptionsFragment);
    }

    public BalanceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_balance, container, false);

        ButterKnife.inject(this, view);

        //Button resetValues = (Button) view.findViewById(R.id.buttonResetValuesForCycle);

        // setting the texts
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        lastReadingValue = sharedPreferences.getInt(PREF_LAST_READING, 0);

        lastReadingDateValue = sharedPreferences.getString(PREF_LAST_READING_DATE, "No cycle");

        totalLitersForCycleValue = sharedPreferences.getInt(PREF_TOTAL_LITERS_FOR_CYCLE, 0);

        lastReading.setText(String.valueOf(lastReadingValue));

        //lastReadingDate.setText(lastReadingDateValue);

        totalLitersForCycle.setText(String.valueOf(totalLitersForCycleValue));


//        resetValues.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                resetPreferencesValuesForReadings();
//            }
//        });

        // Normal Graph
/*
        GraphViewSeries exampleSeries = new GraphViewSeries(new GraphView.GraphViewData[] {
                new GraphView.GraphViewData(1, 2)
                , new GraphView.GraphViewData(10, 20)
                , new GraphView.GraphViewData(15, 46)
                , new GraphView.GraphViewData(30, 30)
        });

        GraphView graphView = new LineGraphView(
                this.getActivity() // context
                , "Consumo" // heading
        );
        graphView.getGraphViewStyle().setGridStyle(GraphViewStyle.GridStyle.NONE);
        graphView.addSeries(exampleSeries); // data

        LinearLayout layout = (LinearLayout) view.findViewById(R.id.layoutBalanceContainer);
        layout.addView(graphView);
*/

        /*
         * Custom Label graph
		 * use Date as x axis label
		 */
        Random rand = new Random();
        int size = 15;

        GraphView.GraphViewData[] data = new GraphView.GraphViewData[size];
        long now = new Date().getTime();
        data = new GraphView.GraphViewData[size];
        for (int i = 0; i < size; i++) {
            data[i] = new GraphView.GraphViewData(now + (i * 60 * 60 * 24 * 1000), rand.nextInt(20)); // next day
        }


        GraphViewSeries currentSpend = new GraphViewSeries("consumo actual", new GraphViewSeries.GraphViewSeriesStyle(Color.rgb(132, 215, 138), 6), new GraphView.GraphViewData[]{
                new GraphView.GraphViewData(1, 2)
                , new GraphView.GraphViewData(10, 15)
                , new GraphView.GraphViewData(15, 20)
                , new GraphView.GraphViewData(20, 27)
                , new GraphView.GraphViewData(30, 30)
        });

        GraphViewSeries proyectedSpend = new GraphViewSeries("consumo proyectado", new GraphViewSeries.GraphViewSeriesStyle(Color.rgb(187, 202, 204), 6), new GraphView.GraphViewData[]{
                new GraphView.GraphViewData(1, 2)
                , new GraphView.GraphViewData(10, 15)
                , new GraphView.GraphViewData(15, 20)
                , new GraphView.GraphViewData(20, 32)
                , new GraphView.GraphViewData(30, 60)
        });


        GraphViewSeries prepaidBar = new GraphViewSeries("Prepago", new GraphViewSeries.GraphViewSeriesStyle(Color.rgb(200, 50, 00), 3), new GraphView.GraphViewData[]{
                new GraphView.GraphViewData(0, 40)
                , new GraphView.GraphViewData(30, 40)
        });

        GraphViewSeries paymentLimit = new GraphViewSeries("Fecha de corte", null, new GraphView.GraphViewData[]{
                new GraphView.GraphViewData(25, 60)
                , new GraphView.GraphViewData(25, 0)
        });


        /** determine if line of bar **/
        GraphView graphView = new LineGraphView(
                this.getActivity()
                , "Consumo"
        );
        //((LineGraphView) graphView).setDrawBackground(true);
        ((LineGraphView) graphView).setDrawDataPoints(true);
        ((LineGraphView) graphView).setDataPointsRadius(7f);

        graphView.addSeries(proyectedSpend); // data
        graphView.addSeries(currentSpend); // data
        graphView.addSeries(prepaidBar); // data
        graphView.addSeries(paymentLimit); // data
        // set legend
        graphView.setShowLegend(true);
        graphView.setLegendAlign(GraphView.LegendAlign.BOTTOM);
        graphView.getGraphViewStyle().setLegendBorder(10);
        graphView.getGraphViewStyle().setLegendSpacing(10);
        graphView.getGraphViewStyle().setLegendWidth(350);
        graphView.setHorizontalLabels(new String[]{"1", "10", "15", "20", "25", "30"});
        // set view port, start=2, size=40

		/*
         * date as label formatter
		 */
       /* final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d");
        graphView.setCustomLabelFormatter(new CustomLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    Date d = new Date((long) value);
                    return dateFormat.format(d);
                }
                return null; // let graphview generate Y-axis label for us
            }
        });*/
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.layoutBalanceContainer);
        layout.addView(graphView);


//        Button button = (Button) view.findViewById(R.id.buttonPaypalTest);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getActivity(), BalancePaypalActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        textViewBalanceMoney = (TextView) view.findViewById(R.id.textViewBalanceMoney);
//        textViewBalanceMoneyOut = (TextView) view.findViewById(R.id.textViewBalanceMoneyOutOf);
//        progressBar = (ProgressBar) view.findViewById(R.id.progressBarBalance);
//
//
//
//        SharedPreferences settings = getActivity().getApplicationContext().getSharedPreferences(GoLocky.PREFERENCES, 0);
//        final String userAccountName = settings.getString(GoLocky.PREFERENCES_LOGGED_USER_EMAIL, "");
//
//        /** get user call so we can get the updated balance (might not want to do this and just add
//         * the reward amount that we just got)**/
//        new AsyncTask<Void, Void, Integer>() {
//
//            Double balance = 0.0;
//
//            @Override
//            protected Integer doInBackground(Void... params) {
//                try {
//                    // Use a builder to help formulate the API request.
//                    Backend.Builder builder = new Backend.Builder(
//                            AndroidHttp.newCompatibleTransport(),
//                            new AndroidJsonFactory(),
//                            null);
//                    Backend service = builder.build();
//
//                    // First we try to get the user
//                    MessagesGetUser messagesGetUser = new MessagesGetUser();
//                    messagesGetUser.setEmail(userAccountName);
//
//                    MessagesGetUserResponse response = service.user().get(messagesGetUser).execute();
//
//                    if (response.getOk()) {
//                        balance = response.getBalance();
//                        Log.i("golocky_get_user", response.toPrettyString());
//                        return GoLocky.TRANSACTION_GET_USER_OK_CODE;
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    Log.e("golocky", e.getMessage());
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Integer transactionResponse) {
//                switch (transactionResponse) {
//                    case GoLocky.TRANSACTION_GET_USER_OK_CODE:
//                        textViewBalanceMoney.setText(balance.toString());
//                        textViewBalanceMoneyOut.setText(balance.toString());
//                        int progressBarBalance = (int) ((balance * 100) / 200);
//                        progressBar.setProgress(progressBarBalance);
//                        break;
//                    default:
//                        Toast.makeText(getActivity(), "There is an error retrieving the user", Toast.LENGTH_LONG).show();
//                }
//            }
//        }.execute();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set title
        MainActivity.bus.post(TAG);
    }

    /**
     * Resets the preferences to 0 and then refreshes the fragment
     */
    private void resetPreferencesValuesForReadings() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(BalanceFragment.PREF_LAST_READING, 0).commit(); // last reading value (the one we just scanned)
        editor.putString(BalanceFragment.PREF_LAST_READING_DATE, "default").commit(); // last reading date
        editor.putInt(BalanceFragment.PREF_TOTAL_LITERS_FOR_CYCLE, 0).commit(); // total liters for this cycle

        // Refresh the fragment
        Fragment fragment = new BalanceFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment).commit();

    }
}
