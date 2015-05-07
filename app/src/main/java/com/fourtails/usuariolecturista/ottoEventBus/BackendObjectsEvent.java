package com.fourtails.usuariolecturista.ottoEventBus;

/**
 * Readings, bills and prepaids to be passed on the otto event bus
 */
public class BackendObjectsEvent {
    public enum Type {
        READING, PAID_BILL, UNPAID_BILL, PAID_PREPAY, UNPAID_PREPAY
    }

    public enum Status {
        NORMAL, NOT_FOUND, ERROR
    }

    public Type type;
    public Status status;

    public BackendObjectsEvent(Type type, Status status) {
        this.type = type;
        this.status = status;
    }
}
