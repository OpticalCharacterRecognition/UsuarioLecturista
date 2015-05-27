package com.fourtails.usuariolecturista.ottoEvents;

/**
 * Created by Vazh on 5/5/2015.
 */
public class MakePaymentWithConektaEvent extends AbstractEvent {
    public enum Type {
        COMPLETED,
        STARTED
    }

    public boolean _isBill;

    private int _resultCode;

    private long _m3;

    private String _error;

    public MakePaymentWithConektaEvent(Type type, int resultCode, boolean _isBill, long _m3, String _error) {
        super(type);
        this._resultCode = resultCode;
        this._isBill = _isBill;
        this._m3 = _m3;
        this._error = _error;
    }

    public int getResultCode() {
        return _resultCode;
    }

    public boolean getIsBill() {
        return _isBill;
    }

    public long getM3() {
        return _m3;
    }

    public String getError() {
        return _error;
    }
}
