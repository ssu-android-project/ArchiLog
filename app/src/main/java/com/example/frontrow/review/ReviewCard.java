package com.example.frontrow.review;

public class ReviewCard {

    private String title;        // 후기 제목
    private String location;     // 위치 (예: 서울특별시 종로구)
    private String date;         // 방문 날짜 (예: 2024. 11. 12 방문)
    private float rating;        // 별점 (예: 4.5)
    private String tag1;         // 태그 1 (예: 브루탈리즘)
    private String tag2;         // 태그 2 (예: 노출 콘크리트)
    private String subtitle;     // 한 줄 요약 내용
    private String body;         // 자세한 후기 본문
    private int imageResId;      // 이미지 리소스 ID

    public ReviewCard(String title,
                      String location,
                      String date,
                      float rating,
                      String tag1,
                      String tag2,
                      String subtitle,
                      String body,
                      int imageResId) {

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

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public String getDate() {
        return date;
    }

    public float getRating() {
        return rating;
    }

    public String getTag1() {
        return tag1;
    }

    public String getTag2() {
        return tag2;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getBody() {
        return body;
    }

    public int getImageResId() {
        return imageResId;
    }
}