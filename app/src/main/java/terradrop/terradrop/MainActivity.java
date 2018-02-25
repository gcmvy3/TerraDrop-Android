package terradrop.terradrop;

import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity implements FoundDropsFragment.OnFragmentInteractionListener,
                                                                CompassFragment.OnFragmentInteractionListener,
                                                                ProfileFragment.OnFragmentInteractionListener
{
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    private boolean mRequestingLocationUpdates = false;

    private double currentLatitude = 0;
    private double currentLongitude = 0;

    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //Remove text from navigation tabs
        removeTextLabel(navigation, R.id.navigation_compass);
        removeTextLabel(navigation, R.id.navigation_profile);
        removeTextLabel(navigation, R.id.navigation_foundDrops);

        //Open the compass fragment by default
        navigation.setSelectedItemId(R.id.navigation_compass);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //Start tracking location
        mLocationCallback = new LocationCallback()
        {
            @Override
            public void onLocationResult(LocationResult locationResult)
            {
                for (Location location : locationResult.getLocations())
                {
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();

                    if(currentFragment instanceof CompassFragment)
                    {
                        TextView longitudeView = (TextView) currentFragment.getView().findViewById(R.id.longitudeValue);
                        longitudeView.setText("" + currentLongitude);

                        TextView latitudeView = (TextView) currentFragment.getView().findViewById(R.id.latitudeValue);
                        latitudeView.setText("" + currentLatitude);
                    }
                }
            };
        };

        Log.d("[DEBUG]", "So far so good!");

        createLocationRequest();
    }

    protected void createLocationRequest()
    {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task;
        task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>()
        {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse)
            {
                mRequestingLocationUpdates = true;
                startLocationUpdates();
                Log.d("[DEBUG]", "Successfully started location services");
            }
        });

        task.addOnFailureListener(this, new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Log.d("[DEBUG]", "Failed to start location services");

                if (e instanceof ResolvableApiException)
                {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try
                    {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    }
                    catch (IntentSender.SendIntentException sendEx)
                    {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (mRequestingLocationUpdates)
        {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates()
    {
        try
        {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null /* Looper */);

            Log.d("[DEBUG]", "Started tracking location!");
        }
        catch (SecurityException e)
        {
            //If we don't have permission to track location, display a warning
            showLocationError();
        }
    }

    private void showLocationError()
    {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this.getApplicationContext());
        builder1.setMessage(R.string.location_error_text);
        builder1.setTitle(getString(R.string.location_error_title));

        AlertDialog locationAlert = builder1.create();
        locationAlert.show();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener()
    {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item)
        {
            switch (item.getItemId())
            {
                case R.id.navigation_foundDrops:
                    currentFragment = FoundDropsFragment.newInstance();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, currentFragment).commit();
                    return true;
                case R.id.navigation_compass:
                    currentFragment = CompassFragment.newInstance();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, currentFragment).commit();
                    return true;
                case R.id.navigation_profile:
                    currentFragment = ProfileFragment.newInstance();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, currentFragment).commit();
                    return true;
            }
            return false;
        }
    };

    //Remove text from the tabs at the bottom of the screen
    private void removeTextLabel(@NonNull BottomNavigationView bottomNavigationView, int menuItemId)
    {
        View view = bottomNavigationView.findViewById(menuItemId);
        if (view == null) return;
        if (view instanceof MenuView.ItemView)
        {
            ViewGroup viewGroup = (ViewGroup) view;
            int padding = 0;
            for (int i = 0; i < viewGroup.getChildCount(); i++)
            {
                View v = viewGroup.getChildAt(i);
                if (v instanceof ViewGroup)
                {
                    padding = v.getHeight();
                    viewGroup.removeViewAt(i);
                }
            }
            viewGroup.setPadding(view.getPaddingLeft(), (viewGroup.getPaddingTop() + padding) / 2, view.getPaddingRight(), view.getPaddingBottom());
        }
    }

    @Override
    public void onFragmentInteraction(String title)
    {
        if(getSupportActionBar() != null)
        {
            getSupportActionBar().setTitle(title);
        }
    }
}
