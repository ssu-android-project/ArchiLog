package com.example.frontrow.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.frontrow.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private int selectedIndex = -1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    private GoogleMap mMap;

    private BitmapDescriptor defaultIcon;
    private BitmapDescriptor selectedIcon;

    private Marker selectedMarker = null;

    private BottomSheetBehavior<View> bottomSheetBehavior;
    private View bottomSheet;
    private ImageView imgBuilding;
    private TextView tvBuildingName;
    private TextView tvArchitectName;
    private TextView tvBuildingDesc;
    private Button btnDetail;

    private int topPadding = 0;
    private int bottomPaddingWhenSheetOpen = 0;

    private boolean keepSheetExpanded = false;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selectedIndex", selectedIndex);
        outState.putBoolean("keepSheetExpanded", keepSheetExpanded);
    }

    private BitmapDescriptor resizeMarker(int drawableRes, int width, int height) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), drawableRes);
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, width, height, false);
        return BitmapDescriptorFactory.fromBitmap(resized);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        if (savedInstanceState != null) {
            selectedIndex = savedInstanceState.getInt("selectedIndex", -1);
            keepSheetExpanded = savedInstanceState.getBoolean("keepSheetExpanded", false);
        }

        defaultIcon = resizeMarker(R.drawable.marker_default, 60, 80);
        selectedIcon = resizeMarker(R.drawable.marker_selected, 80, 100);

        bottomSheet = root.findViewById(R.id.bottom_sheet);

        bottomSheet.setClickable(true);
        bottomSheet.setFocusable(true);
        bottomSheet.setOnClickListener(v -> { /* 클릭 소비 */ });

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setHideable(true);

        bottomSheetBehavior.setPeekHeight(0, false);
        bottomSheetBehavior.setSkipCollapsed(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        imgBuilding     = root.findViewById(R.id.imgBuilding);
        tvBuildingName  = root.findViewById(R.id.tvBuildingName);
        tvArchitectName = root.findViewById(R.id.tvArchitectName);
        tvBuildingDesc  = root.findViewById(R.id.tvBuildingDesc);
        btnDetail       = root.findViewById(R.id.btnDetail);

        btnDetail.setOnClickListener(v -> {
            if (selectedIndex == -1) return;
            Intent intent = new Intent(requireContext(), PlaceDetailActivity.class);
            intent.putExtra("place_index", selectedIndex);
            startActivity(intent);
        });

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (keepSheetExpanded && selectedIndex != -1) {
            expandBottomSheetFully();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        setupMapUi();
        enableMyLocation();

        topPadding = getStatusBarHeight();
        bottomPaddingWhenSheetOpen = (int) (450 * getResources().getDisplayMetrics().density);

        LatLng[] positions =  {
                new LatLng(37.528921, 126.968690),
                new LatLng(37.566731, 127.009261),
                new LatLng(37.524143, 127.044114),
                new LatLng(37.538150, 127.058969),
                new LatLng(37.561105, 126.946442),
                new LatLng(37.576607, 126.983283),
                new LatLng(37.570352, 126.965203),
                new LatLng(37.525641, 127.046230),
                new LatLng(37.537980, 126.999313),
                new LatLng(37.577706, 126.988314),
                new LatLng(37.525848, 126.928375),
        };

        String[] names = {
                "아모레퍼시픽 본사", "DDP", "송은문화재단 사옥", "하우스 노웨어 서울", "이화여대 ECC",
                "서울공예박물관", "주한스위스대사관", "루이비통 메종 서울", "리움 미술관", "아라리오뮤지엄 인 스페이스",
                "더 현대 서울"
        };

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (int i = 0; i < positions.length; i++) {
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(positions[i])
                    .title(names[i])
                    .icon(defaultIcon)
            );
            if (marker != null) marker.setTag(i);
            builder.include(positions[i]);

            // If we are restoring state, re-select the marker
            if (i == selectedIndex) {
                if (selectedIcon != null) marker.setIcon(selectedIcon);
                selectedMarker = marker;
            }
        }

        LatLngBounds bounds = builder.build();
        int padding = 150;

        mMap.setOnMapLoadedCallback(() ->
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
        );

        mMap.setOnMarkerClickListener(marker -> {

            if (selectedMarker != null && defaultIcon != null) {
                selectedMarker.setIcon(defaultIcon);
            }
            if (selectedIcon != null) marker.setIcon(selectedIcon);
            selectedMarker = marker;

            Object tag = marker.getTag();
            if (tag instanceof Integer) {
                selectedIndex = (Integer) tag;
                Place place = PlaceRepository.getPlace(selectedIndex);

                imgBuilding.setImageResource(place.imageResId);
                tvBuildingName.setText(place.name);
                tvArchitectName.setText(place.architect);
                tvBuildingDesc.setText(place.shortDesc);
            } else {
                selectedIndex = -1;
                tvBuildingName.setText(marker.getTitle());
                tvArchitectName.setText(marker.getTitle());
                tvBuildingDesc.setText("설명 정보가 없습니다.");
                imgBuilding.setImageResource(R.drawable.sample_building);
            }

            keepSheetExpanded = true;

            if (mMap != null) {
                mMap.setPadding(0, topPadding, 0, bottomPaddingWhenSheetOpen);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
            }

            expandBottomSheetFully();
            return true;
        });

        mMap.setOnMapClickListener(latLng -> {
            keepSheetExpanded = false;
            hideBottomSheetAndReset();
        });

        if (keepSheetExpanded && selectedIndex != -1) {
            // Update the sheet content for the restored selection
            Place place = PlaceRepository.getPlace(selectedIndex);
            if (place != null) {
                imgBuilding.setImageResource(place.imageResId);
                tvBuildingName.setText(place.name);
                tvArchitectName.setText(place.architect);
                tvBuildingDesc.setText(place.shortDesc);
            }
            expandBottomSheetFully();
        }
    }

    private void expandBottomSheetFully() {
        if (bottomSheetBehavior == null || bottomSheet == null) return;

        bottomSheetBehavior.setPeekHeight(0, true);
        bottomSheetBehavior.setSkipCollapsed(true);

        bottomSheet.post(() -> {
            if (bottomSheetBehavior != null) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
    }

    private void hideBottomSheetAndReset() {
        if (bottomSheetBehavior != null) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }

        if (selectedMarker != null && defaultIcon != null) {
            selectedMarker.setIcon(defaultIcon);
            selectedMarker = null;
        }

        selectedIndex = -1;

        if (mMap != null) {
            mMap.setPadding(0, topPadding, 0, 0);
        }
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) result = getResources().getDimensionPixelSize(resourceId);
        return result;
    }

    private void setupMapUi() {
        if (mMap == null) return;
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setMapToolbarEnabled(true);
    }

    private void enableMyLocation() {
        if (mMap == null) return;

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            }
        }
    }
}
