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
 * (build: 2015-01-14 17:53:03 UTC)
 * on 2015-02-09 at 18:13:30 UTC 
 * Modify at your own risk.
 */

package com.appspot.ocr_backend.backend.model;

/**
 * Response to user information request ok: (Boolean) User search successful or failed error:
 * (String) If search failed, contains the reason, otherwise empty. email = (String) name = (String)
 * age = (Integer) account_type = (String)
 * <p/>
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the backend. For a detailed explanation see:
 * <a href="http://code.google.com/p/google-http-java-client/wiki/JSON">http://code.google.com/p/google-http-java-client/wiki/JSON</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class MessagesGetUserResponse extends com.google.api.client.json.GenericJson {

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
    @com.google.api.client.util.Key
    private java.lang.String error;

    /**
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private java.lang.String name;

    /**
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private java.lang.Boolean ok;

    /**
     * @return value or {@code null} for none
     */
    public java.lang.String getAccountType() {
        return accountType;
    }

    /**
     * @param accountType accountType or {@code null} for none
     */
    public MessagesGetUserResponse setAccountType(java.lang.String accountType) {
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
    public MessagesGetUserResponse setAge(java.lang.Long age) {
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
    public MessagesGetUserResponse setEmail(java.lang.String email) {
        this.email = email;
        return this;
    }

    /**
     * @return value or {@code null} for none
     */
    public java.lang.String getError() {
        return error;
    }

    /**
     * @param error error or {@code null} for none
     */
    public MessagesGetUserResponse setError(java.lang.String error) {
        this.error = error;
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
    public MessagesGetUserResponse setName(java.lang.String name) {
        this.name = name;
        return this;
    }

    /**
     * @return value or {@code null} for none
     */
    public java.lang.Boolean getOk() {
        return ok;
    }

    /**
     * @param ok ok or {@code null} for none
     */
    public MessagesGetUserResponse setOk(java.lang.Boolean ok) {
        this.ok = ok;
        return this;
    }

    @Override
    public MessagesGetUserResponse set(String fieldName, Object value) {
        return (MessagesGetUserResponse) super.set(fieldName, value);
    }

    @Override
    public MessagesGetUserResponse clone() {
        return (MessagesGetUserResponse) super.clone();
    }

}
