package me.alexeygusev.riders;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import me.alexeygusev.riders.runnable.RunOn;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Location mLocation;
    private int mScreenHeight;

    private RelativeLayout mPopupMenuPanel;
    private Button mToggleMessages;
    private ListView mMenuOptions;
    private TextView mGpsinfo;

    private ArrayList<String> mMockupOptionsArray = new ArrayList<>();

    private Timer mGpsInfoTimer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        mPopupMenuPanel = (RelativeLayout) findViewById(R.id.rlPopupMenuPanel);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        mScreenHeight = dm.heightPixels;
        mPopupMenuPanel.setY(-mScreenHeight);

        mMenuOptions = (ListView) findViewById(R.id.lvOptions);
        mMockupOptionsArray.add("Resting");
        mMockupOptionsArray.add("Broken down");
        mMockupOptionsArray.add("Accident");
        mMockupOptionsArray.add("Waiting customer");
        mMockupOptionsArray.add("Parking");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.messages_item_text,
                android.R.id.text1, mMockupOptionsArray);
        mMenuOptions.setAdapter(adapter);

        mGpsinfo = (TextView) findViewById(R.id.tvGPSInfo);

        mToggleMessages = (Button) findViewById(R.id.btnToggleMessages);
        mToggleMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int delta = 0;
                if (mPopupMenuPanel.getY() < 0)
                    delta = mScreenHeight;
                else
                    delta = -mScreenHeight;
                mPopupMenuPanel.animate().yBy(delta).setDuration(1000);
            }
        });

        mGpsInfoTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mMap == null)
                    return;
                RunOn.mainThread(new Runnable() {
                    @Override
                    public void run() {
                        Location loc = mMap.getMyLocation();
                        if (loc != null) {
                            String locStr = String.format(Locale.ENGLISH, "[%.4f, %.4f]", loc.getLatitude(), loc.getLongitude());
                            mGpsinfo.setText(locStr);
                        }
                    }
                });
            }
        }, 1000, 1000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setMyLocationEnabled(true);

        UiSettings settings = mMap.getUiSettings();
        settings.setMyLocationButtonEnabled(true);
        settings.setScrollGesturesEnabled(true);
        settings.setZoomGesturesEnabled(true);
        settings.setZoomControlsEnabled(false);

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (mLocation == null) {
                    mLocation = location;
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 12));
                }
            }
        });

        Location location = mMap.getMyLocation();
        if (location == null)
            return;

        double lat = location.getLatitude();
        double lon = location.getLongitude();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 12));

        mLocation = location;
    }
}
