package mckracken.co.uk.mongolpandasapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DashboardFragment extends Fragment {

    private OnDashboardFragmentInteractionListener mListener;
    private Button recordVideoButton;
    //private TextView consoleTextView;
    private EditText ipEditText;
    private Button takePictureButton;
    private TextView heat1TextView;
    private TextView heat2TextView;
    private Button ipSetButton;

    //private BottomNavigationView navigationView;

    private String ipAddress;

    public DashboardFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static DashboardFragment newInstance() {
        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        ipAddress = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("ip", "");

        //consoleTextView = (TextView) view.findViewById(R.id.console_text);
        ipEditText = (EditText) view.findViewById(R.id.ip_text);
        ipEditText.setText(ipAddress);
        //navigationView = ((MainActivity)getActivity()).getNavigation();
        takePictureButton = (Button) view.findViewById(R.id.take_picture);
        recordVideoButton = (Button) view.findViewById(R.id.start_recording_button);
        heat1TextView = (TextView) view.findViewById(R.id.heat1);
        heat2TextView = (TextView) view.findViewById(R.id.heat2);
        recordVideoButton.setOnClickListener(startRecordingListener());
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });
        ipSetButton = (Button) view.findViewById(R.id.ip_set_button);
        ipSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("ip", ipAddress);
                editor.apply();
                Toast.makeText(getActivity(), ipAddress + " saved", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    public View.OnClickListener startRecordingListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRecording();
            }
        };
    }

    public View.OnClickListener stopRecordingListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRecording();
            }
        };
    }

    // TODO s
    // 1) if IP is missing, we need to prevent everything
    // 2) until stop recording is ok, take picture has to be disabled
    // 3) every request need to have a timeout
    // 4) keyboard should disappear when I click somewhere else?
    // 5) MAYBE, we can add a reset button which reset the camera
    // 6) We might want to make the console be a Toast

    public void startRecording(){
        recordVideoButton.setEnabled(false);
        takePictureButton.setEnabled(false);
        //disableNavigation();
        String url = "http://" + ipAddress + ":5001/startvideo";
        // make HTTP request
        final OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, "{}");


        final Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        AsyncTask<Void, Void, Integer> asyncTask = new AsyncTask<Void, Void, Integer>() {

            @Override
            protected Integer doInBackground(Void... voids) {
                try{
                    Response response = client.newCall(request).execute();
                    return response.code();
                }catch (IOException e){
                    return 0;
                }

            }

            @Override
            protected void onPostExecute(Integer statusCode) {
                super.onPostExecute(statusCode);
                //consoleTextView.setText(String.valueOf(statusCode));
                recordVideoButton.setEnabled(true);
                //enableNavigation();

                if(statusCode == 200){
                    recordVideoButton.setText(R.string.stop_recording);
                    recordVideoButton.setOnClickListener(stopRecordingListener());
                    Toast.makeText(getActivity(), "Video started", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getActivity(), "Error: " + statusCode, Toast.LENGTH_SHORT).show();
                    takePictureButton.setEnabled(true);
                }
            }
        };
        asyncTask.execute();
    }

    /*private void disableNavigation(){
        ((MainActivity)getActivity()).disableNavigation();
    }

    private void enableNavigation(){
        ((MainActivity)getActivity()).enableNavigation();
    }*/

    public void stopRecording(){
        recordVideoButton.setEnabled(false);
        String url = "http://" + ipAddress + ":5001/stopvideo";
        // make HTTP request
        final OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, "{}");


        final Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        AsyncTask<Void, Void, Integer> asyncTask = new AsyncTask<Void, Void, Integer>() {

            @Override
            protected Integer doInBackground(Void... voids) {
                try{
                    Response response = client.newCall(request).execute();
                    return response.code();
                }catch (IOException e){
                    return 0;
                }

            }

            @Override
            protected void onPostExecute(Integer statusCode) {
                super.onPostExecute(statusCode);
                //consoleTextView.setText(String.valueOf(statusCode));
                recordVideoButton.setEnabled(true);
                takePictureButton.setEnabled(true);
                if(statusCode == 200){
                    Toast.makeText(getActivity(), R.string.video_taken, Toast.LENGTH_SHORT).show();
                    recordVideoButton.setText(R.string.start_recording);
                    recordVideoButton.setOnClickListener(startRecordingListener());

                }
            }
        };
        asyncTask.execute();
    }

    public void setHeatValues(Double heat1, Double heat2){
        heat1TextView.setText(String.valueOf(heat1));
        heat2TextView.setText(String.valueOf(heat2));
    }

    public void takePicture(){
        recordVideoButton.setEnabled(false);
        String url = "http://" + ipAddress + ":5001/shootpicture";
        // make HTTP request
        final OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, "{}");


        final Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        AsyncTask<Void, Void, Integer> asyncTask = new AsyncTask<Void, Void, Integer>() {

            @Override
            protected Integer doInBackground(Void... voids) {
                try{
                    Response response = client.newCall(request).execute();
                    return response.code();
                }catch (IOException e){
                    return 0;
                }

            }

            @Override
            protected void onPostExecute(Integer statusCode) {
                super.onPostExecute(statusCode);
                //consoleTextView.setText(String.valueOf(statusCode));
                recordVideoButton.setEnabled(true);
                if(statusCode == 200){
                    Toast.makeText(getActivity(), R.string.picture_taken, Toast.LENGTH_SHORT).show();
                }
            }
        };
        asyncTask.execute();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDashboardFragmentInteractionListener) {
            mListener = (OnDashboardFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnEventFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface OnDashboardFragmentInteractionListener {
        // TODO: Update argument type and name
        void onDashboardFragmentInteraction(Uri uri);
    }
}
