/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * This code was generated by https://code.google.com/p/google-apis-client-generator/
 * (build: 2015-05-05 20:00:12 UTC)
 * on 2015-05-07 at 22:27:28 UTC 
 * Modify at your own risk.
 */

package com.appspot.ocr_backend.backend.model;

/**
 * Message containing the information of a User email: (String) name: (String) age: (Integer)
 * account_type: (String) installation_id: (String) Parse ID for Push notifications
 * <p/>
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the backend. For a detailed explanation see:
 * <a href="http://code.google.com/p/google-http-java-client/wiki/JSON">http://code.google.com/p/google-http-java-client/wiki/JSON</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class MessagesCreateUser extends com.google.api.client.json.GenericJson {

    /**
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key("account_type")
    private java.lang.String accountType;

    /**
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    @com.google.api.client.json.JsonString
    private java.lang.Long age;

    /**
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private java.lang.String email;

    /**
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key("installation_id")
    private java.lang.String installationId;

    /**
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private java.lang.String name;

    /**
     * @return value or {@code null} for none
     */
    public java.lang.String getAccountType() {
        return accountType;
    }

    /**
     * @param accountType accountType or {@code null} for none
     */
    public MessagesCreateUser setAccountType(java.lang.String accountType) {
        this.accountType = accountType;
        return this;
    }

    /**
     * @return value or {@code null} for none
     */
    public java.lang.Long getAge() {
        return age;
    }

    /**
     * @param age age or {@code null} for none
     */
    public MessagesCreateUser setAge(java.lang.Long age) {
        this.age = age;
        return this;
    }

    /**
     * @return value or {@code null} for none
     */
    public java.lang.String getEmail() {
        return email;
    }

    /**
     * @param email email or {@code null} for none
     */
    public MessagesCreateUser setEmail(java.lang.String email) {
        this.email = email;
        return this;
    }

    /**
     * @return value or {@code null} for none
     */
    public java.lang.String getInstallationId() {
        return installationId;
    }

    /**
     * @param installationId installationId or {@code null} for none
     */
    public MessagesCreateUser setInstallationId(java.lang.String installationId) {
        this.installationId = installationId;
        return this;
    }

    /**
     * @return value or {@code null} for none
     */
    public java.lang.String getName() {
        return name;
    }

    /**
     * @param name name or {@code null} for none
     */
    public MessagesCreateUser setName(java.lang.String name) {
        this.name = name;
        return this;
    }

    @Override
    public MessagesCreateUser set(String fieldName, Object value) {
        return (MessagesCreateUser) super.set(fieldName, value);
    }

    @Override
    public MessagesCreateUser clone() {
        return (MessagesCreateUser) super.clone();
    }

}
