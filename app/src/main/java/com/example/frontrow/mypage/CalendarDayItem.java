package com.example.frontrow.mypage;
public class CalendarDayItem {
    public int day;              // 1~31
    public boolean inMonth;      // 이번 달 날짜인지
    public String reviewId;      // ⭐ 중요
    public String thumbUrl;      // 썸네일 이미지

    public CalendarDayItem(int day, boolean inMonth) {
        this.day = day;
        this.inMonth = inMonth;
    }
}
