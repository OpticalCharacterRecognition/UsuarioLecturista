package com.fourtails.usuariolecturista.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.api.client.util.DateTime;

/**
 * Created by Vazh on 2/4/2015.
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
    public DateTime dateTime;
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

    public ChartBill(int day, int month, int year, DateTime dateTime, double amount, long balance, String urlSafeKey, String accountNumber, String status) {
        this.day = day;
        this.month = month;
        this.year = year;
        this.dateTime = dateTime;
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
