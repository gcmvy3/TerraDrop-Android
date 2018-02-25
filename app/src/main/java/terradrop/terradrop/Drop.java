package terradrop.terradrop;

import android.graphics.Color;
import android.location.Location;

/**
 * Created by grant_000 on 2/25/2018.
 */

public class Drop
{
    private int id;
    private Location location;
    int color;

    public Drop(int id, double latitude, double longitude, String col)
    {
        location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        this.color = Color.parseColor(col);
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

    public int getColor()
    {
        return color;
    }

    public int getId()
    {
        return id;
    }
}
