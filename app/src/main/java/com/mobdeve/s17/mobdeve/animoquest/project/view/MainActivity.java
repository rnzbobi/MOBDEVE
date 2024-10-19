package com.mobdeve.s17.mobdeve.animoquest.project.view;

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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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

        // Add a marker
        LatLng location = new LatLng(14.565010891802403, 120.9932070935714);
        LatLng location2 = new LatLng(14.5664620569781, 120.9932070935746);
        LatLng location3 = new LatLng(14.567179107795651, 120.99289729315733);
        LatLng location4 = new LatLng(14.56706754480281, 120.99213440937257);
        LatLng location5 = new LatLng(14.564374938729204, 120.99384588171114);

        gMap.addMarker(new MarkerOptions().position(location).title("Henry")
                .icon(setIcon(MainActivity.this,R.drawable.henry_marker,"Henry")));
        gMap.addMarker(new MarkerOptions().position(location2).title("Goks")
                .icon(setIcon(MainActivity.this,R.drawable.goks_marker, "Goks")));
        gMap.addMarker(new MarkerOptions().position(location3).title("Andrew")
                .icon(setIcon(MainActivity.this,R.drawable.andrew_marker, "Andrew")));
        gMap.addMarker(new MarkerOptions().position(location4).title("Razon")
                .icon(setIcon(MainActivity.this,R.drawable.razon_marker, "Razon")));
        gMap.addMarker(new MarkerOptions().position(location5).title("La Salle Hall")
                .icon(setIcon(MainActivity.this,R.drawable.lasallehall_marker, "La Salle Hall")));

        // Open the bottom dialog when a marker is clicked
        gMap.setOnMarkerClickListener(marker -> {
            openBottomDialog(marker.getTitle());
            return true;
        });

    }

    private BitmapDescriptor setIcon(Activity context, int drawableID, String title) {
        // Inflate the custom layout
        View markerLayout = LayoutInflater.from(context).inflate(R.layout.custom_marker, null);

        // Set the marker image
        ImageView markerImage = markerLayout.findViewById(R.id.marker_image);
        markerImage.setImageResource(drawableID);

        // Set the title text
        TextView markerTitle = markerLayout.findViewById(R.id.marker_title);
        markerTitle.setText(title);

        // Measure and layout the view
        markerLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        markerLayout.layout(0, 0, markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight());

        // Create a bitmap from the view
        Bitmap bitmap = Bitmap.createBitmap(markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerLayout.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    private void openBottomDialog(String title) {
        // 1. Create the BottomSheetDialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);

        // 2. Inflate the layout (you'll create this XML layout shortly)
        @SuppressLint("InflateParams")
        View view = getLayoutInflater().inflate(R.layout.marker_popup, null);

        // 3. Find views inside the dialog (example: title TextView)
        TextView titleView = view.findViewById(R.id.marker_title);
        titleView.setText(title);  // Set the marker title
        String drawable;
        String desc = "";
        switch (title) {
            case "Henry": drawable = "henry_photo";
                desc = "The Henry Sy Sr. Hall, abbreviated as HSSH and more widely known" +
                        " as “Henry” among students, is a 14-story building located in " +
                        "the middle of the campus, and is situated in between two " +
                        "historical buildings, the St. La Salle Hall and the " +
                        "Velasco Hall. Named after its main benefactor, business tycoon " +
                        "Henry Sy Sr., whose generous donations jump-started the " +
                        "project’s construction, the edifice is home to different " +
                        "administrative and executive offices, as well as the learning " +
                        "commons and the University’s library. The building was designed " +
                        "by the renowned architectural firm, Leandro V. Locsin Partners.\n" +
                        "\n";
            break;
            case "Goks": drawable = "gokongwei_photo";
                desc = "The Henry Sy Sr. Hall, abbreviated as HSSH and more widely known" +
                        " as “Henry” among students, is a 14-story building located in " +
                        "the middle of the campus, and is situated in between two " +
                        "historical buildings, the St. La Salle Hall and the " +
                        "Velasco Hall. Named after its main benefactor, business tycoon " +
                        "Henry Sy Sr., whose generous donations jump-started the " +
                        "project’s construction, the edifice is home to different " +
                        "administrative and executive offices, as well as the learning " +
                        "commons and the University’s library. The building was designed " +
                        "by the renowned architectural firm, Leandro V. Locsin Partners.\n" +
                        "\n";
            break;
            case "Andrew": drawable = "andrew_photo";
                desc = "The Henry Sy Sr. Hall, abbreviated as HSSH and more widely known" +
                        " as “Henry” among students, is a 14-story building located in " +
                        "the middle of the campus, and is situated in between two " +
                        "historical buildings, the St. La Salle Hall and the " +
                        "Velasco Hall. Named after its main benefactor, business tycoon " +
                        "Henry Sy Sr., whose generous donations jump-started the " +
                        "project’s construction, the edifice is home to different " +
                        "administrative and executive offices, as well as the learning " +
                        "commons and the University’s library. The building was designed " +
                        "by the renowned architectural firm, Leandro V. Locsin Partners.\n" +
                        "\n";
            break;
            case "Razon": drawable = "razon_photo";
                desc = "The Henry Sy Sr. Hall, abbreviated as HSSH and more widely known" +
                        " as “Henry” among students, is a 14-story building located in " +
                        "the middle of the campus, and is situated in between two " +
                        "historical buildings, the St. La Salle Hall and the " +
                        "Velasco Hall. Named after its main benefactor, business tycoon " +
                        "Henry Sy Sr., whose generous donations jump-started the " +
                        "project’s construction, the edifice is home to different " +
                        "administrative and executive offices, as well as the learning " +
                        "commons and the University’s library. The building was designed " +
                        "by the renowned architectural firm, Leandro V. Locsin Partners.\n" +
                        "\n";
            break;
            case "La Salle Hall": drawable = "lasallehall_photo";
                desc = "The Henry Sy Sr. Hall, abbreviated as HSSH and more widely known" +
                        " as “Henry” among students, is a 14-story building located in " +
                        "the middle of the campus, and is situated in between two " +
                        "historical buildings, the St. La Salle Hall and the " +
                        "Velasco Hall. Named after its main benefactor, business tycoon " +
                        "Henry Sy Sr., whose generous donations jump-started the " +
                        "project’s construction, the edifice is home to different " +
                        "administrative and executive offices, as well as the learning " +
                        "commons and the University’s library. The building was designed " +
                        "by the renowned architectural firm, Leandro V. Locsin Partners.\n" +
                        "\n";
            break;
            default: drawable = "logo_dlsu";
                desc = "null";
            break;
        }
        ImageView imageView = view.findViewById(R.id.marker_image);
        @SuppressLint("DiscouragedApi")
        int drawableId = getResources().getIdentifier(drawable, "drawable", getPackageName());
        imageView.setImageResource(drawableId);
        TextView descView = view.findViewById(R.id.marker_description);
        descView.setText(desc);
        // 4. Set the content view of the dialog
        bottomSheetDialog.setContentView(view);

        // 5. Show the dialog
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
