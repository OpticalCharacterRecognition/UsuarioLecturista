package com.fourtails.usuariolecturista.ottoEvents;

/**
 * Created by Vazh on 5/5/2015.
 */
public class GetPrepaysEvent extends AbstractEvent {

    public enum Type {
        COMPLETED,
        STARTED
    }

    private int _resultCode;


    public GetPrepaysEvent(Type type, int resultCode) {
        super(type);
        this._resultCode = resultCode;
    }

    public int getResultCode() {
        return _resultCode;
    }

}
