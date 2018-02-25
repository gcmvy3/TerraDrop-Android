package terradrop.terradrop;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LocationListener,
                                                                FoundDropsFragment.OnFragmentInteractionListener,
                                                                CompassFragment.OnFragmentInteractionListener,
                                                                ProfileFragment.OnFragmentInteractionListener
{
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    private LocationManager locationManager;

    private Location currentLocation;
    private double currentLatitude = 0;
    private double currentLongitude = 0;

    private Fragment currentFragment;

    private RequestQueue requestQueue;

    private String serverIP = "http://192.168.1.125:8080";
    private String getDropsRequest = serverIP + "/getDrops";

    ArrayList<Drop> closestDrops;

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

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000,
                    0, this);
        }
        catch(SecurityException e)
        {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        closestDrops = new ArrayList<Drop>();
        requestQueue = Volley.newRequestQueue(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
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

    private void requestDrops()
    {
        JsonArrayRequest arrayRequest = new JsonArrayRequest(getDropsRequest, new Response.Listener<JSONArray>()
        {
            @Override
            public void onResponse(JSONArray response)
            {
                closestDrops = new ArrayList<Drop>();

                try
                {
                    //Read in drops sent from the server
                    //Add them to an arraylist

                    for (int i = 0; i < response.length(); i++)
                    {
                        JSONObject jo = response.getJSONObject(i);

                        Log.d("[SERVER RESPONSE]", jo.toString());

                        int dropID = jo.getInt("id");
                        double latitude = jo.getDouble("latitude");
                        double longitude = jo.getDouble("longitude");

                        Drop newDrop = new Drop(dropID, latitude, longitude);
                        closestDrops.add(newDrop);
                    }

                    updateCompass();
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                if(error != null)
                {
                    Log.e("[SERVER ERROR]", "" + error.getMessage());
                }
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(arrayRequest);
        Log.d("[SERVER]", "Querying " + getDropsRequest);
    }

    private void updateCompass()
    {
        if(currentFragment != null && currentFragment instanceof CompassFragment)
        {
            CompassFragment compassFrag = (CompassFragment) currentFragment;
            compassFrag.updateDrops(closestDrops, currentLocation);
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

    @Override
    public void onLocationChanged(Location location)
    {
        setCurrentLocation(location);

        updateCompass();

        requestDrops();

        if(currentFragment != null && currentFragment instanceof CompassFragment)
        {
            TextView longitudeView = (TextView) currentFragment.getView().findViewById(R.id.longitudeValue);
            longitudeView.setText("" + currentLongitude);

            TextView latitudeView = (TextView) currentFragment.getView().findViewById(R.id.latitudeValue);
            latitudeView.setText("" + currentLatitude);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle)
    {

    }

    @Override
    public void onProviderEnabled(String s)
    {
        Toast.makeText(getBaseContext(), "Gps is turned on!! ",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String s)
    {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
        Toast.makeText(getBaseContext(), "Gps is turned off!! ",
                Toast.LENGTH_SHORT).show();
    }

    private void setCurrentLocation(Location l)
    {
        if(l != null)
        {
            currentLocation = l;
            currentLatitude = l.getLatitude();
            currentLongitude = l.getLongitude();
        }
    }
}
