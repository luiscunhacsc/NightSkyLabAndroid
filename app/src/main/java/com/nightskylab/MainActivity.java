package com.nightskylab;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.nightskylab.ui.LocationDialog;
import com.nightskylab.views.SkyView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Main Activity for NightSkyLab Android app.
 */
public class MainActivity extends AppCompatActivity implements LocationDialog.LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private SkyView skyView;
    private TextView txtDate, txtLocation, txtViewDirection, txtTimeSpeed;
    private TextView txtUtcTime, txtLocalTime;
    private LinearLayout uiLayer;

    private FusedLocationProviderClient fusedLocationClient;
    private SimpleDateFormat dateFormat;
    private Handler uiUpdateHandler;
    private boolean isFullscreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        skyView = findViewById(R.id.skyView);
        txtDate = findViewById(R.id.txtDate);
        txtLocation = findViewById(R.id.txtLocation);
        txtViewDirection = findViewById(R.id.txtViewDirection);
        txtTimeSpeed = findViewById(R.id.txtTimeSpeed);
        txtUtcTime = findViewById(R.id.txtUtcTime);
        txtLocalTime = findViewById(R.id.txtLocalTime);
        uiLayer = findViewById(R.id.uiLayer);

        // Load sky data
        skyView.loadData(R.raw.stars, R.raw.constellations);

        // Set up gesture callbacks
        skyView.setGestureCallbacks(new SkyView.GestureCallbacks() {
            @Override
            public void onLongPress() {
                showOptionsMenu();
            }

            @Override
            public void onDoubleTap() {
                toggleUI();
            }

            @Override
            public void onTwoFingerTap() {
                showLocationDialog();
            }
        });

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up settings button
        ImageButton btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v -> showOptionsMenu());

        // Set up location button
        ImageButton btnLocation = findViewById(R.id.btnLocation);
        btnLocation.setOnClickListener(v -> showLocationDialog());

        // Set up time control buttons
        Button btnRewind = findViewById(R.id.btnRewind);
        Button btnResetTime = findViewById(R.id.btnResetTime);
        Button btnFastForward = findViewById(R.id.btnFastForward);

        btnRewind.setOnClickListener(v -> {
            // Decrease time speed (slower, then backwards, then faster backwards)
            double currentSpeed = skyView.getTimeSpeed();
            if (currentSpeed > 1) {
                skyView.setTimeSpeed(currentSpeed / 10f);
            } else if (currentSpeed > 0.1f) {
                skyView.setTimeSpeed(0); // Pause
            } else if (currentSpeed >= 0) {
                skyView.setTimeSpeed(-1); // Start going backward
            } else if (currentSpeed > -1000) {
                skyView.setTimeSpeed(currentSpeed * 10f); // Faster backward
            }
            Toast.makeText(this, getTimeSpeedLabel(skyView.getTimeSpeed()), Toast.LENGTH_SHORT).show();
        });

        btnResetTime.setOnClickListener(v -> {
            skyView.resetTime();
            skyView.setTimeSpeed(1);
            Toast.makeText(this, "Time reset to NOW", Toast.LENGTH_SHORT).show();
        });

        btnFastForward.setOnClickListener(v -> {
            // Increase time speed (forward faster)
            double currentSpeed = skyView.getTimeSpeed();
            if (currentSpeed < 0) {
                skyView.setTimeSpeed(currentSpeed / 10f); // Slower backward
                if (skyView.getTimeSpeed() > -0.1f) {
                    skyView.setTimeSpeed(0); // Pause
                }
            } else if (currentSpeed < 0.1f) {
                skyView.setTimeSpeed(1); // Resume normal
            } else if (currentSpeed < 1000) {
                skyView.setTimeSpeed(currentSpeed * 10f); // Faster forward
            }
            Toast.makeText(this, getTimeSpeedLabel(skyView.getTimeSpeed()), Toast.LENGTH_SHORT).show();
        });

        // Date formatter
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        // Start UI update loop
        uiUpdateHandler = new Handler();
        uiUpdateHandler.post(uiUpdateRunnable);
    }

    private String getTimeSpeedLabel(double speed) {
        if (speed == 0)
            return "⏸ Paused";
        if (speed == 1)
            return "▶ Real Time";
        if (speed < 0) {
            if (speed <= -100)
                return "⏪ " + (int) Math.abs(speed) + "×";
            return "⏪ " + Math.abs(speed) + "×";
        }
        if (speed >= 100)
            return "⏩ " + (int) speed + "×";
        return "⏩ " + speed + "×";
    }

    private final Runnable uiUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updateUI();
            uiUpdateHandler.postDelayed(this, 100); // Update UI 10 times per second
        }
    };

    private void updateUI() {
        Date currentDate = skyView.getCurrentDate();

        // Format UTC time
        SimpleDateFormat utcFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        txtUtcTime.setText("UTC " + utcFormat.format(currentDate));

        // Get timezone for the location (with DST support for known locations)
        TimeZone localTz = getTimezoneForLocation(skyView.getLocationName(), skyView.getLongitude());
        SimpleDateFormat localFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        localFormat.setTimeZone(localTz);

        // Display timezone offset (accounting for DST)
        int offsetMs = localTz.getOffset(currentDate.getTime());
        int offsetHours = offsetMs / 3600000;
        int offsetMinutes = Math.abs((offsetMs % 3600000) / 60000);
        String tzLabel;
        if (offsetMinutes == 0) {
            tzLabel = "UTC" + (offsetHours >= 0 ? "+" : "") + offsetHours;
        } else {
            tzLabel = "UTC" + (offsetHours >= 0 ? "+" : "") + offsetHours + ":" + String.format("%02d", offsetMinutes);
        }
        txtLocalTime.setText(tzLabel + " " + localFormat.format(currentDate));

        // Update date (UTC)
        SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateOnlyFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        txtDate.setText(dateOnlyFormat.format(currentDate));

        // Update location
        txtLocation.setText(skyView.getLocationName());

        // Update view direction
        double azDeg = Math.toDegrees(skyView.getViewAzimuth());
        azDeg = (azDeg + 360) % 360;
        String dir = getCardinalDirection(azDeg);
        txtViewDirection.setText(String.format(Locale.getDefault(), "%s (%.0f°)", dir, azDeg));

        // Update time speed
        double speed = skyView.getTimeSpeed();
        String speedText;
        if (skyView.isPaused()) {
            speedText = "⏸ PAUSED";
        } else if (speed == 1.0) {
            speedText = "▶ Real Time";
        } else if (speed > 1.0) {
            speedText = String.format(Locale.getDefault(), "▶▶ %.0f×", speed);
        } else if (speed < 0) {
            speedText = String.format(Locale.getDefault(), "◀◀ %.0f×", Math.abs(speed));
        } else {
            speedText = "▶ Real Time";
        }
        txtTimeSpeed.setText(speedText);
    }

    private String getCardinalDirection(double azDeg) {
        if (azDeg >= 315 || azDeg < 45)
            return "N";
        if (azDeg >= 45 && azDeg < 135)
            return "E";
        if (azDeg >= 135 && azDeg < 225)
            return "S";
        return "W";
    }

    /**
     * Get the proper timezone for a location, with DST support for known cities.
     */
    private TimeZone getTimezoneForLocation(String locationName, double longitude) {
        // Map known locations to their proper IANA timezone IDs (with DST support)
        if (locationName.contains("Lisbon"))
            return TimeZone.getTimeZone("Europe/Lisbon");
        if (locationName.contains("London"))
            return TimeZone.getTimeZone("Europe/London");
        if (locationName.contains("Paris"))
            return TimeZone.getTimeZone("Europe/Paris");
        if (locationName.contains("Berlin"))
            return TimeZone.getTimeZone("Europe/Berlin");
        if (locationName.contains("Rome"))
            return TimeZone.getTimeZone("Europe/Rome");
        if (locationName.contains("Moscow"))
            return TimeZone.getTimeZone("Europe/Moscow");
        if (locationName.contains("Cairo"))
            return TimeZone.getTimeZone("Africa/Cairo");
        if (locationName.contains("Cape Town"))
            return TimeZone.getTimeZone("Africa/Johannesburg");
        if (locationName.contains("Nairobi"))
            return TimeZone.getTimeZone("Africa/Nairobi");
        if (locationName.contains("Dubai"))
            return TimeZone.getTimeZone("Asia/Dubai");
        if (locationName.contains("Mumbai"))
            return TimeZone.getTimeZone("Asia/Kolkata");
        if (locationName.contains("Beijing"))
            return TimeZone.getTimeZone("Asia/Shanghai");
        if (locationName.contains("Tokyo"))
            return TimeZone.getTimeZone("Asia/Tokyo");
        if (locationName.contains("Seoul"))
            return TimeZone.getTimeZone("Asia/Seoul");
        if (locationName.contains("Singapore"))
            return TimeZone.getTimeZone("Asia/Singapore");
        if (locationName.contains("Sydney"))
            return TimeZone.getTimeZone("Australia/Sydney");
        if (locationName.contains("Auckland"))
            return TimeZone.getTimeZone("Pacific/Auckland");
        if (locationName.contains("Honolulu") || locationName.contains("Hawaii"))
            return TimeZone.getTimeZone("Pacific/Honolulu");
        if (locationName.contains("Los Angeles"))
            return TimeZone.getTimeZone("America/Los_Angeles");
        if (locationName.contains("Denver"))
            return TimeZone.getTimeZone("America/Denver");
        if (locationName.contains("New York"))
            return TimeZone.getTimeZone("America/New_York");
        if (locationName.contains("Toronto"))
            return TimeZone.getTimeZone("America/Toronto");
        if (locationName.contains("Mexico"))
            return TimeZone.getTimeZone("America/Mexico_City");
        if (locationName.contains("São Paulo") || locationName.contains("Sao Paulo"))
            return TimeZone.getTimeZone("America/Sao_Paulo");
        if (locationName.contains("Buenos Aires"))
            return TimeZone.getTimeZone("America/Buenos_Aires");
        if (locationName.contains("Santiago"))
            return TimeZone.getTimeZone("America/Santiago");
        if (locationName.contains("Reykjavik"))
            return TimeZone.getTimeZone("Atlantic/Reykjavik");
        if (locationName.contains("Tromsø") || locationName.contains("Tromso"))
            return TimeZone.getTimeZone("Europe/Oslo");
        if (locationName.contains("McMurdo"))
            return TimeZone.getTimeZone("Antarctica/McMurdo");

        // Fallback: calculate timezone from longitude (no DST)
        int offsetHours = (int) Math.round(longitude / 15.0);
        return TimeZone.getTimeZone("GMT" + (offsetHours >= 0 ? "+" : "") + offsetHours);
    }

    private void showOptionsMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_options, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Get button references
        Button btnCycleConstellations = dialogView.findViewById(R.id.btnCycleConstellations);
        Button btnCycleStarNames = dialogView.findViewById(R.id.btnCycleStarNames);
        Button btnToggleGrid = dialogView.findViewById(R.id.btnToggleGrid);
        Button btnToggleUI = dialogView.findViewById(R.id.btnToggleUI);
        Button btnNorth = dialogView.findViewById(R.id.btnNorth);
        Button btnEast = dialogView.findViewById(R.id.btnEast);
        Button btnSouth = dialogView.findViewById(R.id.btnSouth);
        Button btnWest = dialogView.findViewById(R.id.btnWest);
        Button btnSelectLocation = dialogView.findViewById(R.id.btnSelectLocation);
        Button btnClose = dialogView.findViewById(R.id.btnClose);
        Button btnExit = dialogView.findViewById(R.id.btnExit);

        // Update current states with readable labels
        updateConstellationButtonText(btnCycleConstellations);
        btnCycleStarNames.setText("✨ Star Names: " + skyView.getStarNameMode());
        btnToggleGrid.setText("📐 Grid: " + (skyView.isGridVisible() ? "ON" : "OFF"));

        // Set click listeners
        btnCycleConstellations.setOnClickListener(v -> {
            skyView.cycleConstellationMode();
            updateConstellationButtonText(btnCycleConstellations);
        });

        btnCycleStarNames.setOnClickListener(v -> {
            skyView.cycleStarNameMode();
            btnCycleStarNames.setText("✨ Star Names: " + skyView.getStarNameMode());
        });

        btnToggleGrid.setOnClickListener(v -> {
            skyView.toggleGrid();
            btnToggleGrid.setText("📐 Grid: " + (skyView.isGridVisible() ? "ON" : "OFF"));
        });

        btnToggleUI.setOnClickListener(v -> {
            toggleUI();
            dialog.dismiss();
        });

        btnNorth.setOnClickListener(v -> {
            snapToDirection(0);
            dialog.dismiss();
        });

        btnEast.setOnClickListener(v -> {
            snapToDirection(90);
            dialog.dismiss();
        });

        btnSouth.setOnClickListener(v -> {
            snapToDirection(180);
            dialog.dismiss();
        });

        btnWest.setOnClickListener(v -> {
            snapToDirection(270);
            dialog.dismiss();
        });

        btnSelectLocation.setOnClickListener(v -> {
            dialog.dismiss();
            showLocationDialog();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnExit.setOnClickListener(v -> finish());

        dialog.show();
    }

    private void updateConstellationButtonText(Button btn) {
        String modeText;
        switch (skyView.getConstellationMode()) {
            case OFF:
                modeText = "OFF";
                break;
            case LINES:
                modeText = "LINES";
                break;
            case LINES_NAMES:
                modeText = "LINES+NAMES";
                break;
            default:
                modeText = "OFF";
        }
        btn.setText("⭐ Constellations: " + modeText);
    }

    private void snapToDirection(int azimuth) {
        // Set view to the specified direction
        double azRad = Math.toRadians(azimuth);
        double altRad = Math.toRadians(30); // Look 30 degrees up
        skyView.setViewDirection(azRad, altRad);
        Toast.makeText(this, "View snapped to " + getCardinalDirection(azimuth), Toast.LENGTH_SHORT).show();
    }

    private void toggleUI() {
        if (uiLayer.getVisibility() == View.VISIBLE) {
            uiLayer.setVisibility(View.GONE);
        } else {
            uiLayer.setVisibility(View.VISIBLE);
        }
    }

    private void showLocationDialog() {
        LocationDialog dialog = new LocationDialog(this, this);
        dialog.show();
    }

    @Override
    public void onLocationSelected(double latitude, double longitude, String name) {
        skyView.setLocation(latitude, longitude, name);
        Toast.makeText(this, "Location set to " + name, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUseCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    LOCATION_PERMISSION_REQUEST);
            return;
        }

        getCurrentLocation();
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double lat = location.getLatitude();
                        double lon = location.getLongitude();
                        String name = String.format(Locale.getDefault(),
                                "GPS (%.2f, %.2f)", lat, lon);
                        skyView.setLocation(lat, lon, name);
                        Toast.makeText(this, "Using current location", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Could not get location", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Location error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiUpdateHandler.removeCallbacks(uiUpdateRunnable);
    }
}
