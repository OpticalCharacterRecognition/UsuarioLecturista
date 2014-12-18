package com.fourtails.usuariolecturista.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by Vazh on 12/16/2014.
 */
@Table(name = "CreditCards")
public class CreditCard extends Model {

    @Column
    public String type;
    @Column
    public long number;
    @Column
    public String name;
    @Column
    public int ccv;
    @Column
    public String date;

    public CreditCard() {
        super();
    }

    public CreditCard(String type, long number, String name, int ccv, String date) {
        super();
        this.type = type;
        this.number = number;
        this.name = name;
        this.ccv = ccv;
        this.date = date;
    }

}
