package com.example.eartquakewatcher.Activities;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.eartquakewatcher.Data.Earthquake;
import com.example.eartquakewatcher.R;
import com.example.eartquakewatcher.UI.CustomInfoWindow;
import com.example.eartquakewatcher.Util.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private RequestQueue queue;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private BitmapDescriptor iconcolors[];
    private Button showListbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        showListbtn=(Button) findViewById(R.id.showListBtn);
        showListbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapsActivity.this,QuakesList.class));
            }
        });
        iconcolors=new BitmapDescriptor[]{
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)

        };
        queue= Volley.newRequestQueue(this);
        getearthquakes();
    }

    private void getearthquakes() {
        Earthquake earthquake=new Earthquake();
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET,Constants.URL,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray features=response.getJSONArray("features");
                            for(int i=0;i<Constants.LIMIT;i++){
                                JSONObject properties=features.getJSONObject(i).getJSONObject("properties");
                                //create coordinates object
                                JSONObject geometry=features.getJSONObject(i).getJSONObject("geometry");
                                //create an array of each place longitude and latitude

                                JSONArray coordinates=geometry.getJSONArray("coordinates");
                                double lon=coordinates.getDouble(0);
                                double lat=coordinates.getDouble(1);

                               // Log.d("properties", "onResponse: " + lon + ", " + lat);
                                earthquake.setLat(lat);
                                earthquake.setLon(lon);
                                earthquake.setPlace(properties.getString("place"));
                                earthquake.setMagnitude(properties.getDouble("mag"));
                                earthquake.setDetailLink(properties.getString("detail"));
                                earthquake.setTime(properties.getLong("time"));
                                earthquake.setType(properties.getString("type"));

                                java.text.DateFormat dateFormat=java.text.DateFormat.getDateInstance();
                                String formattedDate=dateFormat.format(new Date(properties.getLong("time")).getTime());

                                //todo: set marker options
                                MarkerOptions markerOptions=new MarkerOptions();
                                markerOptions.icon(iconcolors[Constants.randomInt(0,iconcolors.length)]);
                                markerOptions.title(earthquake.getPlace());
                                markerOptions.position(new LatLng(earthquake.getLat(),earthquake.getLon()));
                                markerOptions.snippet("Magnitude" + earthquake.getMagnitude()+ "\n"
                                + "Date" + formattedDate);


                                //Add circle to markers that have magnitude greater than certain range
                                if(earthquake.getMagnitude()>=2.0){
                                    CircleOptions circleOptions=new CircleOptions();
                                    circleOptions.center(new LatLng(earthquake.getLat(),earthquake.getLon()));
                                    circleOptions.radius(30000);
                                    circleOptions.strokeWidth(3.6f);
                                    circleOptions.fillColor(Color.RED);
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                    mMap.addCircle(circleOptions);
                                }

                                Marker marker=mMap.addMarker(markerOptions);
                                marker.setTag(earthquake.getDetailLink());
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lon),4));

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }


                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(jsonObjectRequest);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //todo: create our custom view for map
        mMap.setInfoWindowAdapter(new CustomInfoWindow(getApplicationContext()));
        //to see our actions on  map we need to register to the map by using following code
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }
        };
        if (Build.VERSION.SDK_INT < 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
        else{
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
            else{

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                        .title("Prayagraj"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 8));
            }


        }

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {
        //Toast.makeText(getApplicationContext(),marker.getTag().toString(),Toast.LENGTH_LONG).show();
       Log.d("tag", "onInfoWindowClick: " + marker.getTag().toString());
        getQuakeDetail(marker.getTag().toString());

    }

    private void getQuakeDetail(String url) {
        //Log.d("city", "onResponse: " + url);
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String detailsUrl="";
                try {
                    JSONObject properties=response.getJSONObject("properties");
                    JSONObject products=properties.getJSONObject("products");
                    JSONArray geoserve=products.getJSONArray("nearby-cities");

                    for(int i=0;i<geoserve.length();i++){
                        JSONObject geoserveObj=geoserve.getJSONObject(i);
                        JSONObject contentObj=geoserveObj.getJSONObject("contents");
                        JSONObject geoJsonObj=contentObj.getJSONObject("nearby-cities.json");
                        
                        detailsUrl=geoJsonObj.getString("url");

                    }
     //               getMoreDetails(detailsUrl);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(jsonObjectRequest);
    }


//    public void getMoreDetails(String url){
//        Log.d("urls", "onResponse: " +url);
//
//        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url,null, new Response.Listener<JSONObject>() {
//
//            @Override
//            public void onResponse(JSONObject response) {
//                dialogBuilder=new AlertDialog.Builder(MapsActivity.this);
//                View view =getLayoutInflater().inflate(R.layout.popup,null);
//                Button dismissButton=(Button) view.findViewById(R.id.dismissPop);
//                Button dismisButtonTop=(Button) view.findViewById(R.id.dismissPopTop);
//                TextView popList=(TextView)view.findViewById(R.id.popList);
//                WebView webView=(WebView) view.findViewById(R.id.htmlWebview);
//
//                StringBuilder stringBuilder=new StringBuilder();
//
//
//                try {
//                    JSONArray cities=response.getJSONArray("nearby-cities");
//                    Log.d("city", "onResponse: "+ cities);
//
//                    for(int i = 0; i<cities.length(); i = i + 1){
//                        JSONObject citiesObj=cities.getJSONObject(i);
//                        stringBuilder.append("City: " + citiesObj.getString("name")
//                                            + "\n" + "Distance: " + citiesObj.getString("distance")
//                                            + "\n" + "Population: " + citiesObj.getString("population"));
//                        stringBuilder.append("\n\n");
//
//                    }
//
//                    popList.setText(stringBuilder);
//                    Log.d("info", "onResponse: " + stringBuilder);
//                    dialogBuilder.setView(view);
//                    dialog=dialogBuilder.create();
//                    dialog.show();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//
//            }
//        });
//        queue.add(jsonObjectRequest);
    //}

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        return false;
    }
}