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

    // PNG 아이콘 캐싱
    private BitmapDescriptor defaultIcon;
    private BitmapDescriptor selectedIcon;

    private BitmapDescriptor resizeMarker(int drawableRes, int width, int height) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), drawableRes);
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, width, height, false);
        return BitmapDescriptorFactory.fromBitmap(resized);
    }

    // 현재 선택된 마커 저장
    private Marker selectedMarker = null;

    // 바텀시트 관련 뷰
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private ImageView imgBuilding;
    private TextView tvBuildingName;
    private TextView tvArchitectName;
    private TextView tvBuildingDesc;
    private Button btnDetail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // fragment_map.xml 사용
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        // PNG 아이콘 로드
        defaultIcon = resizeMarker(R.drawable.marker_default, 60, 80);
        selectedIcon = resizeMarker(R.drawable.marker_selected, 80, 100);

        // 바텀시트 뷰 초기화
        View bottomSheet = root.findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // 완전히 숨길 수 있게 허용
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN); // 시작은 숨김

        imgBuilding     = root.findViewById(R.id.imgBuilding);
        tvBuildingName  = root.findViewById(R.id.tvBuildingName);
        tvArchitectName = root.findViewById(R.id.tvArchitectName);
        tvBuildingDesc  = root.findViewById(R.id.tvBuildingDesc);
        btnDetail       = root.findViewById(R.id.btnDetail);

        // 상세보기 버튼 -> PlaceDetailActivity로 이동
        btnDetail.setOnClickListener(v -> {
            if (selectedIndex == -1) return;

            Intent intent = new Intent(requireContext(), PlaceDetailActivity.class);
            intent.putExtra("place_index", selectedIndex);
            startActivity(intent);
        });

        // 이 프래그먼트 안에 있는 지도 프래그먼트 찾기
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // 지도 UI 버튼들 먼저 켜주기
        setupMapUi();
        // 위치 권한 체크 후, 현위치 활성화
        enableMyLocation();

        final int topPadding = getStatusBarHeight();   // 상태바 높이(px)
        final int bottomPaddingWhenSheetOpen = (int) (
                450 * getResources().getDisplayMetrics().density
        ); // 바텀시트 높이 450dp → px 변환

        // PlaceRepository 에서 모든 장소 가져오기
        Place[] places = PlaceRepository.PLACES;

        // 더미 데이터 11개 (건축물 이름 + 위도/경도)
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

        // 마커 뿌리기
        for (int i = 0; i < positions.length; i++) {
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(positions[i])
                    .title(names[i])
                    .icon(defaultIcon)
            );

            if (marker != null) {
                marker.setTag(i); // 이 마커가 몇 번째 건물인지 태그로 저장
            }

            builder.include(positions[i]);   // 경계 박스에 추가
        }

        // Bounds 객체 생성
        LatLngBounds bounds = builder.build();
        int padding = 150;

        // 지도 뷰가 그려진 뒤 실행 (newLatLngBounds 에러 방지)
        mMap.setOnMapLoadedCallback(() ->
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
        );

        // 마커 클릭 시 : 아이콘 변경 + 바텀시트 내용 채우고 띄우기
        mMap.setOnMarkerClickListener(marker -> {

            // 기존 선택된 마커 복구
            if (selectedMarker != null) {
                selectedMarker.setIcon(defaultIcon);
            }

            // 새 마커를 선택 상태로 변경
            marker.setIcon(selectedIcon);
            selectedMarker = marker;

            // 이 마커가 몇 번째 데이터인지 가져오기
            Object tag = marker.getTag();
            if (tag instanceof Integer) {
                int index = (Integer) tag;
                selectedIndex = index;

                Place place = PlaceRepository.getPlace(index);

                imgBuilding.setImageResource(place.imageResId);
                tvBuildingName.setText(place.name);
                tvArchitectName.setText(place.architect);
                tvBuildingDesc.setText(place.shortDesc);
            } else {
                tvBuildingName.setText(marker.getTitle());
                tvArchitectName.setText(marker.getTitle());
                tvBuildingDesc.setText("설명 정보가 없습니다.");
                imgBuilding.setImageResource(R.drawable.sample_building);
            }

            // 상태바 + 바텀시트 사이 영역을 “지도 유효 영역”으로 설정
            mMap.setPadding(0, topPadding, 0, bottomPaddingWhenSheetOpen);

            // 카메라도 살짝 이동 (선택된 마커 쪽으로)
            mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));

            // 바텀시트 보이게
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            return true;
        });

        // 지도 빈 곳 클릭 시 바텀시트 선택 해제
        mMap.setOnMapClickListener(latLng -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

            if (selectedMarker != null) {
                selectedMarker.setIcon(defaultIcon);
                selectedMarker = null;
            }

            selectedIndex = -1;
        });
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier(
                "status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    // 지도 UI 버튼들 설정 (줌 버튼, 나침반, 내 위치 버튼 등)
    private void setupMapUi() {
        if (mMap == null) return;

        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);       // + / - 줌 버튼
        uiSettings.setCompassEnabled(true);            // 나침반
        uiSettings.setMyLocationButtonEnabled(true);   // 내 위치 버튼 (현위치 활성화해야 보임)
        uiSettings.setMapToolbarEnabled(true);         // 구글맵 앱으로 가는 툴바 (길찾기 등)
    }

    // 위치 권한 체크 + 현위치 활성화
    private void enableMyLocation() {
        if (mMap == null) return;

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true); // 지도에 파란 점 + 내 위치 버튼 활성화
        } else {
            // 프래그먼트용 권한 요청
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    // 권한 요청 결과 처리 (프래그먼트 버전)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();  // 다시 시도
            }
        }
    }
}