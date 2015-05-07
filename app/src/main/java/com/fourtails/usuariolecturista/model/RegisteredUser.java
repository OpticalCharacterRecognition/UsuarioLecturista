package com.fourtails.usuariolecturista.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by Vazh on 10/2/2015.
 */
@Table(name = "RegisteredUsers")
public class RegisteredUser extends Model {

    @Column
    public String accountType;
    @Column
    public long age;
    @Column
    public String email;
    @Column
    public String name;
    @Column
    public String installationId;

    public RegisteredUser() {
        super();
    }

    public RegisteredUser(String accountType, long age, String email, String name, String installationId) {
        super();
        this.accountType = accountType;
        this.age = age;
        this.email = email;
        this.name = name;
        this.installationId = installationId;
    }

}
