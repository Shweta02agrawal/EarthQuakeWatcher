package com.example.eartquakewatcher.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.eartquakewatcher.Data.Earthquake;
import com.example.eartquakewatcher.R;
import com.example.eartquakewatcher.Util.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QuakesList extends AppCompatActivity {
    private ArrayList<String> arrayList;
    private ListView listView;
    private RequestQueue queue;
    private ArrayAdapter arrayAdapter;
    private List<Earthquake> quakelist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quakes_list);
        quakelist=new ArrayList<>();
        listView=(ListView) findViewById(R.id.listview);
        queue= Volley.newRequestQueue(this);
        arrayList=new ArrayList<>();
        getAllQuakes(Constants.URL);

    }

    public void getAllQuakes(String url){
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url,null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Earthquake earthquake=new Earthquake();
                try {
                    JSONArray features=response.getJSONArray("features");
                    for(int i = 0; i< features.length(); i++){
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

                        arrayList.add(earthquake.getPlace());
                    }

                    arrayAdapter=new ArrayAdapter<>(QuakesList.this, android.R.layout.simple_list_item_1,
                            android.R.id.text1,arrayList);
                    listView.setAdapter(arrayAdapter);
                    arrayAdapter.notifyDataSetChanged();

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
}