package com.example.calendar.calendar;

public interface CalendarTopView {

    int[] getCurrentSelectPositon();

    int getItemHeight();

    void setCaledarTopViewChangeListener(CaledarTopViewChangeListener listener);
}
