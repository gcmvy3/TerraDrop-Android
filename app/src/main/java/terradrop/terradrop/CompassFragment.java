package terradrop.terradrop;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import static android.content.Context.SENSOR_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CompassFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CompassFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CompassFragment extends Fragment implements SensorEventListener
{
    private OnFragmentInteractionListener mListener;

    private double compassAngle;

    private View view;
    private TextView compassValueView;
    private TextView dropsNearbyView;
    private CompassView compassView;

    private Button placeDropButton;

    private Location currentLocation;
    private ArrayList<Drop> nearbyDrops;

    //For reading compass value
    private SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;

    float[] mGravity;
    float[] mGeomagnetic;

    public CompassFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CompassFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CompassFragment newInstance()
    {
        CompassFragment fragment = new CompassFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        //Set app title to the name of this fragment
        if (mListener != null)
        {
            mListener.onFragmentInteraction(getString(R.string.title_compass));
        }

        View view = inflater.inflate(R.layout.fragment_compass, container, false);
        this.view = view;

        compassView = (CompassView) view.findViewById(R.id.compassView);
        compassValueView = (TextView) view.findViewById(R.id.compassValue);
        dropsNearbyView = (TextView) view.findViewById(R.id.dropsNearby);
        placeDropButton = (Button) view.findViewById(R.id.dropButton);
        placeDropButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(currentLocation == null)
                {
                    //Display error dialog there is no location data
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder
                            .setTitle("No location!")
                            .setMessage("Could not find phone location. Please wait for GPS to initialize.")
                            .setCancelable(true);
                    //Create and show alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
                else
                {
                    //Display error dialog if we cannot create a drop
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder
                            .setTitle("GPS error!")
                            .setMessage("Location not accurate enough to place drop")
                            .setCancelable(true);
                    //Create and show alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            }
        });

        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        // Reactivate orientation listeners
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        // stop the orientation listeners to save battery
        mSensorManager.unregisterListener(this);
    }


    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener)
        {
            //Init sensor manager
            mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
            accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

            mListener = (OnFragmentInteractionListener) context;
        } else
        {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = sensorEvent.values;
        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = sensorEvent.values;
        if (mGravity != null && mGeomagnetic != null)
        {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success)
            {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                float azimut = orientation[0]; // orientation contains: azimut, pitch and roll
                compassAngle = -azimut - (Math.PI / 2); //Phone rotation in radians
            }
        }

        if(compassValueView != null)
        {
            compassValueView.setText("" + compassAngle);
        }
        if(compassView != null)
        {
            compassView.setCompassAngle(compassAngle);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i)
    {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener
    {
        void onFragmentInteraction(String title);
    }

    public void updateDrops(int numDrops)
    {
        if(numDrops  == 1)
        {
            dropsNearbyView.setText("There is 1 drop nearby");
        }
        else
        {
            dropsNearbyView.setText("There are " + numDrops + " drops nearby");
        }

        compassView.draw();
    }
}
