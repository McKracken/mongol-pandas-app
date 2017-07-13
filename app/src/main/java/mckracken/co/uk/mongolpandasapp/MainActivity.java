package mckracken.co.uk.mongolpandasapp;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.tasks.OnSuccessListener;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.http.body.JSONObjectBody;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements DashboardFragment.OnDashboardFragmentInteractionListener, EventFragment.OnEventFragmentInteractionListener {

    private AsyncHttpServer server;
    private AsyncServer mAsyncServer = new AsyncServer();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_dashboard:
                    DashboardFragment dashboardFragment = DashboardFragment.newInstance();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content, dashboardFragment, "dashboard_fragment").commit();
                    return true;
                case R.id.navigation_event:
                    EventFragment eventFragment = EventFragment.newInstance();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content, eventFragment, "event_fragment").commit();
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

    public FusedLocationProviderClient getLocationClient(){
        return mFusedLocationClient;
    }

    @Override
    protected void onResume(){
        super.onResume();

        if(server == null) {
            server = new AsyncHttpServer();
            startServer();
            Log.i("SERVER", "Server started");
        }
    }

    public BottomNavigationView getNavigation(){
        return navigation;
    }

    private void startServer() {
        server.get("/getgps", new HttpServerRequestCallback() {
            @Override
            public void onRequest(final AsyncHttpServerRequest request, final AsyncHttpServerResponse response) {
                Log.d("Server", "Ready to serve");

                    // I should probably make a class that manages the server
                    // and has this two fields as attributes, at least
                mFusedLocationClient.getLastLocation()
                            .addOnSuccessListener(new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    try {
                                        DashboardFragment dashboardFragment = (DashboardFragment) getSupportFragmentManager().findFragmentByTag("dashboard_fragment");


                                        JSONObject json = new JSONObject();
                                        if (request.getBody() instanceof JSONObjectBody) {
                                            json = ((JSONObjectBody) request.getBody()).get();
                                        } else {
                                            response.code(400);
                                            response.send("Request must be in json format");
                                        }
                                        double heat1 = json.getDouble("heat1");
                                        double heat2 = json.getDouble("heat2");

                                        // or if I can get the two textboxes from here, I can just
                                        // update the values
                                        Log.i("SERVER", String.valueOf(heat1));
                                        Log.i("SERVER", String.valueOf(heat2));
                                        if(dashboardFragment != null) {
                                            dashboardFragment.setHeatValues(heat1, heat2);
                                        }
                                        response.code(200);
                                        response.getHeaders().add("Content-type", "application/json");
                                        if (location != null) {
                                            JSONObject respObject = new JSONObject();
                                            respObject.put("latitude", location.getLatitude());
                                            respObject.put("longitude", location.getLongitude());

                                            if(location.hasAltitude()) {
                                                respObject.put("altitude", location.getAltitude());
                                            }
                                            else {
                                                respObject.put("altitude", -1.0);
                                            }

                                            if(location.hasSpeed()) {
                                                respObject.put("kph", location.getSpeed());
                                            }
                                            else {
                                                respObject.put("kph", -1.0);
                                            }

                                            response.send(respObject);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        response.code(500);
                                        response.send(e.getMessage());
                                    }
                                }
                            });

                // others exception could be caught

            }
        });
        server.get("/ack", new HttpServerRequestCallback() {
                    @Override
                    public void onRequest(final AsyncHttpServerRequest request, final AsyncHttpServerResponse response) {
                        response.code(200);
                        response.send("OK");
                    }
        });
        server.listen(mAsyncServer, 8080);
    }

    /*public void disableNavigation(){
        findViewById(R.id.navigation).setEnabled(false);
    }

    public void enableNavigation(){
        findViewById(R.id.navigation).setEnabled(true);
    }*/

    @Override
    protected void onDestroy(){
        super.onDestroy();
        server.stop();
        Log.i("SERVER", "Server stopped");
    }

    @Override
    public void onEventMessageSent() {
        navigation.setSelectedItemId(R.id.navigation_dashboard);
    }

    @Override
    public void onDashboardFragmentInteraction(Uri uri) {

    }

    @Override
    public void onBackPressed(){

    }
}
