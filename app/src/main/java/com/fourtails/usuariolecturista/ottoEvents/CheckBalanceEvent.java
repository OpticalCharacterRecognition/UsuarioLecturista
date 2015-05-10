package com.fourtails.usuariolecturista.ottoEvents;

import android.os.Bundle;

/**
 * Created by Vazh on 5/5/2015.
 */
public class CheckBalanceEvent extends AbstractEvent {

    public enum Type {
        COMPLETED,
        STARTED
    }

    private int _resultCode;

    private long _balance;

    Bundle _savedInstanceState;


    public CheckBalanceEvent(Type type, int resultCode, long balance, Bundle savedInstanceState) {
        super(type);
        this._resultCode = resultCode;
        this._balance = balance;
        this._savedInstanceState = savedInstanceState;
    }

    public int getResultCode() {
        return _resultCode;
    }

    public long getBalance() {
        return _balance;
    }

    public Bundle getSavedInstanceState() {
        return _savedInstanceState;
    }
}
