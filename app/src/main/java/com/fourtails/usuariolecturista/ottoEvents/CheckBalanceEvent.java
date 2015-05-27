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

    private boolean _isFirstTime;


    public CheckBalanceEvent(Enum type, int _resultCode, long _balance, Bundle _savedInstanceState, boolean _isFirstTime) {
        super(type);
        this._resultCode = _resultCode;
        this._balance = _balance;
        this._savedInstanceState = _savedInstanceState;
        this._isFirstTime = _isFirstTime;
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

    public boolean isFirstTime() {
        return _isFirstTime;
    }
}
