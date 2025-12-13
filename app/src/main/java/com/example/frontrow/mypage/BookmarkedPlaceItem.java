package com.example.frontrow.mypage;

public class BookmarkedPlaceItem {
    public int placeIndex;
    public String name;
    public String architect;
    public int imageResId;

    public BookmarkedPlaceItem(int placeIndex, String name, String architect, int imageResId) {
        this.placeIndex = placeIndex;
        this.name = name;
        this.architect = architect;
        this.imageResId = imageResId;
    }
}