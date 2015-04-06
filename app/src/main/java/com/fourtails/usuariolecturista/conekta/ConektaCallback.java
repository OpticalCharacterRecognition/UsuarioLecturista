package com.fourtails.usuariolecturista.conekta;

/**
 * Created by mauriciomurga on 3/2/15.
 */

import com.conekta.Charge;
import com.conekta.Token;

public abstract class ConektaCallback {
    public abstract void failure(Exception error);

    public abstract void success(Token token);

    public abstract void success(Charge token);
}