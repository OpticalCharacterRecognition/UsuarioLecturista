package com.fourtails.usuariolecturista.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Stores the Readings to be displayed on the chart
 */
@Table(name = "ChartReadings")
public class ChartReading extends Model {

    @Column
    public int day;
    @Column
    public int month;
    @Column
    public int year;
    @Column
    public long timeInMillis;
    @Column
    public long value;
    @Column
    public String urlSafeKey;
    @Column
    public String accountNumber;

    public ChartReading(int day, int month, int year, long timeInMillis, long value, String urlSafeKey, String accountNumber) {
        this.day = day;
        this.month = month;
        this.year = year;
        this.timeInMillis = timeInMillis;
        this.value = value;
        this.urlSafeKey = urlSafeKey;
        this.accountNumber = accountNumber;
    }

    public ChartReading() {
        super();
    }
}
