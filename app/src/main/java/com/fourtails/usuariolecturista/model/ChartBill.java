package com.fourtails.usuariolecturista.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Stores the Bills to be displayed on the bill chart
 */
@Table(name = "ChartBills")
public class ChartBill extends Model {

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
    public String urlSafeKey;
    @Column
    public String accountNumber;
    @Column
    public String status;

    public ChartBill(int day, int month, int year, long timeInMillis, double amount, long balance, String urlSafeKey, String accountNumber, String status) {
        this.day = day;
        this.month = month;
        this.year = year;
        this.timeInMillis = timeInMillis;
        this.amount = amount;
        this.balance = balance;
        this.urlSafeKey = urlSafeKey;
        this.accountNumber = accountNumber;
        this.status = status;
    }

    public ChartBill() {
        super();
    }
}
