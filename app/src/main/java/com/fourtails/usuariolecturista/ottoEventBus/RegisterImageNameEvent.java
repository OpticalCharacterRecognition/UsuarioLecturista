package com.fourtails.usuariolecturista.ottoEventBus;

/**
 * Created by Vazh on 5/5/2015.
 */
public class RegisterImageNameEvent extends AbstractEvent {
    public enum Type {
        COMPLETED,
        STARTED
    }

    private int _resultCode;

    public RegisterImageNameEvent(Type type, int resultCode) {
        super(type);
        this._resultCode = resultCode;
    }

    public int getResultCode() {
        return _resultCode;
    }
}
