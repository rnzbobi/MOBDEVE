package com.mobdeve.s17.mobdeve.animoquest.project.view;
import com.mobdeve.s17.mobdeve.animoquest.project.model.MarkerInfo;

import com.mobdeve.s17.mobdeve.animoquest.project.BuildConfig;
import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
    private static final String API_KEY = BuildConfig.DIRECTIONS_API_KEY;
    private GoogleMap gMap;
    private EditText etDestination;
    private Button btnGetDirections;
    private DatabaseReference markersRef;
    private String profilePictureUrl;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

       profilePictureUrl = getIntent().getStringExtra("profilePictureUrl");

        // Initialize Firebase Database reference with the correct database URL
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://animoquest-c89ff-default-rtdb.asia-southeast1.firebasedatabase.app/");
        markersRef = database.getReference("markers");


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

        btnGetDirections.setOnClickListener(v -> {
            String destination = etDestination.getText().toString().trim();
            if (!destination.isEmpty()) {
                LatLng origin = new LatLng(14.5647, 120.99313); // DLSU Coordinates
                getDirections(origin, destination);
            } else {
                Toast.makeText(MainActivity.this, "Please enter a destination", Toast.LENGTH_SHORT).show();
            }
        });
        // Set up existing icon click listeners
        setupIconClickListeners();
    }

    @SuppressLint("PotentialBehaviorOverride")
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
        gMap.setMinZoomPreference(17f); // Minimum zoom level
        gMap.setMaxZoomPreference(22f); // Maximum zoom level

        // Load markers from Firebase
        loadMarkersFromFirebase();
    }

    private void loadMarkersFromFirebase() {
        markersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                gMap.clear();
                for (DataSnapshot markerSnapshot : dataSnapshot.getChildren()) {
                    String title = markerSnapshot.child("name").getValue(String.class);
                    Double latitude = markerSnapshot.child("latitude").getValue(Double.class);
                    Double longitude = markerSnapshot.child("longitude").getValue(Double.class);
                    String description = markerSnapshot.child("description").getValue(String.class);
                    String iconResourceName = markerSnapshot.child("iconResource").getValue(String.class);
                    String drawableName = markerSnapshot.child("drawable").getValue(String.class);

                    if (latitude == null || longitude == null) {
                        Log.e(TAG, "Latitude or Longitude is null for marker: " + title);
                        continue;
                    }

                    LatLng position = new LatLng(latitude, longitude);

                    int iconResId = getResources().getIdentifier(iconResourceName, "drawable", getPackageName());
                    if (iconResId == 0) {
                        Log.e(TAG, "Icon resource not found for name: " + iconResourceName + ". Using default icon.");
                        iconResId = R.drawable.map_icon; // Replace with your default icon
                    }

                    // Add marker to the map with the specific icon
                    Marker marker = gMap.addMarker(new MarkerOptions()
                            .position(position)
                            .title(title)
                            .icon(setIcon(MainActivity.this, iconResId, title)));

                    // Store MarkerInfo in the marker's tag
                    if (marker != null) {
                        MarkerInfo markerInfo = new MarkerInfo(description, drawableName);
                        marker.setTag(markerInfo);
                    }
                }

                gMap.setOnMarkerClickListener(marker -> {
                    openBottomDialog(marker.getTitle(), (MarkerInfo) marker.getTag());
                    return true;
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load markers: ", databaseError.toException());
                Toast.makeText(MainActivity.this, "Failed to load markers", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private BitmapDescriptor setIcon(Activity context, int drawableID, String title) {
        View markerLayout = LayoutInflater.from(context).inflate(R.layout.custom_marker, null);

        ImageView markerImage = markerLayout.findViewById(R.id.marker_image);
        markerImage.setImageResource(drawableID);

        TextView markerTitle = markerLayout.findViewById(R.id.marker_title);
        markerTitle.setText(title);

        markerLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        markerLayout.layout(0, 0, markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerLayout.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    private void openBottomDialog(String title, MarkerInfo markerInfo) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);

        @SuppressLint("InflateParams")
        View view = getLayoutInflater().inflate(R.layout.marker_popup, null);

        TextView titleView = view.findViewById(R.id.marker_title);
        titleView.setText(title);

        TextView descView = view.findViewById(R.id.marker_description);
        descView.setText(markerInfo.getDescription());

        ImageView imageView = view.findViewById(R.id.marker_image);

        // Get the drawable resource ID
        int imageResId = getResources().getIdentifier(markerInfo.getDrawableName(), "drawable", getPackageName());

        if (imageResId != 0) {
            imageView.setImageResource(imageResId);
        } else {
            // If the drawable is not found, you can set a default image or hide the ImageView
            imageView.setImageResource(R.drawable.map_icon); // Replace with your default image
        }

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
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
                    new LatLngBounds(southwest, northeast), 100));
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
        profileIcon.setOnClickListener(v -> {
            // Create an intent to open ProfileActivity and pass user data
            Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
            profileIntent.putExtra("profilePictureUrl", profilePictureUrl);
            startActivity(profileIntent);
        });
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
