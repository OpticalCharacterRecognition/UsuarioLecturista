package com.fourtails.usuariolecturista;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

//import com.facebook.Request;
//import com.facebook.Response;
//import com.facebook.Session;
//import com.facebook.SessionState;
//import com.facebook.UiLifecycleHelper;
//import com.facebook.model.GraphUser;
//import com.facebook.widget.FacebookDialog;
//import com.facebook.widget.LoginButton;

/**
 * Created by Vazh on 9/25/2014.
 */
public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";
    public static final String PREF_FACEBOOK_PROFILE_ID = "facebookProfileIdPref";
    public static final String PREF_FACEBOOK_PROFILE_NAME = "facebookProfileNamePref";

    //private UiLifecycleHelper uiHelper;

    public static final String COMES_FROM_LOGOUT = "COMES_FROM_LOGOUT";


//    private Session.StatusCallback callback = new Session.StatusCallback() {
//        @Override
//        public void call(Session session, SessionState state, Exception exception) {
//            onSessionStateChange(session, state, exception);
//        }
//    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_login, container, false);

//        LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
//        authButton.setFragment(this);
//        authButton.setReadPermissions(Arrays.asList("user_likes", "user_status", "user_checkins", "user_location", "user_birthday"));

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        uiHelper = new UiLifecycleHelper(getActivity(), callback);
//        uiHelper.onCreate(savedInstanceState);
    }

//    private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
//        if (state.isOpened()) {
//
//            Log.i(TAG, "Logged in...");
//
//            // Request user data and show the results
//            Request.newMeRequest(session, new Request.GraphUserCallback() {
//                @Override
//                public void onCompleted(GraphUser user, Response response) {
//                    if (user != null) {
//                        try {
//                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//                            SharedPreferences.Editor editor = prefs.edit();
//
//                            editor.putString(PREF_FACEBOOK_PROFILE_ID, user.getId()).apply();
//                            editor.putString(PREF_FACEBOOK_PROFILE_NAME, user.getName()).apply();
//                            Intent intent = getActivity().getIntent();
//                            boolean comesFromLogoutButton = intent.getBooleanExtra(IntermediateActivity.COMES_FROM_LOGOUT, false);
//                            intent.removeExtra(IntermediateActivity.COMES_FROM_LOGOUT);
//                            if (!comesFromLogoutButton) {
//
//                                //Starts Main Drawer Activity After login
//                                Intent intentNewActivity = new Intent(getActivity(), MainActivity.class);
//                                intent.putExtra(COMES_FROM_LOGOUT, true);
//                                LoginActivity.loginBus.post(intentNewActivity);
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }).executeAsync();
//
//
//        } else if (state.isClosed()) {
//            Log.i(TAG, "Logged out...");
//        }
//    }


    @Override
    public void onResume() {
        super.onResume();
        //uiHelper.onResume();

        // For scenarios where the main activity is launched and user
        // session is not null, the session state change notification
        // may not be triggered. Trigger it if it's open/closed.
//        Session session = Session.getActiveSession();
//        if (session != null &&
//                (session.isOpened() || session.isClosed())) {
//            onSessionStateChange(session, session.getState(), null);
//        }
//
//        uiHelper.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //uiHelper.onActivityResult(requestCode, resultCode, data);
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
    }

    @Override
    public void onPause() {
        super.onPause();
        //uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //uiHelper.onSaveInstanceState(outState);
    }

}
