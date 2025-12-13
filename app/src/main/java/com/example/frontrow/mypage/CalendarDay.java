package com.example.frontrow.mypage;

public class CalendarDay {

    public final boolean isEmpty;
    public final int day;          // 1~31
    public final String dateKey;   // yyyy.MM.dd

    private CalendarDay(boolean isEmpty, int day, String dateKey) {
        this.isEmpty = isEmpty;
        this.day = day;
        this.dateKey = dateKey;
    }

    public static CalendarDay empty() {
        return new CalendarDay(true, 0, null);
    }

    public static CalendarDay of(int day, String dateKey) {
        return new CalendarDay(false, day, dateKey);
    }
}
