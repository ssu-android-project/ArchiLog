package com.example.frontrow.review;

public class ReviewCard {

    private String reviewId;     // ✅ Firestore document id (가장 중요)
    private String title;        // 후기 제목
    private String location;     // 위치
    private String date;         // 방문 날짜
    private float rating;        // 별점
    private String tag1;         // 태그 1
    private String tag2;         // 태그 2
    private String subtitle;     // 한 줄 요약
    private String body;         // 본문
    private int imageResId;      // 대표 이미지 리소스(임시)

    public ReviewCard(String reviewId,
                      String title,
                      String location,
                      String date,
                      float rating,
                      String tag1,
                      String tag2,
                      String subtitle,
                      String body,
                      int imageResId) {
        this.reviewId = reviewId;
        this.title = title;
        this.location = location;
        this.date = date;
        this.rating = rating;
        this.tag1 = tag1;
        this.tag2 = tag2;
        this.subtitle = subtitle;
        this.body = body;
        this.imageResId = imageResId;
    }

    public String getReviewId() { return reviewId; }
    public String getTitle() { return title; }
    public String getLocation() { return location; }
    public String getDate() { return date; }
    public float getRating() { return rating; }
    public String getTag1() { return tag1; }
    public String getTag2() { return tag2; }
    public String getSubtitle() { return subtitle; }
    public String getBody() { return body; }
    public int getImageResId() { return imageResId; }
}