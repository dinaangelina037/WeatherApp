package com.dina.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityNameTV, temperatureTV, conditionTV;
    private RecyclerView weatherRV;
    private TextInputEditText cityEdt;
    private ImageView backTV, iconIV, searchIV;
    private ArrayList<WeatherRVModal> weatherRVModalsArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    private String cityName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_main);
        homeRL = findViewById(R.id.idRLHome);
        loadingPB = findViewById(R.id.idPBLoading);
        cityNameTV = findViewById(R.id.idTVCityName);
        temperatureTV = findViewById(R.id.idTVTemperature);
        conditionTV = findViewById(R.id.idTVCondition);
        weatherRV = findViewById(R.id.idRvWeather);
        cityEdt = findViewById(R.id.idEdtCity);
        backTV = findViewById(R.id.idTVBack);
        iconIV = findViewById(R.id.idTVIcon);
        searchIV = findViewById(R.id.idTVSearch);
        weatherRVModalsArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this, weatherRVModalsArrayList);
        weatherRV.setAdapter(weatherRVAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ;
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
    }

    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        cityName = getCityName(location.getLongitude(),location.getLatitude());
        getWeatherInfo(cityName);

        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = cityEdt.getText().toString();
                if (city.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter city Name", Toast.LENGTH_SHORT).show();
                } else {
                    cityNameTV.setText(cityName);
                    getWeatherInfo(city);
                }
            }
        });

}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==PERMISSION_CODE){
            if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Permissions granted..",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"Please provide the permissions",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude){
        String cityName = "Not found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try{
            List<Address> addresses = gcd.getFromLocation(latitude,longitude,10);

            for (Address adr : addresses){
                if (adr!=null){
                    String city = adr.getLocality();
                    if (city!=null && !city.equals("")){
                        cityName = city;
                    }else {
                        Log.d("TAG","CITY NOT FOUND");
                        Toast.makeText(this,"User City Not Found..", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }catch (IOException e){
            e.printStackTrace();
        }
        return cityName;
    }

    private void getWeatherInfo(String cityName){
        String url = "http://api.weatherapi.com/v1/current.json?key=9099406e5b394114ade62820210312&q="+cityName+"&days=1&aqi=yes&alerts=yes";
        cityNameTV.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);
                weatherRVModalsArrayList.clear();

                try {
                    String temperature = response.getJSONObject("current").getString("temp_c");
                    temperatureTV.setText(temperature+"");
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("text");
                   Picasso.get().load("http:".concat(conditionIcon)).into(iconIV);
                   conditionTV.setText(condition);
                   if (isDay==1){
//                       morning
                       Picasso.get().load("https://www.google.com/search?q=image+morning&sxsrf=AOaemvJQEKANid2SU4V-NIDdoDdOlmm6dw:1638536942431&tbm=isch&source=iu&ictx=1&fir=8CfIkRSXOawUjM%252CC-qhWr_Yy7Df2M%252C_%253BiYJ9ySyXiWxQsM%252CgSmZQj29WPZ8IM%252C_%253BqYYkHKpF5cTDGM%252CNIbv6oz5mmLM9M%252C_%253BcfM0wXXXHX0smM%252CC-qhWr_Yy7Df2M%252C_%253B9WjjqVto4ouuIM%252CcOKS_YYPj_Tq5M%252C_%253Btfg9uUI5rGlnEM%252CQdmqunDjhFUe1M%252C_%253B9Nlg6KpDFOErkM%252CC-qhWr_Yy7Df2M%252C_%253BcR5xFm1TmZf1zM%252CAta9SVueyjkvOM%252C_%253BCFp0CVdI-qLQuM%252Cim8soqfI8obygM%252C_%253BTmPxfjncw-NEIM%252CzLCyoppLKKXN_M%252C_%253B31QKibC-nrD60M%252C-SSiUI-Orrh7QM%252C_%253BaDzNv5v3tfOKhM%252CBI93SXHC56zkqM%252C_%253Bs9_iN-ArjwY_xM%252CK6_od1-p8jJvQM%252C_%253BpNRUyp3SHtJyHM%252C8IIUaK9yEy1ZtM%252C_%253BOOIgXo0R5bhxFM%252CC-qhWr_Yy7Df2M%252C_%253BtfGZ_6ZAJcyugM%252Cyqpi1VScON7FPM%252C_&vet=1&usg=AI4_-kRVvWlSJfz-vE9shljD2mPKf2RbgA&sa=X&ved=2ahUKEwiwn57j2cf0AhWNILcAHVzpBa0Q9QF6BAgFEAE&biw=1366&bih=625&dpr=1#imgrc=8CfIkRSXOawUjM").into(backTV);
                   }else{
                       Picasso.get().load("https://www.google.com/search?q=image+morning&sxsrf=AOaemvJQEKANid2SU4V-NIDdoDdOlmm6dw:1638536942431&tbm=isch&source=iu&ictx=1&fir=8CfIkRSXOawUjM%252CC-qhWr_Yy7Df2M%252C_%253BiYJ9ySyXiWxQsM%252CgSmZQj29WPZ8IM%252C_%253BqYYkHKpF5cTDGM%252CNIbv6oz5mmLM9M%252C_%253BcfM0wXXXHX0smM%252CC-qhWr_Yy7Df2M%252C_%253B9WjjqVto4ouuIM%252CcOKS_YYPj_Tq5M%252C_%253Btfg9uUI5rGlnEM%252CQdmqunDjhFUe1M%252C_%253B9Nlg6KpDFOErkM%252CC-qhWr_Yy7Df2M%252C_%253BcR5xFm1TmZf1zM%252CAta9SVueyjkvOM%252C_%253BCFp0CVdI-qLQuM%252Cim8soqfI8obygM%252C_%253BTmPxfjncw-NEIM%252CzLCyoppLKKXN_M%252C_%253B31QKibC-nrD60M%252C-SSiUI-Orrh7QM%252C_%253BaDzNv5v3tfOKhM%252CBI93SXHC56zkqM%252C_%253Bs9_iN-ArjwY_xM%252CK6_od1-p8jJvQM%252C_%253BpNRUyp3SHtJyHM%252C8IIUaK9yEy1ZtM%252C_%253BOOIgXo0R5bhxFM%252CC-qhWr_Yy7Df2M%252C_%253BtfGZ_6ZAJcyugM%252Cyqpi1VScON7FPM%252C_&vet=1&usg=AI4_-kRVvWlSJfz-vE9shljD2mPKf2RbgA&sa=X&ved=2ahUKEwiwn57j2cf0AhWNILcAHVzpBa0Q9QF6BAgFEAE&biw=1366&bih=625&dpr=1#imgrc=8CfIkRSXOawUjM").into(backTV);
                   }
                   JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forcastO = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray = forcastO.getJSONArray("hour");

                    for (int i=0; i<hourArray.length(); i++){
                        JSONObject hourObj = hourArray.getJSONObject(i);
                        String time = hourObj.getString("time");
                        String temper = hourObj.getString("temp_c");
                        String img = hourObj.getJSONObject("condition").getString("icon");
                        String wind= hourObj.getString("wind_kph");
                        weatherRVModalsArrayList.add(new WeatherRVModal(time,temper,img,wind));
                    }
                    weatherRVAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this,"Please enter valid cityName..",Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }
}
