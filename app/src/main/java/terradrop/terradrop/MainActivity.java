package terradrop.terradrop;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements FoundDropsFragment.OnFragmentInteractionListener,
                                                                CompassFragment.OnFragmentInteractionListener,
                                                                ProfileFragment.OnFragmentInteractionListener
{
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
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener()
    {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item)
        {
            Fragment selectedFragment = null;
            switch (item.getItemId())
            {
                case R.id.navigation_foundDrops:
                    selectedFragment = FoundDropsFragment.newInstance();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, selectedFragment).commit();
                    return true;
                case R.id.navigation_compass:
                    selectedFragment = CompassFragment.newInstance();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, selectedFragment).commit();
                    return true;
                case R.id.navigation_profile:
                    selectedFragment = ProfileFragment.newInstance();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, selectedFragment).commit();
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
        getSupportActionBar().setTitle(title);
    }
}
