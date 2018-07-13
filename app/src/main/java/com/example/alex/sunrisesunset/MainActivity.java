package com.example.alex.sunrisesunset;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements OnConnectionFailedListener {

    private static final String TAG = "SunriseSunset";
    private SunriseSunsetAPI sunriseSunsetAPI;
    private GoogleApiClient mGoogleApiClient;
    private TextView sunriseTextView;
    private TextView sunsetTextView;
    public void placeFinder() {
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                .build();

        autocompleteFragment.setFilter(typeFilter);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, "Place: " + place.getName());
                setSunInfo(place);
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    private void setSunInfo(Place place) {
        sunriseSunsetAPI.setLat(place.getLatLng().latitude);
        sunriseSunsetAPI.setLng(place.getLatLng().longitude);
        sunriseSunsetAPI.getRepoList(this);

        Timer myTimer = new Timer();
        final Handler uiHandler = new Handler();

        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        sunriseTextView.setText(sunriseSunsetAPI.getSunrise());
                        sunsetTextView.setText(sunriseSunsetAPI.getSunset());
                    }
                });
            }
        }, 10);
    }

    public void setCurrentAddress(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "No permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        final PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(mGoogleApiClient, null);

       final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setCancelable(false); // set cancelable to false
        progressDialog.setMessage("Please, wait"); // set message
        progressDialog.show(); // show progress dialog

        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer placeLikelihoods) {
                ((EditText)findViewById(R.id.place_autocomplete_search_input)).setText(placeLikelihoods.get(0).getPlace().getAddress());
                setSunInfo(placeLikelihoods.get(0).getPlace());
                placeLikelihoods.release();
            }
        });
        progressDialog.dismiss();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sunriseTextView=(TextView) findViewById(R.id.sunriseText);
        sunsetTextView = (TextView) findViewById(R.id.sunsetText);
        sunriseSunsetAPI = new SunriseSunsetAPI();
        isWifiEnabled();
        isLocationEnabled();

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
        mGoogleApiClient.connect();


        setCurrentAddress();
        placeFinder();

        ((ImageButton)findViewById(R.id.currentLocationButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isWifiEnabled();
                isLocationEnabled();
                setCurrentAddress();
            }
        });

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed!");
    }

    public void isLocationEnabled(){
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled=false;
        boolean network_enabled=false;
        try{
            gps_enabled=lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch (Exception ex){}
        try{
            network_enabled=lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }catch(Exception ex){}
        if(!gps_enabled && !network_enabled){
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage(getResources().getString(R.string.gps_network_not_enabled));
            dialog.setPositiveButton(getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int paramInt) {
                    Intent myIntent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            });
            dialog.show();
        }
    }
    public void isWifiEnabled(){
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(!wifi.isWifiEnabled()) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage(getResources().getString(R.string.wifi_not_enabled));
            dialog.setPositiveButton(getResources().getString(R.string.open_wifi_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int paramInt) {
                    Intent myIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    startActivity(myIntent);
                }
            });
            dialog.show();
        }
    }


}
