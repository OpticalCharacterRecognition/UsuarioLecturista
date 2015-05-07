package com.fourtails.usuariolecturista.ottoEventBus;

/**
 * Created by Vazh on 5/5/2015.
 */
public class CheckIfUserHasMeterEvent extends AbstractEvent {
    public enum Type {
        COMPLETED,
        STARTED
    }

    private int _resultCode;

    private boolean _meterExists;

    public CheckIfUserHasMeterEvent(Type type, int resultCode, boolean meterExists) {
        super(type);
        this._resultCode = resultCode;
        this._meterExists = meterExists;
    }

    public int getResultCode() {
        return _resultCode;
    }

    public boolean getMeterExists() {
        return _meterExists;
    }

}
