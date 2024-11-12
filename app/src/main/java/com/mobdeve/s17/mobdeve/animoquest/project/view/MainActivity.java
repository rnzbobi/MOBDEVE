package com.mobdeve.s17.mobdeve.animoquest.project.view;

import com.mobdeve.s17.mobdeve.animoquest.project.BuildConfig;
import com.mobdeve.s17.mobdeve.animoquest.project.R;
import com.mobdeve.s17.mobdeve.animoquest.project.model.MarkerInfo;
import com.mobdeve.s17.mobdeve.animoquest.project.model.AutocompleteSuggestionAdapter;
import com.mobdeve.s17.mobdeve.animoquest.project.model.PlaceItem;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;

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

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;

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
    private LatLng selectedDestinationLatLng;
    private RecyclerView rvAutocompleteSuggestions;
    private AutocompleteSuggestionAdapter autocompleteSuggestionAdapter;

    // Fields to keep track of the route polyline and markers
    private Polyline currentPolyline;
    private Marker originMarker;
    private Marker destinationMarker;

    // Field to keep track of Firebase markers
    private List<Marker> firebaseMarkers = new ArrayList<>();

    // List to store all PlaceItems from Firebase
    private List<PlaceItem> allPlaceItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Database reference
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

        // Initialize UI components
        etDestination = findViewById(R.id.et_destination);
        btnGetDirections = findViewById(R.id.btn_get_directions);
        rvAutocompleteSuggestions = findViewById(R.id.rv_autocomplete_suggestions);

        // Set up RecyclerView for autocomplete suggestions
        autocompleteSuggestionAdapter = new AutocompleteSuggestionAdapter(placeItem -> {
            // Handle the user selection
            etDestination.setText(placeItem.getName());
            rvAutocompleteSuggestions.setVisibility(View.GONE);

            // Set the selected destination coordinates
            selectedDestinationLatLng = new LatLng(placeItem.getLatitude(), placeItem.getLongitude());
        });

        rvAutocompleteSuggestions.setLayoutManager(new LinearLayoutManager(this));
        rvAutocompleteSuggestions.setAdapter(autocompleteSuggestionAdapter);

        // Fetch places from Firebase
        fetchPlacesFromFirebase();

        // Set up TextWatcher for the EditText
        etDestination.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed before text changed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    rvAutocompleteSuggestions.setVisibility(View.VISIBLE);
                    getAutocompletePredictions(s.toString());
                } else {
                    rvAutocompleteSuggestions.setVisibility(View.GONE);
                    autocompleteSuggestionAdapter.clearPlaceItems();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed after text changed
            }
        });

        // Add OnEditorActionListener to the EditText
        etDestination.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {

                // Trigger the same action as the "Start Navigation" button
                startNavigation();

                // Hide the keyboard
                hideKeyboard();

                return true;
            }
            return false;
        });

        // Modify the button click listener
        btnGetDirections.setOnClickListener(v -> {
            startNavigation();
        });

        // Set up existing icon click listeners
        setupIconClickListeners();
    }

    private void fetchPlacesFromFirebase() {
        DatabaseReference placesRef = FirebaseDatabase.getInstance().getReference("places");

        placesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allPlaceItems.clear();
                for (DataSnapshot placeSnapshot : snapshot.getChildren()) {
                    PlaceItem placeItem = placeSnapshot.getValue(PlaceItem.class);
                    if (placeItem != null) {
                        allPlaceItems.add(placeItem);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to fetch places: ", error.toException());
            }
        });
    }

    private void startNavigation() {
        if (selectedDestinationLatLng != null) {
            LatLng origin = new LatLng(14.5647, 120.99313); // DLSU Coordinates
            getDirections(origin, selectedDestinationLatLng);
        } else {
            Toast.makeText(MainActivity.this, "Please select a destination", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void getAutocompletePredictions(String query) {
        List<PlaceItem> filteredPlaceItems = new ArrayList<>();
        for (PlaceItem placeItem : allPlaceItems) {
            if (placeItem.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredPlaceItems.add(placeItem);
            }
        }
        autocompleteSuggestionAdapter.setPlaceItems(filteredPlaceItems);
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
                // Remove existing Firebase markers from the map
                for (Marker marker : firebaseMarkers) {
                    marker.remove();
                }
                firebaseMarkers.clear();

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
                        // Add marker to the list of Firebase markers
                        firebaseMarkers.add(marker);
                    }
                }

                // Set marker click listener
                gMap.setOnMarkerClickListener(marker -> {
                    // Check if the marker is origin or destination to prevent interfering with route markers
                    if (marker.equals(originMarker) || marker.equals(destinationMarker)) {
                        return false;
                    }
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

    private void getDirections(LatLng origin, LatLng destination) {
        OkHttpClient client = new OkHttpClient();

        String url = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=" + origin.latitude + "," + origin.longitude +
                "&destination=" + destination.latitude + "," + destination.longitude +
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

    private void drawRouteOnMap(List<LatLng> path, LatLng origin, LatLng destination) {
        runOnUiThread(() -> {
            // Remove previous route polyline if it exists
            if (currentPolyline != null) {
                currentPolyline.remove();
            }

            // Remove previous origin/destination markers if they exist
            if (originMarker != null) {
                originMarker.remove();
            }
            if (destinationMarker != null) {
                destinationMarker.remove();
            }

            // Draw the polyline for the route
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(path)
                    .width(10)
                    .color(Color.rgb(255, 165, 0)); // Orange color

            currentPolyline = gMap.addPolyline(polylineOptions);

            originMarker = gMap.addMarker(new MarkerOptions()
                    .position(origin)
                    .title("Start")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))); // Custom green marker

            // Set the camera view to show the entire route
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            boundsBuilder.include(origin);
            boundsBuilder.include(destination);
            for (LatLng point : path) {
                boundsBuilder.include(point);
            }

            LatLngBounds bounds = boundsBuilder.build();
            int padding = 100; // Offset from edges of the map in pixels
            gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
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
