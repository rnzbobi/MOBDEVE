package com.mobdeve.s17.mobdeve.animoquest.project.view;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mobdeve.s17.mobdeve.animoquest.project.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MainActivity";
    private static final String API_KEY = "AIzaSyBj61zucmzuoczsR7JbHmYPhdmRPDPndbU";
    private GoogleMap gMap;
    private EditText etDestination;
    private Button btnGetDirections;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Existing icon setup
        ImageView mapIcon = findViewById(R.id.map_icon);
        mapIcon.setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.id_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "Error - Map Fragment was null!");
        }

        // New feature: Input and button for searching directions
        etDestination = findViewById(R.id.et_destination);
        btnGetDirections = findViewById(R.id.btn_get_directions);

        btnGetDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String destination = etDestination.getText().toString().trim();
                if (!destination.isEmpty()) {
                    LatLng origin = new LatLng(14.5647, 120.99313); // DLSU Coordinates
                    getDirections(origin, destination);
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a destination", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set up existing icon click listeners
        setupIconClickListeners();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;

        try {
            boolean success = gMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        // Set initial camera position (DLSU)
        LatLng initialPosition = new LatLng(14.5647, 120.99313);
        float initialZoomLevel = 19f; // Initial zoom level
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, initialZoomLevel));

        // Set zoom limits
        gMap.setMinZoomPreference(18.5f); // Minimum zoom level
        gMap.setMaxZoomPreference(21f); // Maximum zoom level
    }


    private void getDirections(LatLng origin, String destination) {
        OkHttpClient client = new OkHttpClient();

        String url = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=" + origin.latitude + "," + origin.longitude +
                "&destination=" + destination +
                "&mode=walking" +
                "&alternatives=true" +
                "&key=" + API_KEY;

        Log.d(TAG, "Directions API URL: " + url);

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to get directions", e);
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to get directions", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseData = response.body().string();
                Log.d(TAG, "API Response: " + responseData);

                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        String status = jsonResponse.getString("status");
                        Log.d(TAG, "API Status: " + status);

                        if ("OK".equals(status)) {
                            JSONArray routes = jsonResponse.getJSONArray("routes");
                            if (routes.length() > 0) {
                                // Choose the route with the shortest distance
                                JSONObject shortestRoute = null;
                                int shortestDistance = Integer.MAX_VALUE;
                                for (int i = 0; i < routes.length(); i++) {
                                    JSONObject route = routes.getJSONObject(i);
                                    JSONArray legs = route.getJSONArray("legs");
                                    JSONObject leg = legs.getJSONObject(0);
                                    int distance = leg.getJSONObject("distance").getInt("value");
                                    if (distance < shortestDistance) {
                                        shortestDistance = distance;
                                        shortestRoute = route;
                                    }
                                }

                                if (shortestRoute != null) {
                                    JSONObject overviewPolyline = shortestRoute.getJSONObject("overview_polyline");
                                    String encodedPath = overviewPolyline.getString("points");

                                    List<LatLng> decodedPath = decodePolyline(encodedPath);

                                    runOnUiThread(() -> drawRouteOnMap(decodedPath, origin, destination));
                                } else {
                                    Log.d(TAG, "No suitable route found");
                                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "No suitable route found", Toast.LENGTH_SHORT).show());
                                }
                            } else {
                                Log.d(TAG, "No routes found in the response");
                                runOnUiThread(() -> Toast.makeText(MainActivity.this, "No route found", Toast.LENGTH_SHORT).show());
                            }
                        } else {
                            Log.e(TAG, "API returned non-OK status: " + status);
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error: " + status, Toast.LENGTH_SHORT).show());
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing JSON response", e);
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error processing directions", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Log.e(TAG, "Unsuccessful response: " + response.code() + " " + response.message());
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error getting directions", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void drawRouteOnMap(List<LatLng> path, LatLng origin, String destination) {
        runOnUiThread(() -> {
            gMap.clear();
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(path)
                    .width(10)
                    .color(Color.rgb(255, 165, 0)); // Orange color

            gMap.addPolyline(polylineOptions);

            gMap.addMarker(new MarkerOptions().position(origin).title("Start"));
            gMap.addMarker(new MarkerOptions().position(path.get(path.size() - 1)).title(destination));

            LatLng southwest = new LatLng(
                    Math.min(origin.latitude, path.get(path.size() - 1).latitude),
                    Math.min(origin.longitude, path.get(path.size() - 1).longitude)
            );
            LatLng northeast = new LatLng(
                    Math.max(origin.latitude, path.get(path.size() - 1).latitude),
                    Math.max(origin.longitude, path.get(path.size() - 1).longitude)
            );

            gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                    new com.google.android.gms.maps.model.LatLngBounds(southwest, northeast), 100));
        });
    }

    // Function to decode polyline points
    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    private void setupIconClickListeners() {
        // Set up existing icon click listeners
        ImageView indoorIcon = findViewById(R.id.indoor_icon);
        indoorIcon.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, IndoorNavigationActivity.class)));

        ImageView elevatorIcon = findViewById(R.id.elevator_icon);
        elevatorIcon.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ElevatorActivity.class)));

        ImageView notificationIcon = findViewById(R.id.notification_icon);
        notificationIcon.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, NotificationActivity.class)));

        ImageView profileIcon = findViewById(R.id.profile_icon);
        profileIcon.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
