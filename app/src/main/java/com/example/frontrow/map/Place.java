package com.example.frontrow.map;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class Place implements Serializable {
    public int imageResId;
    public String name;
    public String architect;
    public String shortDesc;   // 바텀시트 요약
    public String longDesc;    // 상세 설명
    public String location;
    public String hours;
    public String tips;
    public String points;
    public LatLng position;

    public Place(int imageResId,
                 String name,
                 String architect,
                 String shortDesc,
                 String longDesc,
                 String location,
                 String hours,
                 String tips,
                 String points,
                 LatLng position) {

        this.imageResId = imageResId;
        this.name = name;
        this.architect = architect;
        this.shortDesc = shortDesc;
        this.longDesc = longDesc;
        this.location = location;
        this.hours = hours;
        this.tips = tips;
        this.points = points;
        this.position = position;
    }
}
