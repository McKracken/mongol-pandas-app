package mckracken.co.uk.mongolpandasapp;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.http.body.JSONObjectBody;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements DashboardFragment.OnDashboardFragmentInteractionListener, EventFragment.OnEventFragmentInteractionListener {

    private AsyncHttpServer server = new AsyncHttpServer();
    private AsyncServer mAsyncServer = new AsyncServer();

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

        startServer();
    }

    private void startServer() {
        server.get("/getgps", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                Log.d("Server","Ready to serve");
                JSONObject json = new JSONObject();
                if (request.getBody() instanceof JSONObjectBody) {
                    json = ((JSONObjectBody)request.getBody()).get();
                }
                else {
                    response.code(400);
                    response.send("Request must be in json format");
                }
                try {
                    // I should probably make a class that manages the server
                    // and has this two fields as attributes, at least
                    double heat1 = json.getDouble("heat1");
                    double heat2 = json.getDouble("heat2");

                    // or if I can get the two textboxes from here, I can just
                    // update the values
                    Log.i("SERVER",String.valueOf(heat1));
                    Log.i("SERVER",String.valueOf(heat2));

                    response.code(200);
                    response.getHeaders().add("Content-type","application/json");
                    JSONObject respObject = new JSONObject();

                    // hardcoded coordinates
                    respObject.put("latitude",10.039495);
                    respObject.put("longitude", -0.039495);
                    response.send(respObject);

                } catch (JSONException e) {
                    e.printStackTrace();
                    response.code(500);
                    response.send(e.getMessage());
                }
                // others exception could be caught

            }
        });
        server.listen(mAsyncServer, 8080);
    }

    @Override
    public void onEventFragmentInteraction(Uri uri) {

    }

    @Override
    public void onDashboardFragmentInteraction(Uri uri) {

    }
}
