package com.fourtails.usuariolecturista.ottoEvents;

/**
 * Created by Vazh on 5/5/2015.
 */
public class BillPaymentAttemptEvent {

    private double _amount;

    public BillPaymentAttemptEvent(double amount) {
        this._amount = amount;
    }

    public double getAmount() {
        return _amount;
    }

}
