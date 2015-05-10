package com.fourtails.usuariolecturista.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Stores the Readings to be displayed on the chart
 */
@Table(name = "ChartPrepays")
public class ChartPrepay extends Model {

    @Column
    public int day;
    @Column
    public int month;
    @Column
    public int year;
    @Column
    public long timeInMillis;
    @Column
    public double amount;
    @Column
    public long balance;
    @Column
    public long prepay;
    @Column
    public String urlSafeKey;
    @Column
    public String accountNumber;

    public ChartPrepay(int day, int month, int year, long timeInMillis, double amount, long balance, long prepay, String urlSafeKey, String accountNumber) {
        this.day = day;
        this.month = month;
        this.year = year;
        this.timeInMillis = timeInMillis;
        this.amount = amount;
        this.balance = balance;
        this.prepay = prepay;
        this.urlSafeKey = urlSafeKey;
        this.accountNumber = accountNumber;
    }

    public ChartPrepay() {
        super();
    }
}
