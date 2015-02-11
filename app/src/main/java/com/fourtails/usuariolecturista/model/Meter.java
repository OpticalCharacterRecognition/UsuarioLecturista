package com.fourtails.usuariolecturista.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by Vazh on 10/2/2015.
 */
@Table(name = "Meters")
public class Meter extends Model {
    @Column
    public String accountNumber;
    @Column
    public long balance;
    @Column
    public String modelType;

    public Meter() {
        super();
    }

    public Meter(String accountNumber, long balance, String modelType) {
        super();
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.modelType = modelType;
    }
}
