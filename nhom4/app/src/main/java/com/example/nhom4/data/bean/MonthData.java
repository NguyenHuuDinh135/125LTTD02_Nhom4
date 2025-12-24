package com.example.nhom4.data.bean;

import java.time.YearMonth;
import java.util.List;

public class MonthData {
    public YearMonth yearMonth;
    public List<CalendarDay> days;

    public MonthData(YearMonth yearMonth, List<CalendarDay> days) {
        this.yearMonth = yearMonth;
        this.days = days;
    }
}