package tcss450.uw.edu.chatapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import tcss450.uw.edu.chatapp.chats.Chats;
import tcss450.uw.edu.chatapp.chats.ChatsFragment;
import tcss450.uw.edu.chatapp.chats.DeleteChatFragment;
import tcss450.uw.edu.chatapp.chats.Message;
import tcss450.uw.edu.chatapp.chats.NewChatFragment;
import tcss450.uw.edu.chatapp.contacts.ContactsTabActivity;
import tcss450.uw.edu.chatapp.model.Contacts;
import tcss450.uw.edu.chatapp.chats.MessageFragment;
import tcss450.uw.edu.chatapp.utils.SendPostAsyncTask;
import tcss450.uw.edu.chatapp.utils.WaitFragment;
import tcss450.uw.edu.chatapp.weather.SavedLocationsFragment;
import tcss450.uw.edu.chatapp.weather.WeatherDisplayCityFragment;
import tcss450.uw.edu.chatapp.weather.WeatherDisplayLatLngFragment;
import tcss450.uw.edu.chatapp.weather.WeatherDisplayZipCodeFragment;
import tcss450.uw.edu.chatapp.weather.MapsActivity;
import tcss450.uw.edu.chatapp.weather.WeatherFragment;
import tcss450.uw.edu.chatapp.weather.ZipCodeFragment;

/**
 * This activity is loaded after login. The navigation menu on the top left is main way of
 * navigating to other fragments/activity.
 */
public class HomeActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        ChatsFragment.OnChatListFragmentInteractionListener,
        DeleteChatFragment.DeleteChatFragmentInteractionListener,
        NewChatFragment.OnNewChatListFragmentInteractionListener,
        WaitFragment.OnFragmentInteractionListener,
        WeatherFragment.OnWeatherFragmentInteractionListener,
        ZipCodeFragment.OnZipCodeFragmentInteractionListener,
        LandingPageFragment.OnLandingPageFragmentInteractionListener,
        WeatherDisplayZipCodeFragment.OnWeatherDisplayZipCodeFragmentInteractionListener,
        WeatherDisplayLatLngFragment.OnWeatherDisplayLatLngFragmentInteractionListener,
        SavedLocationsFragment.OnSavedLocationsFragmentInteractionListener {

    public static final String MESSAGE_CONTACTS_CONTACTS = "contacts";
    public static final String MESSAGE_CONTACTS_EMAIL = "email";
    public static final String MESSAGE_CHATID = "chat_ID";
    public static final String MESSAGE_NICKNAME = "msg_nickname";
    public static final String OPEN_CHAT = "open_chat";
    private FloatingActionButton mFab;
    Bundle thisBundle;
    private String mEmail;
    private ArrayList<Contacts> mContacts;
    private WeatherFragment weatherFragment;
    private FragmentPagerAdapter mAdapterViewPager;
    private ViewPager mViewPager;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private static final int MY_PERMISSIONS_LOCATIONS = 8414;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //getContacts();
            }
        });
        mFab.hide();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("args");
        mEmail = intent.getStringExtra(MainActivity.HOME_LOGIN_EMAIL);
        Log.v("EMAIL", mEmail);
        thisBundle = bundle;

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        TextView t = (TextView) headerView.findViewById(R.id.header_curEmail);
        t.setText(mEmail);

        weatherFragment = new WeatherFragment();

        if (savedInstanceState == null) {
            if (findViewById(R.id.content_home_container) != null) {
                //Fragment landing = new LandingPageFragment();
                if (getIntent().getBooleanExtra(getString(R.string.keys_intent_notifification_msg), true)) {
                    Log.d("NOTIF_MSG", "opening framgent...");
                    Message msg = (Message) getIntent().getExtras().get("message");
                    Bundle args = new Bundle();
                    args.putInt(MESSAGE_CHATID, msg.getChatId());
                    args.putString(MESSAGE_NICKNAME, msg.getNickname());
                    MessageFragment messageFragment = new MessageFragment();
                    messageFragment.setArguments(args);
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.content_home_container, messageFragment, "message fragment")
                            .commit();
//                    if (getIntent().getExtras().get("newChatMsg") != null){
//                        Message newChatMsg = (Message) getIntent().getExtras().get("newChatMsg");
//                        Bundle args = new Bundle();
//                        Log.d("newCHatNotif", "Homectivty: " + newChatMsg.getChatId() +" "+ newChatMsg.getChatName());
//
//                        args.putInt(MESSAGE_CHATID, newChatMsg.getChatId());
//                        args.putString(MESSAGE_NICKNAME, newChatMsg.getChatName());
//                        MessageFragment messageFragment = new MessageFragment();
//                        Log.d("newCHatNotif", "Homectivty: " + messageFragment);
//                        messageFragment.setArguments(args);
//                        getSupportFragmentManager().beginTransaction()
//                                .add(R.id.content_home_container, messageFragment)
//                                .commit();
//                    } else {
//
//                    }
                } else {
                    Fragment landing = new LandingPageFragment();
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.content_home_container, landing, "landing page fragment")
                            .commit();
                }
            }
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION
                            , Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_LOCATIONS);
        } else {
            //The user has already allowed the use of Locations. Get the current location.
            requestLocation();
        }

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                    mCurrentLocation = location;
                    WeatherFragment.setLocation(location);
                    LandingPageFragment.setLocation(location);
                    Log.d("LOCATION UPDATE!", location.toString());
                }
            }
        };
        createLocationRequest();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            mFab.hide();
            LandingPageFragment landingPageFragment = new LandingPageFragment();
            Bundle landingPageBundle = new Bundle();
            landingPageBundle.putSerializable("lat", mCurrentLocation.getLatitude());
            landingPageBundle.putSerializable("lon", mCurrentLocation.getLongitude());
            landingPageFragment.setArguments(landingPageBundle);
            loadFragment(landingPageFragment);
        } else if (id == R.id.nav_chat) {
            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .appendPath(getString(R.string.ep_base_url))
                    .appendPath(getString(R.string.ep_chats_base))
                    .appendPath(getString(R.string.ep_getallchats))
                    .build();
            JSONObject messageJson = new JSONObject();
            try {
                messageJson.put("email", mEmail);
                Log.e("IN_JSON", "post body email");
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("IN_JSON", "didnt put email");
            }
            new SendPostAsyncTask.Builder(uri.toString(), messageJson)
                    .onPreExecute(this::onWaitFragmentInteractionShow)
                    .onPostExecute(this::handleChatsPostExecute)
                    .onCancelled(error -> Log.e("SEND_TAG", error))
                    .build().execute();
        } else if (id == R.id.nav_contacts) {
            Bundle args = new Bundle();
            args.putSerializable("email", mEmail);
            Intent contactIntent = new Intent(HomeActivity.this, ContactsTabActivity.class);
            contactIntent.putExtras(args);
            HomeActivity.this.startActivity(contactIntent);
            //getContacts();
        } else if (id == R.id.nav_logout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            builder.setCancelable(true);
            builder.setTitle("Are you sure you want to logout?");
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    logout();
                }
            });
            builder.show();
        } else if (id == R.id.nav_weather) {
            mFab.hide();
            FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_home_container, weatherFragment, "MY_FRAGMENT")
                    .addToBackStack(null);
            transaction.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void handleChatsPostExecute(final String result) {
        try {
            JSONObject root = new JSONObject(result);
            if (root.has("success") && root.getBoolean("success")) {

                JSONArray data = root.getJSONArray("data");
                ArrayList<Chats> chatList = new ArrayList<>();
                for (int i = 0; i < data.length(); i++) {
                    JSONObject jsonChats = data.getJSONObject(i);
                    chatList.add(new Chats.Builder("",
                            "", "")
                            .addChatID(jsonChats.getInt("chatid"))
                            .addChatName(jsonChats.getString("name").replace(mEmail, ""))
                            .build());
                }
                JSONObject usernamObj = root.getJSONObject("currentuser");
                String currentUsername = usernamObj.getString("username");
                Log.d("GETALLCHATS", currentUsername);
                Chats[] chatsAsArray = new Chats[chatList.size()];
                chatsAsArray = chatList.toArray(chatsAsArray);
                Bundle args = new Bundle();
                args.putString("email", mEmail);
                args.putString("currentUser", currentUsername);
                args.putSerializable(ChatsFragment.ARG_CHATS, chatsAsArray);
                ChatsFragment chatFrag = new ChatsFragment();
                chatFrag.setArguments(args);
                onWaitFragmentInteractionHide();
                loadFragment(chatFrag);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
            //notify user
            onWaitFragmentInteractionHide();
        }
    }

    private void logout() {
        SharedPreferences prefs =
                getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        //remove the saved credentials from StoredPrefs
        prefs.edit().remove(getString(R.string.keys_prefs_password)).apply();
        prefs.edit().remove(getString(R.string.keys_prefs_email)).apply();
        //close the app
//        finishAndRemoveTask();
        //or close this activity and bring back the Login
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        //End this Activity and remove it from the Activity back stack.
        finish();
    }

    private void loadFragment(Fragment frag) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_home_container, frag)
                .addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onChatListFragmentInteraction(Chats item) {
        mFab.hide();
        MessageFragment messageFragment = new MessageFragment();
        Message msg = new Message.Builder(item.getEmail(), item.getNickname(), item.getChatID()).build();
        Bundle args = new Bundle();
        args.putString(MESSAGE_NICKNAME, item.getChatName());
        args.putInt(MESSAGE_CHATID, item.getChatID());
        messageFragment.setArguments(args);
        loadFragment(messageFragment);
    }

    @Override
    public void onWaitFragmentInteractionShow() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.content_home_container, new WaitFragment(), "WAIT")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onWaitFragmentInteractionHide() {
        getSupportFragmentManager()
                .beginTransaction()
                .remove(getSupportFragmentManager().findFragmentByTag("WAIT"))
                .commit();
    }


    @Override
    public void onSearchButtonClicked(String zipCodeString) {
        WeatherDisplayZipCodeFragment frag = new WeatherDisplayZipCodeFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("zip code", zipCodeString);
        frag.setArguments(bundle);
        loadFragment(frag);
    }

    @Override
    public void deleteChatFragmentInteraction(Chats item) {
    }

    @Override
    public void newChatFragmentInteraction(Contacts item, String currentUsername) {
        //webserivece call to start new chat, retrieve chatid and put into message fragment
        Log.d("NewChatSingle", "Adding new single chat..." + item.getEmail());
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_chats_base))
                .appendPath(getString(R.string.ep_newchat))
                .build();
        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put("chatName", "Single: " + item.getNickname() + ", " + currentUsername);
            messageJson.put("email1", mEmail);
            messageJson.put("email2", item.getEmail());
        } catch (JSONException e) {
            Log.d("NewChatSingle", "array wrong");
            e.printStackTrace();
        }
        new SendPostAsyncTask.Builder(uri.toString(), messageJson)
                .onPreExecute(this::onWaitFragmentInteractionShow)
                .onPostExecute(this::handleNewChat)
                .onCancelled(error -> Log.e("SEND_TAG", error))
                .build().execute();
    }

    private void handleNewChat(final String result) {
        try {
            JSONObject root = new JSONObject(result);
            Log.d("NewChatSingle", "Result: " + result);
            if (root.has("success") && root.getBoolean("success")) {
                JSONArray data = root.getJSONArray("data");

                String chatName = root.getString("chatname");

//                JSONArray chatIdArray = root.getJSONArray("chatid");
//                JSONObject chatIdObj = chatIdArray.getJSONObject(0);
//                int chatID = chatIdObj.getInt("chatid");
                int chatID = root.getInt("chatid");

                Log.d("NewChatSingle", "ChatID: " + chatID);
                MessageFragment messageFragment = new MessageFragment();
                Bundle args = new Bundle();
                args.putString(MESSAGE_NICKNAME, chatName.replace(mEmail, ""));
                args.putInt(MESSAGE_CHATID, chatID);
                messageFragment.setArguments(args);
                onWaitFragmentInteractionHide();
                loadFragment(messageFragment);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("NewChatSingle", "ERROR: " + e.getMessage());
            onWaitFragmentInteractionHide();
        }
    }

    @Override
    public void onSavedLocationClicked(String cityString) {
        WeatherDisplayCityFragment weatherDisplayCityFragment = new WeatherDisplayCityFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("city", cityString);
        weatherDisplayCityFragment.setArguments(bundle);
        loadFragment(weatherDisplayCityFragment);
    }

    @Override
    public void onDeleteLocationClicked(String slot) {
        SharedPreferences prefs =
                getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        prefs.edit().remove("keys_prefs_location" + slot).apply();
        Fragment frg = null;
        frg = getSupportFragmentManager().findFragmentByTag("saved locations fragment");
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.detach(frg);
        ft.attach(frg);
        ft.commit();
    }

    // Deleting the InstanceId (Firebase token) must be done asynchronously. Good thing
    // we have something that allows us to do that.
    class DeleteTokenAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            //since we are already doing stuff in the background, go ahead
            //and remove the credentials from shared prefs here.
            SharedPreferences prefs =
                    getSharedPreferences(
                            getString(R.string.keys_shared_prefs),
                            Context.MODE_PRIVATE);
            prefs.edit().remove(getString(R.string.keys_prefs_password)).apply();
            prefs.edit().remove(getString(R.string.keys_prefs_email)).apply();
            try {
                //this call must be done asynchronously.
                FirebaseInstanceId.getInstance().deleteInstanceId();
            } catch (IOException e) {
                Log.e("FCM", "Delete error!");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //close the app
            finishAndRemoveTask();
            //or close this activity and bring back the Login
            Intent i = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(i);
            //Ends this Activity and removes it from the Activity back stack.
//             finish();
        }
    }

    @Override
    public void onMyCurrentLocationButtonClicked(Double lat, Double lon) {
        WeatherDisplayLatLngFragment weatherDisplayLatLngFragment = new WeatherDisplayLatLngFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("lat", lat);
        bundle.putSerializable("lon", lon);
        weatherDisplayLatLngFragment.setArguments(bundle);
        loadFragment(weatherDisplayLatLngFragment);
    }

    @Override
    public void onZipCodeButtonClicked() {
        ZipCodeFragment zipCodeFragment = new ZipCodeFragment();
        loadFragment(zipCodeFragment);
    }

    @Override
    public void onMapButtonClicked() {
        if (mCurrentLocation == null) {
            Snackbar.make(findViewById(android.R.id.content), "Please wait for location to enable", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        } else {
            Intent i = new Intent(HomeActivity.this, MapsActivity.class);
            //pass the current location on to the MapActivity when it is loaded
            i.putExtra("LOCATION", mCurrentLocation);
            startActivity(i);
        }
    }

    @Override
    public void onSavedLocationsButtonClicked() {
        SavedLocationsFragment savedLocationsFragment = new SavedLocationsFragment();
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_home_container, savedLocationsFragment, "saved locations fragment")
                .addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onSaveLocationButtonClicked(String cityString) {
        SharedPreferences prefs =
                getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        for (int i = 1; i <= 9; i++) {
            if (prefs.getString("keys_prefs_location" + Integer.toString(i), "").equals(cityString)) {
                Toast.makeText(getApplicationContext(), "Error: Location Already Saved", Toast.LENGTH_LONG).show();
                return;
            }
        }
        for (int i = 1; i <= 9; i++) {
            if (prefs.getString("keys_prefs_location" + Integer.toString(i), "") == null
                    || prefs.getString("keys_prefs_location" + Integer.toString(i), "").equals("")) {
                prefs.edit().putString("keys_prefs_location" + Integer.toString(i), cityString).apply();
                Toast.makeText(getApplicationContext(), "Location Saved!", Toast.LENGTH_LONG).show();
                return;
            }
        }
        Toast.makeText(getApplicationContext(), "No Saved Location Space", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_LOCATIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // locations-related task you need to do.
                    requestLocation();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d("PERMISSION DENIED", "Nothing to see or do here.");
                    //Shut down the app. In production release, you would let the user
                    // know why the app is shutting down...maybe ask for permission again?
                    finishAndRemoveTask();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request }
        }
    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("REQUEST LOCATION", "User did NOT allow permission to request location!");
        } else {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                setLocation(location);
                                WeatherFragment.setLocation(location);
                                LandingPageFragment.setLocation(location);
                                Log.d("LOCATION", location.toString());
                            }
                        }
                    });
        }
    }

    /**
     * Create and configure a Location Request used when retrieving location updates
     */
    protected void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null /* Looper */);
        }
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportActionBar().setTitle(R.string.app_name);
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onWeatherClicked(Double lat, Double lon) {
        WeatherDisplayLatLngFragment weatherDisplayLatLngFragment = new WeatherDisplayLatLngFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("lat", lat);
        bundle.putSerializable("lon", lon);
        weatherDisplayLatLngFragment.setArguments(bundle);
        loadFragment(weatherDisplayLatLngFragment);
    }

    private void setLocation(final Location location) {
        mCurrentLocation = location;
        Fragment frg = null;
        frg = getSupportFragmentManager().findFragmentByTag("landing page fragment");
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.detach(frg);
        ft.attach(frg);
        ft.commit();
    }

}
