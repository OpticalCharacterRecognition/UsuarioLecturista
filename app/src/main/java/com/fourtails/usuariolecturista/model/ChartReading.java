package com.fourtails.usuariolecturista.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by Vazh on 12/2/2015.
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
    public int value;

    public ChartReading() {
        super();
    }

    public ChartReading(int day, int month, int year, int value) {
        this.day = day;
        this.month = month;
        this.year = year;
        this.value = value;
    }


}
