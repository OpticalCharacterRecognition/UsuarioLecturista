package com.fourtails.usuariolecturista;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphObjectList;
import com.facebook.model.GraphUser;
import com.facebook.model.OpenGraphAction;
import com.facebook.model.OpenGraphObject;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.LoginButton;
import com.facebook.widget.WebDialog;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Vazh on 9/25/2014.
 */
public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";
    public static final String PREF_FACEBOOK_PROFILE_ID = "facebookProfileIdPref";
    public static final String PREF_FACEBOOK_PROFILE_NAME = "facebookProfileNamePref";

    private UiLifecycleHelper uiHelper;

    private TextView userInfoTextView;

    public static final String COMES_FROM_LOGOUT = "COMES_FROM_LOGOUT";

    public static String facebookPicId = "";


    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_login, container, false);

        LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
        authButton.setFragment(this);
        authButton.setReadPermissions(Arrays.asList("user_likes", "user_status", "user_checkins", "user_location", "user_birthday"));

//        //facebook share
//        Button facebookShareButton = (Button) view.findViewById(R.id.button);
//        facebookShareButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                shareLinkClicked();
//            }
//        });
//
//        //facebook open graph
//        Button facebookOpenGraph = (Button) view.findViewById(R.id.buttonOpenGraph);
//        facebookOpenGraph.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openGraphClicked();
//            }
//        });
//
//        //facebook open graph
//        Button requestPermissions = (Button) view.findViewById(R.id.buttonRequestPermissions);
//        requestPermissions.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                requestPublishPermissionsClicked();
//            }
//        });
//
//        userInfoTextView = (TextView) view.findViewById(R.id.userInfoTextView);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);
    }

    private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {

            Log.i(TAG, "Logged in...");

            //userInfoTextView.setVisibility(View.VISIBLE);
            // Request user data and show the results
            Request.newMeRequest(session, new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    if (user != null) {
                        try {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            SharedPreferences.Editor editor = prefs.edit();

                            editor.putString(PREF_FACEBOOK_PROFILE_ID, user.getId()).apply();
                            editor.putString(PREF_FACEBOOK_PROFILE_NAME, user.getName()).apply();

                            //Bitmap bitmap = BitmapFactory.decodeStream(imgUrl      // tried this also
                            //.openConnection().getInputStream());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).executeAsync();

            //TODO: delete this one?
            Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {

                @Override
                public void onCompleted(GraphUser user, Response response) {
                    if (user != null) {
                        // Display the parsed user info
                        //userInfoTextView.setText(buildUserInfoDisplay(user));

                        // If it wasn't called from the logout button
                        Intent intent = getActivity().getIntent();
                        boolean comesFromLogoutButton = intent.getBooleanExtra(IntermediateActivity.COMES_FROM_LOGOUT, false);
                        intent.removeExtra(IntermediateActivity.COMES_FROM_LOGOUT);
                        if (!comesFromLogoutButton) {
                            Log.i(TAG, "opening next activity");
//                            Intent intentNewActivity = new Intent(getActivity(), IntermediateActivity.class);
//                            startActivity(intentNewActivity);
                            //Starts Main Drawer Activity After login
                            Intent intentNewActivity = new Intent(getActivity(), MainActivity.class);
                            intent.putExtra(COMES_FROM_LOGOUT, true);
                            startActivity(intentNewActivity);
                        }
                    }
                }
            });
        } else if (state.isClosed()) {
            Log.i(TAG, "Logged out...");
            //userInfoTextView.setVisibility(View.INVISIBLE);
        }
    }

    private String buildUserInfoDisplay(GraphUser user) {
        StringBuilder userInfo = new StringBuilder("");

        // Example: typed access (name)
        // - no special permissions required
        userInfo.append(String.format("Name: %s\n\n",
                user.getName()));

        // Example: typed access (birthday)
        // - requires user_birthday permission
        userInfo.append(String.format("Birthday: %s\n\n",
                user.getBirthday()));

        // Example: partially typed access, to location field,
        // name key (location)
        // - requires user_location permission
        userInfo.append(String.format("Location: %s\n\n",
                user.getLocation().getProperty("name")));

        // Example: access via property name (locale)
        // - no special permissions required
        userInfo.append(String.format("Locale: %s\n\n",
                user.getProperty("locale")));

        // Example: access via key for array (languages)
        // - requires user_likes permission
        JSONArray languages = (JSONArray) user.getProperty("languages");
        if (languages.length() > 0) {
            ArrayList<String> languageNames = new ArrayList<String>();

            // Get the data from creating a typed interface
            // for the language data.
            GraphObjectList<MyGraphLanguage> graphObjectLanguages =
                    GraphObject.Factory.createList(languages,
                            MyGraphLanguage.class);

            // Iterate through the list of languages
            for (MyGraphLanguage language : graphObjectLanguages) {
                // Add the language name to a list. Use the name
                // getter method to get access to the name field.
                languageNames.add(language.getName());
            }

            userInfo.append(String.format("Languages: %s\n\n",
                    languageNames.toString()));
        }

        return userInfo.toString();
    }

    public void requestPublishPermissionsClicked() {
        Session session = Session.getActiveSession();
        if (session.isOpened() && !session.isClosed()) {
            //session.openForRead(new Session.OpenRequest(this));

            session.requestNewPublishPermissions(new Session.NewPermissionsRequest(this, Arrays.asList("publish_actions")));
        }
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


        OpenGraphObject lectura = OpenGraphObject.Factory.createForPost("fourtailsintegration:lectura");
        lectura.setProperty("title", "Lectura Realizada!");
        lectura.setProperty("image", "http://upload.wikimedia.org/wikipedia/en/8/82/Water_meter_register.jpg");
        lectura.setProperty("url", "http://www.google.com");
        lectura.setProperty("description", "Medidor de agua");

        OpenGraphAction action = GraphObject.Factory.create(OpenGraphAction.class);
        action.setProperty("lectura", lectura);

        FacebookDialog shareDialog = new FacebookDialog.OpenGraphActionDialogBuilder(getActivity(), action, "fourtailsintegration:realizar", "lectura")
                .build();
        uiHelper.trackPendingDialogCall(shareDialog.present());
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

    @Override
    public void onResume() {
        super.onResume();
        //uiHelper.onResume();

        // For scenarios where the main activity is launched and user
        // session is not null, the session state change notification
        // may not be triggered. Trigger it if it's open/closed.
        Session session = Session.getActiveSession();
        if (session != null &&
                (session.isOpened() || session.isClosed())) {
            onSessionStateChange(session, session.getState(), null);
        }

        uiHelper.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //uiHelper.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
                Log.e("Activity", String.format("Error: %s", error.toString()));
            }

            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
                Log.i("Activity", "Success!");
            }
        });
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    private interface MyGraphLanguage extends GraphObject {
        // Getter for the ID field
        String getId();

        // Getter for the Name field
        String getName();
    }

}
