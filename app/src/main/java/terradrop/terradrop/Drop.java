package terradrop.terradrop;

import android.location.Location;

/**
 * Created by grant_000 on 2/25/2018.
 */

public class Drop
{
    private int id;
    private Location location;

    public Drop(int id, double latitude, double longitude)
    {
        location = new Location("");
        location.setLatitude(latitude);//your coords of course
        location.setLongitude(longitude);
    }

    public Location getLocation()
    {
        return location;
    }

    public double getLatitude()
    {
        return location.getLatitude();
    }

    public double getLongitude()
    {
        return location.getLongitude();
    }

    public int getId()
    {
        return id;
    }
}
