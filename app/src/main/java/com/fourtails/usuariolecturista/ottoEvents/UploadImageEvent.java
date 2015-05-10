package com.fourtails.usuariolecturista.ottoEvents;

/**
 * Created by Vazh on 5/5/2015.
 */
public class UploadImageEvent extends AbstractEvent {
    public enum Type {
        COMPLETED,
        STARTED
    }

    private int _resultCode;
    private String _imageName;

    public UploadImageEvent(Type type, int resultCode, String imageName) {
        super(type);
        this._resultCode = resultCode;
        this._imageName = imageName;

    }

    public int getResultCode() {
        return _resultCode;
    }

    public String getImageName() {
        return _imageName;
    }
}
