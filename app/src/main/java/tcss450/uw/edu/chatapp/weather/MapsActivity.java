package tcss450.uw.edu.chatapp.weather;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import tcss450.uw.edu.chatapp.R;

/**
 * This class is the activity that handles the Google Map view for choosing a new location to
 * display weather.
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        WeatherDisplayLatLngFragment.OnWeatherDisplayLatLngFragmentInteractionListener {

    private GoogleMap mMap;
    private Location mCurrentLocation;

    /**
     * This method creates the activity, getting the current location passed from intent and
     * readying the map.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mCurrentLocation = getIntent().getParcelableExtra("LOCATION");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in the current device location and move the camera
        LatLng current = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(current).title("Current Location"));
        //Zoom levels are from 2.0f (zoomed out) to 21.f (zoomed in)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15.0f));

        mMap.setOnMapClickListener(this);
    }

    /**
     * Listener for when the user clicks the map, prompting the user if they would like to change
     * the weather location to the latitude and longitude of the area that was clicked.
     *
     * @param latLng
     */
    @Override
    public void onMapClick(LatLng latLng) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.map);
        if (!(currentFragment instanceof WeatherDisplayLatLngFragment)) {
            Log.d("LAT/LONG", latLng.toString());

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("New Marker"));

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18.0f));

            View v = findViewById(android.R.id.content);

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(v.getContext());
            alertDialog.setTitle("Change location?");
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);

            alertDialog.setPositiveButton("Select",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            WeatherDisplayLatLngFragment weatherDisplayLatLngFragment = new WeatherDisplayLatLngFragment();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("lat", latLng.latitude);
                            bundle.putSerializable("lon", latLng.longitude);
                            weatherDisplayLatLngFragment.setArguments(bundle);
                            FragmentTransaction transaction = getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.map, weatherDisplayLatLngFragment)
                                    .addToBackStack(null);
                            transaction.commit();
                        }
                    });
            alertDialog.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            alertDialog.show();
        }
    }

    /**
     * Method to handle the "Save Location" button being pressed from the display weather fragment.
     * Saves the name of the city, country to shared preferences to be displayed in the saved
     * locations page.
     *
     * @param cityString
     */
    @Override
    public void onSaveLocationButtonClicked(String cityString) {
        SharedPreferences prefs =
                getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
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
}
