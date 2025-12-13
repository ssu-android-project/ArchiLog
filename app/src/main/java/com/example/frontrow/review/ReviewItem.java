package com.example.frontrow.review;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewItem {
    public String reviewId;
    public String title;
    public String place;
    public String date;
    public String rating;
    public String tag1, tag2;
    public String body;
    public String authorUid;
    public String mainPhotoUrl; // This will be the first photo in the list
    public List<String> photoUrls; // List to hold all photo URLs
    public Timestamp createdAt;

    public ReviewItem() {
        photoUrls = new ArrayList<>(); // Initialize the list
    }

    public static ReviewItem fromDoc(DocumentSnapshot doc) {
        if (doc == null) return null;

        ReviewItem item = new ReviewItem();
        item.reviewId = doc.getId();
        item.title = doc.getString("title");
        item.place = doc.getString("place");
        item.date = doc.getString("date");
        item.rating = doc.getString("rating");
        item.tag1 = doc.getString("tag1");
        item.tag2 = doc.getString("tag2");
        item.body = doc.getString("body");
        item.authorUid = doc.getString("authorUid");
        item.mainPhotoUrl = doc.getString("mainPhotoUrl");
        item.createdAt = doc.getTimestamp("createdAt");

        // Retrieve the list of photo URLs
        Object urls = doc.get("photoUrls");
        if (urls instanceof List) {
            item.photoUrls = (List<String>) urls;
        } else {
            item.photoUrls = new ArrayList<>(); // Ensure the list is not null
        }

        return item;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("place", place);
        map.put("date", date);
        map.put("rating", rating);
        map.put("tag1", tag1);
        map.put("tag2", tag2);
        map.put("body", body);
        map.put("authorUid", authorUid);
        map.put("mainPhotoUrl", mainPhotoUrl);
        map.put("photoUrls", photoUrls); // Add the list to the map
        return map;
    }
}