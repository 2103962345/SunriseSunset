package com.example.alex.sunrisesunset;

import android.app.Activity;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.Response;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;
import org.json.JSONException;

public class SunriseSunsetAPI {
    private String baseURL = "https://api.sunrise-sunset.org/json?";
    private static final String TAG = "SunriseSunsetAPI";
    private String requestURL;
    private double lat;
    private double lng;
    private RequestQueue requestQueue;

    private String sunrise;
    private String sunset;

    private void setSunInformation(String sunrise, String sunset){
        this.sunrise = sunrise;
        this.sunset = sunset;
    }

    public String getRequestURL() {
        return requestURL;
    }

    public void setRequestURL(String requestURL) {
        this.requestURL = requestURL;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    private void setRequestURL(){
        requestURL = baseURL+"lat="+lat+"&lng="+lng+"&date=today";
    }

    public void getRepoList(Activity activity) {

        setRequestURL();

        RequestQueue requestQueue = Volley.newRequestQueue(activity);

        // Initialize a new JsonArrayRequest instance
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                requestURL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Do something with response
                        // Process the JSON
                        try{
                            JSONObject sunInfo = response.getJSONObject("results");

                            // Get the current (json object) data
                            String sunrise = sunInfo.getString("sunrise");
                            String sunset = sunInfo.getString("sunset");

                            setSunInformation(sunrise, sunset);

                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        // Do something when error occurred
                    }
                }
        );

        // Add JsonObjectRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    public String getSunrise() {
        return sunrise;
    }

    public String getSunset() {
        return sunset;
    }
}
