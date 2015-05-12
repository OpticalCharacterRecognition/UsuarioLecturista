package com.fourtails.usuariolecturista.ottoEvents;

/**
 * Created by Vazh on 5/5/2015.
 */
public class GetPrepayFactorEvent extends AbstractEvent {


    public enum Type {
        COMPLETED,
        STARTED
    }

    private int _resultCode;
    private double _factor;


    public GetPrepayFactorEvent(Type type, int resultCode, double factor) {
        super(type);
        this._resultCode = resultCode;
        this._factor = factor;
    }

    public int getResultCode() {
        return _resultCode;
    }

    public double getFactor() {
        return _factor;
    }

}
