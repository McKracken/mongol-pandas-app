package mckracken.co.uk.mongolpandasapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class EventFragment extends Fragment {

    private OnEventFragmentInteractionListener mListener;
    private Button sendEventButton;
    private EditText eventTitleEditText;
    private EditText eventDescriptionEditText;

    public EventFragment() {
        // Required empty public constructor
    }


    public static EventFragment newInstance() {
        EventFragment fragment = new EventFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event, container, false);
        sendEventButton = (Button) view.findViewById(R.id.send_event_button);
        sendEventButton.setOnClickListener(sendEventListener());
        eventTitleEditText = (EditText) view.findViewById(R.id.title_text);
        eventDescriptionEditText = (EditText) view.findViewById(R.id.description_text);

        return view;
    }

    public View.OnClickListener sendEventListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEvent();
            }
        };
    }

    public void sendEvent() {
        final String title = eventTitleEditText.getText().toString();
        final String description = eventDescriptionEditText.getText().toString();

        FusedLocationProviderClient locationClient = ((MainActivity)getActivity()).getLocationClient();
        // TODO I should take this from the gps provider
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        final OkHttpClient client = new OkHttpClient();
                        JSONObject toSend = new JSONObject();

                        try {
                            toSend.put("title", title);
                            toSend.put("description", description);
                            toSend.put("lat", location.getLatitude());
                            toSend.put("lon",location.getLongitude());

                            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                            RequestBody body = RequestBody.create(JSON, toSend.toString());

                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            String ip = sp.getString("ip", "");
                            // TODO where the hell do I take the url?
                            final Request request = new Request.Builder()
                                    .url("http://" + ip + ":5001/signalevent")
                                    .post(body)
                                    .build();

                            AsyncTask<Void, Void, Integer> asyncTask = new AsyncTask<Void, Void, Integer>() {

                                @Override
                                protected Integer doInBackground(Void... voids) {
                                    try {
                                        Response response = client.newCall(request).execute();
                                        return response.code();
                                    }
                                    catch (IOException e){
                                        return 0;
                                    }

                                }

                                @Override
                                protected void onPostExecute(Integer statusCode) {
                                    super.onPostExecute(statusCode);

                                    if(statusCode == 200){
                                        Toast.makeText(getActivity(), "Message sent" , Toast.LENGTH_SHORT).show();
                                        ((MainActivity)getActivity()).onEventMessageSent();
                                    }
                                    else {
                                        // TODO report something somewhere, like a Toast
                                        Toast.makeText(getActivity(), "Error" + statusCode, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            };
                            asyncTask.execute();


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });



    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnEventFragmentInteractionListener) {
            mListener = (OnEventFragmentInteractionListener) context;
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
    public interface OnEventFragmentInteractionListener {
        // TODO: Update argument type and name
        void onEventMessageSent();
    }
}
