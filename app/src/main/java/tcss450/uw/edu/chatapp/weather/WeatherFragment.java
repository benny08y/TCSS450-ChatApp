package tcss450.uw.edu.chatapp.weather;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import tcss450.uw.edu.chatapp.R;

/**
 * This class is the secondary landing page for weather functionality, providing the user with
 * several options for weather displays.
 */
public class WeatherFragment extends Fragment {

    private WeatherFragment.OnWeatherFragmentInteractionListener mListener;
    private static Button mMyCurrentLocationButton;
    private static Location mCurrentLocation;

    public WeatherFragment() {
        // Required empty public constructor
    }

    /**
     * Initializes the four buttons on the fragment and attaches listeners that communicate with the
     * activity to switch fragments.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_weather, container, false);

        mMyCurrentLocationButton = (Button) v.findViewById(R.id.weatherFragmentMyCurrentLocationButton);
        mMyCurrentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onMyCurrentLocationButtonClicked(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            }
        });
        Button b = (Button) v.findViewById(R.id.weatherFragmentZipCodeButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onZipCodeButtonClicked();
            }
        });
        b = (Button) v.findViewById(R.id.weatherFragmentMapButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onMapButtonClicked();
            }
        });
        b = (Button) v.findViewById(R.id.weatherFragmentSavedLocationsButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { mListener.onSavedLocationsButtonClicked(); }
        });

        return v;
    }

    /**
     * Ensures activities implement fragment listener.
     *
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof WeatherFragment.OnWeatherFragmentInteractionListener) {
            mListener = (WeatherFragment.OnWeatherFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnWeatherFragmentInteractionListener");
        }
    }

    /**
     * This is a helper method to help set the current location field to the device location.
     *
     * @param location
     */
    public static void setLocation(final Location location) {
        mCurrentLocation = location;
        if (mMyCurrentLocationButton != null) {
            mMyCurrentLocationButton.setText("MY CURRENT LOCATION: " + "\n" + mCurrentLocation.getLatitude() + " " + mCurrentLocation.getLongitude());
        }
    }

    /**
     * Interface for activity interaction.
     */
    public interface OnWeatherFragmentInteractionListener {

        void onMyCurrentLocationButtonClicked(Double lat, Double lon);

        void onZipCodeButtonClicked();

        void onMapButtonClicked();

        void onSavedLocationsButtonClicked();
    }
}
