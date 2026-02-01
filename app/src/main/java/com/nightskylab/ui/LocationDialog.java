package com.nightskylab.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.nightskylab.R;

/**
 * Dialog for selecting observer location.
 */
public class LocationDialog extends Dialog {

    public interface LocationListener {
        void onLocationSelected(double latitude, double longitude, String name);

        void onUseCurrentLocation();
    }

    private LocationListener listener;
    private EditText latInput, lonInput;
    private Spinner locationSpinner;

    // 30 geographically diverse locations with accurate coordinates
    private static final String[] LOCATION_NAMES = {
            "Lisbon, Portugal",
            "London, UK",
            "Paris, France",
            "Berlin, Germany",
            "Rome, Italy",
            "Moscow, Russia",
            "Cairo, Egypt",
            "Cape Town, South Africa",
            "Nairobi, Kenya",
            "Dubai, UAE",
            "Mumbai, India",
            "Beijing, China",
            "Tokyo, Japan",
            "Seoul, South Korea",
            "Singapore",
            "Sydney, Australia",
            "Auckland, New Zealand",
            "Honolulu, Hawaii",
            "Los Angeles, USA",
            "Denver, USA",
            "New York, USA",
            "Toronto, Canada",
            "Mexico City, Mexico",
            "São Paulo, Brazil",
            "Buenos Aires, Argentina",
            "Santiago, Chile",
            "Reykjavik, Iceland",
            "Tromsø, Norway",
            "McMurdo Station, Antarctica",
            "Mauna Kea Observatory, Hawaii"
    };

    private static final double[][] LOCATION_COORDS = {
            { 38.7223, -9.1393 }, // Lisbon
            { 51.5074, -0.1278 }, // London
            { 48.8566, 2.3522 }, // Paris
            { 52.5200, 13.4050 }, // Berlin
            { 41.9028, 12.4964 }, // Rome
            { 55.7558, 37.6173 }, // Moscow
            { 30.0444, 31.2357 }, // Cairo
            { -33.9249, 18.4241 }, // Cape Town
            { -1.2921, 36.8219 }, // Nairobi
            { 25.2048, 55.2708 }, // Dubai
            { 19.0760, 72.8777 }, // Mumbai
            { 39.9042, 116.4074 }, // Beijing
            { 35.6762, 139.6503 }, // Tokyo
            { 37.5665, 126.9780 }, // Seoul
            { 1.3521, 103.8198 }, // Singapore
            { -33.8688, 151.2093 }, // Sydney
            { -36.8509, 174.7645 }, // Auckland
            { 21.3069, -157.8583 }, // Honolulu
            { 34.0522, -118.2437 }, // Los Angeles
            { 39.7392, -104.9903 }, // Denver
            { 40.7128, -74.0060 }, // New York
            { 43.6532, -79.3832 }, // Toronto
            { 19.4326, -99.1332 }, // Mexico City
            { -23.5505, -46.6333 }, // São Paulo
            { -34.6037, -58.3816 }, // Buenos Aires
            { -33.4489, -70.6693 }, // Santiago
            { 64.1466, -21.9426 }, // Reykjavik
            { 69.6492, 18.9560 }, // Tromsø
            { -77.8419, 166.6863 }, // McMurdo Station
            { 19.8207, -155.4680 } // Mauna Kea
    };

    public LocationDialog(Context context, LocationListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_location);

        latInput = findViewById(R.id.latInput);
        lonInput = findViewById(R.id.lonInput);
        locationSpinner = findViewById(R.id.locationSpinner);

        // Set up spinner with location list using custom layouts
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                R.layout.spinner_item,
                LOCATION_NAMES);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        locationSpinner.setAdapter(adapter);

        // Select from dropdown button
        findViewById(R.id.btnSelectFromList).setOnClickListener(v -> {
            int position = locationSpinner.getSelectedItemPosition();
            if (position >= 0 && position < LOCATION_COORDS.length) {
                double lat = LOCATION_COORDS[position][0];
                double lon = LOCATION_COORDS[position][1];
                selectPreset(lat, lon, LOCATION_NAMES[position]);
            }
        });

        // Manual coordinate entry
        findViewById(R.id.btnApply).setOnClickListener(v -> applyManualCoordinates());

        // Use current location
        findViewById(R.id.btnCurrentLocation).setOnClickListener(v -> {
            if (listener != null) {
                listener.onUseCurrentLocation();
            }
            dismiss();
        });

        // Cancel
        findViewById(R.id.btnCancel).setOnClickListener(v -> dismiss());
    }

    private void selectPreset(double lat, double lon, String name) {
        if (listener != null) {
            listener.onLocationSelected(lat, lon, name);
        }
        dismiss();
    }

    private void applyManualCoordinates() {
        try {
            double lat = Double.parseDouble(latInput.getText().toString());
            double lon = Double.parseDouble(lonInput.getText().toString());

            if (lat < -90 || lat > 90) {
                Toast.makeText(getContext(), "Latitude must be between -90 and 90", Toast.LENGTH_SHORT).show();
                return;
            }
            if (lon < -180 || lon > 180) {
                Toast.makeText(getContext(), "Longitude must be between -180 and 180", Toast.LENGTH_SHORT).show();
                return;
            }

            String name = "Custom (" + String.format("%.2f", lat) + ", " + String.format("%.2f", lon) + ")";
            if (listener != null) {
                listener.onLocationSelected(lat, lon, name);
            }
            dismiss();
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }
}
