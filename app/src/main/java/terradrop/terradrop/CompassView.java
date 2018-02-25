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
    private final double COMPASS_LINE_LENGTH = 0.65;

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
        centerY = (int)(getY() + getHeight() / 2.5);
        radius = Math.min(getWidth(), getHeight()) / 4;

        paint.setColor(Color.BLACK);
        canvas.drawCircle(centerX, centerY, radius, paint);

        double markerLineLength = radius * (1 - COMPASS_LINE_LENGTH);

        //Draw a line pointing north
        paint.setColor(Color.RED);
        drawCompassLine(compassAngle, markerLineLength, canvas, paint);

        //Draw lines in the cardinal directions
        paint.setColor(Color.BLACK);
        drawCompassLine(compassAngle + (Math.PI / 2), markerLineLength, canvas, paint);
        drawCompassLine(compassAngle + Math.PI, markerLineLength, canvas, paint);
        drawCompassLine(compassAngle - (Math.PI / 2), markerLineLength, canvas, paint);

        paint.setColor(Color.GREEN);

        MainActivity mainActivity = (MainActivity) getContext();

        ArrayList<Drop> closestDrops = null;
        Location currentLocation = null;

        if(mainActivity != null)
        {
            closestDrops = mainActivity.getClosestDrops();
            currentLocation = mainActivity.getCurrentLocation();
        }

        //Draw a line pointing to each nearby drop
        if(closestDrops != null && currentLocation != null)
        {
            for(Drop d : closestDrops)
            {
                //Calculate angle to drop and draw a line on the compass
                double angleToDrop = getBearing(currentLocation.getLatitude(),
                                                currentLocation.getLongitude(),
                                                d.getLatitude(),
                                                d.getLongitude());
                angleToDrop += compassAngle;

                double distanceToDrop = currentLocation.distanceTo(d.getLocation());

                double dropLineRadius = radius * ((distanceToDrop) / 150);

                drawCompassLine(angleToDrop, dropLineRadius, canvas, paint);
            }
        }
    }

    private void drawCompassLine(double angle, double length, Canvas c, Paint p)
    {
        float endX = (float)Math.cos(angle) * radius;
        float endY = (float)Math.sin(angle) * radius;

        float startX = (float)(Math.cos(angle) * (radius - length));
        float startY = (float)(Math.sin(angle) * (radius - length));

        if(Math.abs(startX) > Math.abs(endX))
        {
            startX = endX;
        }
        if(Math.abs(startY) > Math.abs(endY))
        {
            startY = endY;
        }

        c.drawLine(centerX + startX, centerY + startY, centerX + endX, centerY + endY, paint);
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
