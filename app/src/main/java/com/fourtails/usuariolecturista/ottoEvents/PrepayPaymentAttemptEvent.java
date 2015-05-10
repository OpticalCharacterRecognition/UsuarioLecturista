package com.fourtails.usuariolecturista.ottoEvents;

/**
 * Created by Vazh on 5/5/2015.
 */
public class PrepayPaymentAttemptEvent {

    private double _amount;
    private long _prepay;

    public PrepayPaymentAttemptEvent(double amount, long prepay) {
        this._amount = amount;
        this._prepay = prepay;
    }

    public double getAmount() {
        return _amount;
    }

    public long getPrepay() {
        return _prepay;
    }

}
