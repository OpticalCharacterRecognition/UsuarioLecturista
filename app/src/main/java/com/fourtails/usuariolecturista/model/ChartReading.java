package com.fourtails.usuariolecturista.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.sql.Date;

/**
 * Created by Vazh on 12/2/2015.
 */
@Table(name = "ChartReadings")
public class ChartReading extends Model {

    @Column
    public String day;
    @Column
    public int value;
    @Column
    public Date date;

    public ChartReading() {
        super();
    }

    public ChartReading(String day, int value, Date date) {
        this.day = day;
        this.value = value;
        this.date = date;
    }

}
