package com.fourtails.usuariolecturista;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.OpenGraphAction;
import com.facebook.model.OpenGraphObject;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import java.util.Date;
import java.util.Random;


/**
 * This is the balance fragment where it shows your metrics for the last reading, this month etc,
 * also calls for a facebook publish
 */
public class BalanceFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private final String TAG = "BalanceFragment";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView textViewBalanceMoney;
    private TextView textViewBalanceMoneyOut;

    private ProgressBar progressBar;

    private OnFragmentInteractionListener mListener;

    public static final String PREF_LAST_READING = "lastReadingPref";
    public static final String PREF_LAST_READING_DATE = "lastReadingDatePref";
    public static final String PREF_TOTAL_LITERS_FOR_CYCLE = "totalLitersForCyclePref";
    public static final String PREF_FIRST_READING_FOR_CYCLE = "firstReadingZeroValue";

    private int lastReadingValue;
    private int totalLitersForCycleValue;

    private String lastReadingDateValue;

    private UiLifecycleHelper uiHelper;



    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BalanceFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BalanceFragment newInstance(String param1, String param2) {
        BalanceFragment fragment = new BalanceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public BalanceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        uiHelper = new UiLifecycleHelper(getActivity(), null);
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_balance, container, false);

        // View components
        TextView lastReading = (TextView) view.findViewById(R.id.textViewLastReadingDate);
        TextView lastReadingDate = (TextView) view.findViewById(R.id.textViewLastReadingDate);
        TextView totalLitersForCycle = (TextView) view.findViewById(R.id.textViewTotalBalance);

        Button resetValues = (Button) view.findViewById(R.id.buttonResetValuesForCycle);

        //facebook open graph
        Button facebookOpenGraph = (Button) view.findViewById(R.id.buttonFBOpenGraph);
        facebookOpenGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGraphClicked();
            }
        });


        //facebook share
//        Button facebookShareButton = (Button) view.findViewById(R.id.buttonFB);
//        facebookShareButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                shareLinkClicked();
//            }
//        });


        // setting the texts
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        lastReadingValue = sharedPreferences.getInt(PREF_LAST_READING, 0);

        lastReadingDateValue = sharedPreferences.getString(PREF_LAST_READING_DATE, "No cycle");

        totalLitersForCycleValue = sharedPreferences.getInt(PREF_TOTAL_LITERS_FOR_CYCLE, 0);

        lastReading.setText(String.valueOf(lastReadingValue));

        lastReadingDate.setText(lastReadingDateValue);

        totalLitersForCycle.setText(String.valueOf(totalLitersForCycleValue));


        resetValues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPreferencesValuesForReadings();
            }
        });

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

    /**
     * Used to create a Facebook shareDialog
     */
    public void shareLinkClicked() {
        // If facebook App is installed calls to that app and handles the post from there
        if (FacebookDialog.canPresentShareDialog(getActivity().getApplicationContext(),
                FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
            // Publish the post using the Share Dialog
            FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(getActivity())
                    .setLink("https://developers.facebook.com/android")
                    .build();
            uiHelper.trackPendingDialogCall(shareDialog.present());

        } else { //If the app is not installed then creates a really cool dialog which might be a better choice
            // Fallback. For example, publish the post using the Feed Dialog
            publishFeedDialog();
        }
    }

    private void publishFeedDialog() {
        Bundle params = new Bundle();
        params.putString("name", "Facebook SDK for Android");
        params.putString("caption", "Build great social apps and get more installs.");
        params.putString("description", "The Facebook SDK for Android makes it easier and faster to develop Facebook integrated Android apps.");
        params.putString("link", "https://developers.facebook.com/android");
        params.putString("picture", "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png");

        WebDialog feedDialog = (
                new WebDialog.FeedDialogBuilder(getActivity(),
                        Session.getActiveSession(),
                        params))
                .setOnCompleteListener(new WebDialog.OnCompleteListener() {

                    @Override
                    public void onComplete(Bundle values,
                                           FacebookException error) {
                        if (error == null) {
                            // When the story is posted, echo the success
                            // and the post Id.
                            final String postId = values.getString("post_id");
                            if (postId != null) {
                                Toast.makeText(getActivity(),
                                        "Posted story, id: " + postId,
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // User clicked the Cancel button
                                Toast.makeText(getActivity().getApplicationContext(),
                                        "Publish cancelled",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else if (error instanceof FacebookOperationCanceledException) {
                            // User clicked the "x" button
                            Toast.makeText(getActivity().getApplicationContext(),
                                    "Publish cancelled",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Generic, ex: network error
                            Toast.makeText(getActivity().getApplicationContext(),
                                    "Error posting story",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                })
                .build();
        feedDialog.show();
    }

    /**
     * This kind of "sharing" is more customized
     */
    public void openGraphClicked() {
/*        OpenGraphAction action = GraphObject.Factory.create(OpenGraphAction.class);
        action.setProperty("lectura", "https://example.com/book/Snow-Crash.html");

        FacebookDialog shareDialog = new FacebookDialog.OpenGraphActionDialogBuilder(getActivity(), action, "lectura.realizar", "lectura")
                .build();
        uiHelper.trackPendingDialogCall(shareDialog.present());*/


        OpenGraphObject lectura = OpenGraphObject.Factory.createForPost("lecturista:lectura");
        lectura.setProperty("title", "Lectura Realizada de " + lastReadingValue + "!");
        lectura.setProperty("image", "http://upload.wikimedia.org/wikipedia/en/8/82/Water_meter_register.jpg");
        lectura.setProperty("url", "http://www.google.com");
        lectura.setProperty("description", "Realize una lectura de " + lastReadingValue + " litros el " + lastReadingDateValue + ". Mi consumo total este mes es de " + totalLitersForCycleValue);

        OpenGraphAction action = GraphObject.Factory.create(OpenGraphAction.class);
        action.setProperty("lectura", lectura);

        FacebookDialog shareDialog = new FacebookDialog.OpenGraphActionDialogBuilder(getActivity(), action, "lecturista:realizar", "lectura")
                .build();
        uiHelper.trackPendingDialogCall(shareDialog.present());
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
//            @Override
//            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
//                Log.e("Activity", String.format("Error: %s", error.toString()));
//            }
//
//            @Override
//            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
//                Log.i("Activity", "Success!");
//            }
//        });
//    }
}
