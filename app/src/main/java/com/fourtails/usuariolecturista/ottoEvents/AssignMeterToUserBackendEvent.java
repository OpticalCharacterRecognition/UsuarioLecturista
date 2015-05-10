package com.fourtails.usuariolecturista.ottoEvents;

/**
 * Created by Vazh on 5/5/2015.
 */
public class AssignMeterToUserBackendEvent extends AbstractEvent {
    public enum Type {
        COMPLETED,
        STARTED
    }

    private int _resultCode;

    public AssignMeterToUserBackendEvent(Type type, int resultCode) {
        super(type);
        this._resultCode = resultCode;
    }

    public int getResultCode() {
        return _resultCode;
    }

}
