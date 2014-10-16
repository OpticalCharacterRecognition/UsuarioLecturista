package com.fourtails.usuariolecturista;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BalanceFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BalanceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BalanceFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_balance, container, false);

        TextView lastReading = (TextView) view.findViewById(R.id.textViewLastReading);
        TextView lastReadingDate = (TextView) view.findViewById(R.id.textViewLastReadingDate);
        TextView totalLitersForCycle = (TextView) view.findViewById(R.id.textViewTotalLitersForCycle);

        Button resetValues = (Button) view.findViewById(R.id.buttonResetValuesForCycle);


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int lastReadingValue = sharedPreferences.getInt(PREF_LAST_READING, 0);

        String lastReadingDateValue = sharedPreferences.getString(PREF_LAST_READING_DATE, "No cycle");

        int totalLitersForCycleValue = sharedPreferences.getInt(PREF_TOTAL_LITERS_FOR_CYCLE, 0);

        lastReading.setText(String.valueOf(lastReadingValue));

        lastReadingDate.setText(lastReadingDateValue);

        totalLitersForCycle.setText(String.valueOf(totalLitersForCycleValue));


        resetValues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPreferencesValuesForReadings();
            }
        });

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

}
