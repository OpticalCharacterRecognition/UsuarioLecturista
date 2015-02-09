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
 * Response to a location search ok: (Boolean) Bill search successful or failed prepays: (String) If
 * search successful contains a list of prepay events (see class Prepay on messages.py) error:
 * (String) If search failed, contains the reason, otherwise empty.
 * <p/>
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the backend. For a detailed explanation see:
 * <a href="http://code.google.com/p/google-http-java-client/wiki/JSON">http://code.google.com/p/google-http-java-client/wiki/JSON</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class MessagesGetPrepaysResponse extends com.google.api.client.json.GenericJson {

    /**
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private java.lang.String error;

    /**
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private java.lang.Boolean ok;

    /**
     * Message containing the details of Prepay event urlsafe_key: (String) unique id creation_date:
     * (String) account_number: (String) balance: (Integer) m3 at the creation of the bill prepay:
     * (Integer) amount of m3 to be prepaid amount: (Integer) payment due based on the factor from
     * JMAS
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private java.util.List<MessagesPrepay> prepays;

    /**
     * @return value or {@code null} for none
     */
    public java.lang.String getError() {
        return error;
    }

    /**
     * @param error error or {@code null} for none
     */
    public MessagesGetPrepaysResponse setError(java.lang.String error) {
        this.error = error;
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
    public MessagesGetPrepaysResponse setOk(java.lang.Boolean ok) {
        this.ok = ok;
        return this;
    }

    /**
     * Message containing the details of Prepay event urlsafe_key: (String) unique id creation_date:
     * (String) account_number: (String) balance: (Integer) m3 at the creation of the bill prepay:
     * (Integer) amount of m3 to be prepaid amount: (Integer) payment due based on the factor from
     * JMAS
     *
     * @return value or {@code null} for none
     */
    public java.util.List<MessagesPrepay> getPrepays() {
        return prepays;
    }

    /**
     * Message containing the details of Prepay event urlsafe_key: (String) unique id creation_date:
     * (String) account_number: (String) balance: (Integer) m3 at the creation of the bill prepay:
     * (Integer) amount of m3 to be prepaid amount: (Integer) payment due based on the factor from
     * JMAS
     *
     * @param prepays prepays or {@code null} for none
     */
    public MessagesGetPrepaysResponse setPrepays(java.util.List<MessagesPrepay> prepays) {
        this.prepays = prepays;
        return this;
    }

    @Override
    public MessagesGetPrepaysResponse set(String fieldName, Object value) {
        return (MessagesGetPrepaysResponse) super.set(fieldName, value);
    }

    @Override
    public MessagesGetPrepaysResponse clone() {
        return (MessagesGetPrepaysResponse) super.clone();
    }

}
