package terradrop.terradrop;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by grant_000 on 2/24/2018.
 */

public class CompassView extends View
{
    private final int CIRCLE_THICKNESS = 8;

    private float centerX;
    private float centerY;
    private float radius;

    private double compassAngle;

    private Paint paint;

    public CompassView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        TypedArray a = getContext().obtainStyledAttributes(
                attrs,
                R.styleable.CompassView);


        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(CIRCLE_THICKNESS);
    }

    public void setCompassAngle(double angle)
    {
        compassAngle = angle;
        draw();
    }

    public void draw()
    {
        invalidate();
        requestLayout();
    }

    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        centerX = getX() + getWidth()  / 2;
        centerY = getY() + getHeight() / 2;
        radius = Math.min(getWidth(), getHeight()) / 2 - (CIRCLE_THICKNESS * 2);

        paint.setColor(Color.BLACK);
        canvas.drawCircle(centerX, centerY, radius, paint);

        paint.setColor(Color.RED);

        double northX = Math.cos(compassAngle) * radius;
        double northY = Math.sin(compassAngle) * radius;

        canvas.drawLine(centerX, centerY, centerX + (float)northX, centerY + (float)northY, paint);

        paint.setColor(Color.GREEN);

        MainActivity mainActivity = (MainActivity) getContext();

        ArrayList<Drop> closestDrops = null;
        Location currentLocation = null;

        if(mainActivity != null)
        {
            closestDrops = mainActivity.getClosestDrops();
            currentLocation = mainActivity.getCurrentLocation();
        }

        if(closestDrops != null && currentLocation != null)
        {
            Log.d("[DEBUG]", "Started drawing lines!");
            for(Drop d : closestDrops)
            {
                //Calculate angle to drop and draw a line on the compass
                double angleToDrop = getBearing(currentLocation.getLatitude(),
                                                currentLocation.getLongitude(),
                                                d.getLatitude(),
                                                d.getLongitude());
                angleToDrop += compassAngle;

                double dropX = Math.cos(angleToDrop) * radius;
                double dropY = Math.sin(angleToDrop) * radius;

                canvas.drawLine(centerX, centerY, centerX + (float)dropX, centerY + (float)dropY, paint);
                Log.d("[DEBUG]", "Drawing drop line!");
            }
        }
        if(closestDrops == null)
        {
            //Log.d("[DEBUG]", "Null drops!");
        }
        if(currentLocation == null)
        {
            //Log.d("[DEBUG]", "Null location!");
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private double getBearing(double startLat, double startLng, double endLat, double endLng)
    {
        double longitude1 = startLng;
        double longitude2 = endLng;
        double latitude1 = Math.toRadians(startLat);
        double latitude2 = Math.toRadians(endLat);
        double longDiff = Math.toRadians(longitude2-longitude1);
        double y= Math.sin(longDiff)*Math.cos(latitude2);
        double x=Math.cos(latitude1)*Math.sin(latitude2)-Math.sin(latitude1)*Math.cos(latitude2)*Math.cos(longDiff);
        return Math.atan2(y, x);
    }
}
