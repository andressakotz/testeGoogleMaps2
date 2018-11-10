package com.example.andre.testegooglemaps2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.Marker;


/*public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*MapView mapView = (MapView) findViewById(R.id.map) ;
        mapView.setClickable(true) ;

        MyLocationOverlay mlo = new MyLocationOverlay(this, mapView) ;
        mlo.enableCompass() ;
        mlo.enableMyLocation() ;
        mapView.getOverlays().add(mlo) ;
    }
}*/


import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private GoogleMap myMap;
    private ProgressDialog myProgress;

    private static final String MYTAG = "MYTAG";

    // Request Code to ask the user for permission to view their current location (***).
    // Value 8bit (value <256)
    public static final int REQUEST_ID_ACCESS_COURSE_FINE_LOCATION = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create Progress Bar.
        myProgress = new ProgressDialog(this);
        myProgress.setTitle("Map Loading ...");
        myProgress.setMessage("Please wait...");
        myProgress.setCancelable(true);
        // Display Progress Bar.
        myProgress.show();


        SupportMapFragment mapFragment
                = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        // Set callback listener, on Google Map ready.
        mapFragment.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap googleMap) {
                onMyMapReady(googleMap);
            }
        });

    }

    private void onMyMapReady(GoogleMap googleMap) {
        // Get Google Map from Fragment.
        myMap = googleMap;
        // Sét OnMapLoadedCallback Listener.
        myMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {

            @Override
            public void onMapLoaded() {
                // Map loaded. Dismiss this dialog, removing it from the screen.
                myProgress.dismiss();

                askPermissionsAndShowMyLocation();
            }
        });
        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        myMap.getUiSettings().setZoomControlsEnabled(true);
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
        myMap.setMyLocationEnabled(true);
    }


    private void askPermissionsAndShowMyLocation() {

        // With API> = 23, you have to ask the user for permission to view their location.
        if (Build.VERSION.SDK_INT >= 23) {
            int accessCoarsePermission
                    = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            int accessFinePermission
                    = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);


            if (accessCoarsePermission != PackageManager.PERMISSION_GRANTED
                    || accessFinePermission != PackageManager.PERMISSION_GRANTED) {
                // The Permissions to ask user.
                String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION};
                // Show a dialog asking the user to allow the above permissions.
                ActivityCompat.requestPermissions(this, permissions,
                        REQUEST_ID_ACCESS_COURSE_FINE_LOCATION);

                return;
            }
        }

        // Show current location on Map.
        this.showMyLocation();
    }

    // When you have the request results.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //
        switch (requestCode) {
            case REQUEST_ID_ACCESS_COURSE_FINE_LOCATION: {

                // Note: If request is cancelled, the result arrays are empty.
                // Permissions granted (read/write).
                if (grantResults.length > 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "Permission granted!", Toast.LENGTH_LONG).show();

                    // Show current location on Map.
                    this.showMyLocation();
                }
                // Cancelled or denied.
                else {
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    // Find Location provider is openning.
    private String getEnabledLocationProvider() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Criteria to find location provider.
        Criteria criteria = new Criteria();

        // Returns the name of the provider that best meets the given criteria.
        // ==> "gps", "network",...
        String bestProvider = locationManager.getBestProvider(criteria, true);

        boolean enabled = locationManager.isProviderEnabled(bestProvider);

        if (!enabled) {
            Toast.makeText(this, "No location provider enabled!", Toast.LENGTH_LONG).show();
            Log.i(MYTAG, "No location provider enabled!");
            return null;
        }
        return bestProvider;
    }

    // Call this method only when you have the permissions to view a user's location.
    private void showMyLocation() {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        String locationProvider = this.getEnabledLocationProvider();

        if (locationProvider == null) {
            return;
        }

        // Millisecond
        final long MIN_TIME_BW_UPDATES = 1000;
        // Met
        final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;

        Location myLocation = null;
        try {
            // This code need permissions (Asked above ***)
            locationManager.requestLocationUpdates(
                    locationProvider,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, (LocationListener) this);
            // Getting Location.
            // Lấy ra vị trí.
            myLocation = locationManager
                    .getLastKnownLocation(locationProvider);
        }
        // With Android API >= 23, need to catch SecurityException.
        catch (SecurityException e) {
            Toast.makeText(this, "Show My Location Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(MYTAG, "Show My Location Error:" + e.getMessage());
            e.printStackTrace();
            return;
        }

        if (myLocation != null) {

            LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)             // Sets the center of the map to location user
                    .zoom(15)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            myMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


            // Add Marker to Map
            MarkerOptions option = new MarkerOptions();
            option.title("My Location");
            option.snippet("....");
            option.position(latLng);
            Marker currentMarker = myMap.addMarker(option);
            currentMarker.showInfoWindow();
            
            final LatLng cordGoogleSp = new LatLng(-28.306047908006647,-54.27588880062103);
            final LatLng cordGoogleSp2 = new LatLng(-28.30868802112845,-54.278340339660645);
            final LatLng cordGoogleSp3 = new LatLng(-28.312003419212083,-54.27162945270538);
            final LatLng cordGoogleSp4 = new LatLng(-28.309731768707906,-54.270320534706116);
            final LatLng cordGoogleSp5 = new LatLng(-28.280380138904658,-54.251121282577515);
            final LatLng cordGoogleSp6 = new LatLng(-28.281296614083363,-54.26117420196533);
            final LatLng cordGoogleSp7 = new LatLng(-28.28946895632456,-54.26069140434265);
            final LatLng cordGoogleSp8 = new LatLng(-28.30149485775285,-54.272063970565796);
            final LatLng cordGoogleSp9 = new LatLng(-28.3048293721589,-54.27326023578644);
            /*final LatLng cordGoogleSp10 = new LatLng(-28.3048293721589,-54.27326023578644);
            final LatLng cordGoogleSp11 = new LatLng(-28.3048293721589,-54.27326023578644);
            final LatLng cordGoogleSp12 = new LatLng(-28.3048293721589,-54.27326023578644);
            final LatLng cordGoogleSp13 = new LatLng(-28.3048293721589,-54.27326023578644);
            final LatLng cordGoogleSp14 = new LatLng(-28.3048293721589,-54.27326023578644);
            final LatLng cordGoogleSp15 = new LatLng(-28.3048293721589,-54.27326023578644);
            final LatLng cordGoogleSp16 = new LatLng(-28.3048293721589,-54.27326023578644);*/

            myMap.addMarker(new MarkerOptions().position(cordGoogleSp));
            myMap.addMarker(new MarkerOptions().position(cordGoogleSp2));
            myMap.addMarker(new MarkerOptions().position(cordGoogleSp3));
            myMap.addMarker(new MarkerOptions().position(cordGoogleSp4));
            myMap.addMarker(new MarkerOptions().position(cordGoogleSp5));
            myMap.addMarker(new MarkerOptions().position(cordGoogleSp6));
            myMap.addMarker(new MarkerOptions().position(cordGoogleSp7));
            myMap.addMarker(new MarkerOptions().position(cordGoogleSp8));
            myMap.addMarker(new MarkerOptions().position(cordGoogleSp9));

            Marker meuMarcadorMarker = myMap.addMarker(new MarkerOptions().position(cordGoogleSp));
            meuMarcadorMarker.setTitle("Parada Agropecuária Fogo de Chão");
            meuMarcadorMarker.setSnippet("Rua Sete Povos das Missões");

            Marker meuMarcadorMarker2 = myMap.addMarker(new MarkerOptions().position(cordGoogleSp2));
            meuMarcadorMarker2.setTitle("Parada Redemaq");
            meuMarcadorMarker2.setSnippet("Rua São João Batista");

            Marker meuMarcadorMarker3 = myMap.addMarker(new MarkerOptions().position(cordGoogleSp3));
            meuMarcadorMarker3.setTitle("Parada 219");
            meuMarcadorMarker3.setSnippet("Avenida Rio Grande do Sul");

            Marker meuMarcadorMarker4 = myMap.addMarker(new MarkerOptions().position(cordGoogleSp4));
            meuMarcadorMarker4.setTitle("Parada 603");
            meuMarcadorMarker4.setSnippet("Rua General Carlos de Campos Gayão");

            Marker meuMarcadorMarker5 = myMap.addMarker(new MarkerOptions().position(cordGoogleSp5));
            meuMarcadorMarker5.setTitle("Parada 4672");
            meuMarcadorMarker5.setSnippet("Avenida Salgado Filho");

            Marker meuMarcadorMarker6 = myMap.addMarker(new MarkerOptions().position(cordGoogleSp6));
            meuMarcadorMarker6.setTitle("Parada Praça do Jayme");
            meuMarcadorMarker6.setSnippet("Rua Marechal Floriano Peixoto");

            Marker meuMarcadorMarker7 = myMap.addMarker(new MarkerOptions().position(cordGoogleSp7));
            meuMarcadorMarker7.setTitle("Parada Lancheria Real");
            meuMarcadorMarker7.setSnippet("Rua Marechal Floriano Peixoto");

            Marker meuMarcadorMarker8 = myMap.addMarker(new MarkerOptions().position(cordGoogleSp8));
            meuMarcadorMarker8.setTitle("Parada Mercado O Colono");
            meuMarcadorMarker8.setSnippet("Rua Dez de Novembro");

            Marker meuMarcadorMarker9 = myMap.addMarker(new MarkerOptions().position(cordGoogleSp9));
            meuMarcadorMarker9.setTitle("Parada Bar do Motorista");
            meuMarcadorMarker9.setSnippet("Rua General Carlos de Campos Gayão");


            meuMarcadorMarker.showInfoWindow();
            meuMarcadorMarker2.showInfoWindow();
            meuMarcadorMarker3.showInfoWindow();
            meuMarcadorMarker4.showInfoWindow();
            meuMarcadorMarker5.showInfoWindow();
            meuMarcadorMarker6.showInfoWindow();
            meuMarcadorMarker7.showInfoWindow();
            meuMarcadorMarker8.showInfoWindow();
            meuMarcadorMarker9.showInfoWindow();
            
        } else {
            Toast.makeText(this, "Location not found!", Toast.LENGTH_LONG).show();
            Log.i(MYTAG, "Location not found");
        }


    }
        } else {
            Toast.makeText(this, "Location not found!", Toast.LENGTH_LONG).show();
            Log.i(MYTAG, "Location not found");
        }


    }


    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

}




