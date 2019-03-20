package com.example.mhci4;

import android.Manifest;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private ArFragment fragment;
    private PointerDrawable pointer = new PointerDrawable();
    private boolean isTracking;
    private boolean isHitting;
    private int modelType = 0;
    private String[] modelTypeString = new String[4];
    private String[] MessgeBoxTitleSetString = new String[4];
    private String[] MessgeBoxSetString = new String[4];

    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION =1;

    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private boolean LocationService = false;
    public boolean mLocationPermissionGranted = true;
    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private boolean gps_enabled, network_enabled;
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;
    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;
    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    private Boolean mRequestingLocationUpdates;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;
    //Callback for Location events.
    private LocationCallback mLocationCallback;
    //Setting a listener for bottom menu

    private GoogleApiClient mGoogleApiClient;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent intent = new Intent(MainActivity.this.getApplication(), MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                case R.id.navigation_profile:
                    Intent intent2 = new Intent(MainActivity.this.getApplication(), ProfileActivity.class);
                    startActivity(intent2);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                case R.id.navigation_settings:
                    Intent intent4 = new Intent(MainActivity.this.getApplication(), SettingsActivity.class);
                    startActivity(intent4);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                case R.id.navigation_info:
                    Intent intent5 = new Intent(MainActivity.this.getApplication(), FaqActivity.class);
                    startActivity(intent5);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        //Initialise bottom menu
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.getMenu().getItem(0).setChecked(true);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        if (this.mGoogleApiClient == null) {
            this.mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API).build();
        }
        // Prompt the user for permission.
        getLocationPermission();

        this.mLocationRequest = LocationRequest.create().setPriority(100).setInterval(30000).setFastestInterval(3000);
        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        fragment = (ArFragment)
                getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);

        fragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
            fragment.onUpdate(frameTime);
            onUpdate();
        });
        fragment.getPlaneDiscoveryController().hide();
        fragment.getPlaneDiscoveryController().setInstructionView(null);

        setModelTypeString();
        setMessgeBoxSetString();
        setObjects();
    }

    private void setModelTypeString(){
        modelTypeString[0] = "treasure.sfb";
        modelTypeString[1] = "donut.sfb";
        modelTypeString[2] = "pizza.sfb";
        modelTypeString[3] = "hamburger.sfb";
    }

    private void setMessgeBoxSetString(){
        MessgeBoxTitleSetString[0] = "OCBC Bank";
        MessgeBoxTitleSetString[1] = "Dunkin' Donuts";
        MessgeBoxTitleSetString[2] = "Pizza Hut";
        MessgeBoxTitleSetString[3] = "McDonald's";

        MessgeBoxSetString[0] = "Oversea-Chinese Banking Corporation, Limited, abbreviated as OCBC Bank, " +
                "is a multinational banking and financial services corporation headquartered in OCBC Centre, Singapore.";
        MessgeBoxSetString[1] = "Dunkin' Donuts, currently rebranding its stores as Dunkin', " +
                "is an American multinational coffee company and quick service restaurant. " +
                "It was founded by William Rosenberg in Quincy, Massachusetts in 1950.";
        MessgeBoxSetString[2] = "Pizza Hut is an American restaurant chain and international franchise which was founded in 1958 by Dan and Frank Carney. " +
                "The company is known for its Italian-American cuisine menu, including pizza and pasta, as well as side dishes and desserts.";
        MessgeBoxSetString[3] = "McDonald's is an American fast food company, founded in 1940 as a restaurant operated by Richard and Maurice McDonald, " +
                "in San Bernardino, California, United States.";
    }

    private void setObjects() {

        Button addObject = findViewById(R.id.button);

        addObject.setOnClickListener((View v) -> {
            try {
                addObject(Uri.parse(modelTypeString[modelType++]));
            } catch (Exception e) {
            }
        });

        Button addFloatingObject = findViewById(R.id.button2);

        addFloatingObject.setOnClickListener((View v) -> {
            try {
            addFloatingObjects();
            } catch (Exception e) {
            }
        });

        Button increment = findViewById(R.id.button3);

        increment.setOnClickListener((View v) -> {
            validateArray();
        });
    }

    private void validateArray()
    {
        if(modelType < 3) {
            modelType++;
        }
        else {
            Button A = findViewById(R.id.button);
            A.setVisibility(View.GONE);

            Button B = findViewById(R.id.button2);
            B.setVisibility(View.GONE);

            Button C = findViewById(R.id.button3);
            C.setVisibility(View.GONE);
        }

    }

    private void onUpdate() {
        boolean trackingChanged = updateTracking();
        View contentView = findViewById(android.R.id.content);
        if (trackingChanged) {
            if (isTracking) {
                contentView.getOverlay().add(pointer);
            } else {
                contentView.getOverlay().remove(pointer);
            }
            contentView.invalidate();
        }

        if (isTracking) {
            boolean hitTestChanged = updateHitTest();
            if (hitTestChanged) {
                pointer.setEnabled(isHitting);
                contentView.invalidate();
            }
        }
    }

    private void addFloatingObjects()
    {
        final Renderable[] renderableA = new Renderable[1];
        String messgeBoxTitleSetString = MessgeBoxTitleSetString[modelType];
        String messgeBoxSetString = MessgeBoxSetString[modelType];

        Uri model = Uri.parse(modelTypeString[modelType]);
        ModelRenderable.builder()
                .setSource(this, model)

                .build()
                .thenAccept(renderableB -> renderableA[0] = renderableB)
                .exceptionally(
                        throwable -> {
                            return null;
                        });

        Vector3 cameraPos = fragment.getArSceneView().getScene().getCamera().getWorldPosition();
        Vector3 cameraForward = fragment.getArSceneView().getScene().getCamera().getForward();
        Vector3 position = Vector3.add(cameraPos, cameraForward.scaled(5.0f));

        Pose pose = Pose.makeTranslation(position.x, position.y, position.z);
        Anchor anchor2 = fragment.getArSceneView().getSession().createAnchor(pose);

        AnchorNode mAnchorNode = new AnchorNode(anchor2);
        mAnchorNode.setParent(fragment.getArSceneView().getScene());

        Node node = new Node();
        node.setRenderable(renderableA[0]);
        node.setParent(mAnchorNode);
        node.setOnTapListener(new Node.OnTapListener() {
            @Override
            public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                messageBox(messgeBoxTitleSetString, messgeBoxSetString);
            }
        });
    }

    private void AddArrow() {
        final Renderable[] renderableA = new Renderable[1];
        Uri model = Uri.parse("model.sfb");
        ModelRenderable.builder()
                .setSource(this, model)

                .build()
                .thenAccept(renderableB -> renderableA[0] = renderableB)
                .exceptionally(
                        throwable -> {
                            return null;
                        });

        float distance = 10.0f;
        for (int i = 0; i < 10; i++) {
            Vector3 cameraPos = fragment.getArSceneView().getScene().getCamera().getWorldPosition();
            Vector3 cameraForward = fragment.getArSceneView().getScene().getCamera().getForward();
            Vector3 position = Vector3.add(cameraPos, cameraForward.scaled(distance));

            Pose pose = Pose.makeTranslation(position.x, position.y, position.z);
            Anchor anchor2 = fragment.getArSceneView().getSession().createAnchor(pose);

            AnchorNode mAnchorNode = new AnchorNode(anchor2);
            mAnchorNode.setParent(fragment.getArSceneView().getScene());

            Node node = new Node();
            node.setRenderable(renderableA[0]);
            node.setParent(mAnchorNode);
            distance = distance - 1.0f;
        }
    }

    private void messageBox(String messgeBoxTitleSetString, String messgeBoxSetString)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(Html.fromHtml("<font color=#2F4F4F>" + messgeBoxTitleSetString + "</font>"));
        alertDialog.setMessage(messgeBoxSetString);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, ("Let's go!"),
                (dialog, which) -> AddArrow());

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, ("Back"),
                (dialog, which) -> dialog.dismiss());

        alertDialog.setCancelable(false);
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#DC143C"));
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#228B22"));
    }

    private void addObject(Uri model) {
        Frame frame = fragment.getArSceneView().getArFrame();
        android.graphics.Point pt = getScreenCenter();
        List<HitResult> hits;
        if (frame != null) {
            hits = frame.hitTest(pt.x, pt.y);
            for (HitResult hit : hits) {
                Trackable trackable = hit.getTrackable();
                if (trackable instanceof Plane &&
                        ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                    placeObject(fragment, hit.createAnchor(), model);
                    break;

                }
            }
        }
    }

    private void placeObject(ArFragment fragment, Anchor anchor, Uri model) {
        CompletableFuture<Void> renderableFuture =
                ModelRenderable.builder()
                        .setSource(fragment.getContext(), model)
                        .build()
                        .thenAccept(renderable -> addNodeToScene(fragment, anchor, renderable))
                        .exceptionally((throwable -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setMessage(throwable.getMessage())
                                    .setTitle("Error!");
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return null;
                        }));
    }

    private void addNodeToScene(ArFragment fragment, Anchor anchor, Renderable renderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode node = new TransformableNode(fragment.getTransformationSystem());
        node.setRenderable(renderable);
        node.setParent(anchorNode);
        fragment.getArSceneView().getScene().addChild(anchorNode);
        node.select();
    }

    private boolean updateTracking() {
        Frame frame = fragment.getArSceneView().getArFrame();
        boolean wasTracking = isTracking;
        isTracking = frame != null &&
                frame.getCamera().getTrackingState() == TrackingState.TRACKING;
        return isTracking != wasTracking;
    }

    private boolean updateHitTest() {
        Frame frame = fragment.getArSceneView().getArFrame();
        android.graphics.Point pt = getScreenCenter();
        List<HitResult> hits;
        boolean wasHitting = isHitting;
        isHitting = false;
        if (frame != null) {
            hits = frame.hitTest(pt.x, pt.y);
            for (HitResult hit : hits) {
                Trackable trackable = hit.getTrackable();
                if (trackable instanceof Plane &&
                        ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                    isHitting = true;
                    break;
                }
            }
        }
        return wasHitting != isHitting;
    }

    private android.graphics.Point getScreenCenter() {
        View vw = findViewById(android.R.id.content);
        return new android.graphics.Point(vw.getWidth()/2, vw.getHeight()/2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRestart() {
        super.onRestart();

        //When BACK BUTTON is pressed, the activity on the stack is restarted
        //Do what you want on the refresh procedure here
    }

    protected void onResume() {
        super.onResume();
        this.mGoogleApiClient.connect();
    }

    protected void onPause() {
        super.onPause();
        Log.v(getClass().getSimpleName(), "onPause()");
        if (this.mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(this.mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);
            this.mGoogleApiClient.disconnect();
        }
    }

    public void onConnected(Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == 0)
        {
            Location location = LocationServices.FusedLocationApi.getLastLocation(this.mGoogleApiClient);
            if (location == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient, this.mLocationRequest, (com.google.android.gms.location.LocationListener) this);
                return;
            }
//            latitude = location.getLatitude();
//            longitude = location.getLongitude();
//            latitude = (latitude - 0.904175497966933d) / 9.04510533984752E-5d;
//            longitude= (longitude - 103.395275766039d) / 8.98649589566756E-5d;
//
//            this.current_MGRS.setText(((int) longitude) + " " + ((int) latitude));

        }
    }

    public void onConnectionSuspended(int i) {
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                return;
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
                return;
            }
        }
        Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
    }

    public void onLocationChanged(Location location) {
        //Do something
    }
    public void isLocationOn()
    {
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        while(LocationService == false)
            try
            {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                LocationService = true;
            }catch (Exception ex){}
        try
        {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            LocationService = true;
        }catch (Exception ex){}
        if(!gps_enabled && !network_enabled){
            android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(this);
            dialog.setMessage(getResources().getString(R.string.please_turn_on_gps));
            dialog.setPositiveButton(getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    MainActivity.this.startActivity(myIntent);
                }
            });
            dialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub

                }
            });
            dialog.show();
        }
    }


    public boolean isServicesWorking()
    {
        Log.d(TAG, "isServicesWorking: Checking goggle services version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS)
        {
            //Everything is fine
            Log.d(TAG, "isServicesWorking: Google Play services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available))
        {
            //An error occured but we can resolved it
            Log.d(TAG, "isServicesWorking: An error occured but we can resolve it)");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else
        {
            //Unable to load Google Map
            Toast.makeText(this, "Can't make map request", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    private void getLocationPermission()
    {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;

        }
        else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

//    private void getDeviceLocation()
//    {
//        /*
//         * Get the best and most recent location of the device, which may be null in rare
//         * cases when a location is not available.
//         */
//        try {
//            if (mLocationPermissionGranted) {
//                Task locationResult = mFusedLocationProviderClient.getLastLocation();
//                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
//                    @Override
//                    public void onComplete(@NonNull Task task) {
//                        if (task.isSuccessful() && task.getResult() != null) {
//                            // Set the map's camera position to the current location of the device.
//                            mLastKnownLocation = (Location) task.getResult();
//                            latitude = mLastKnownLocation.getLatitude();
//
//                            longitude = mLastKnownLocation.getLongitude();
//
//                            latitude = (latitude - 0.904175497966933d) / 9.04510533984752E-5d;
//
//                            longitude= (longitude - 103.395275766039d) / 8.98649589566756E-5d;
//
//                            //GET LAT LON FROM LOCATION
//                            current_MGRS.setText(((int) longitude) + " " + ((int) latitude));
//
//                        } else {
//                            current_MGRS.setText("Location not available!");
//                            Log.d(TAG, "Current location is null. Using defaults.");
//                            Log.e(TAG, "Exception: %s", task.getException());
//                        }
//                    }
//                });
//            }
//        } catch(SecurityException e)  {
//            Log.e("Exception: %s", e.getMessage());
//        }
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults)
    {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI()
    {
        try {
            if (mLocationPermissionGranted)
            {
//                getDeviceLocation();
            }
            else
            {
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
}
