package mckracken.co.uk.mongolpandasapp;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements DashboardFragment.OnDashboardFragmentInteractionListener, EventFragment.OnEventFragmentInteractionListener {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_dashboard:
                    DashboardFragment dashboardFragment = DashboardFragment.newInstance();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content, dashboardFragment).commit();
                    return true;
                case R.id.navigation_event:
                    EventFragment eventFragment = EventFragment.newInstance();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content, eventFragment).commit();
                    return true;
            }
            return false;
        }

    };

    private FrameLayout frameLayout;
    private BottomNavigationView navigation;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_dashboard);
        frameLayout = (FrameLayout) findViewById(R.id.content);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    }

    @Override
    public void onEventFragmentInteraction(Uri uri) {

    }

    @Override
    public void onDashboardFragmentInteraction(Uri uri) {

    }
}
