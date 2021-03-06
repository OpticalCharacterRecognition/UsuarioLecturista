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
 * Response to a Bill search ok: (Boolean) Bill search successful or failed bills: (String) If
 * search successful contains a list of bills (see class Bill on messages.py) error: (String) If
 * search failed, contains the reason, otherwise empty.
 * <p/>
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the backend. For a detailed explanation see:
 * <a href="http://code.google.com/p/google-http-java-client/wiki/JSON">http://code.google.com/p/google-http-java-client/wiki/JSON</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class MessagesGetBillsResponse extends com.google.api.client.json.GenericJson {

    /**
     * Message containing the details of Bill urlsafe_key: (String) unique id creation_date: (String)
     * account_number: (String) balance: (Integer) m3 at the creation of the bill amount: (Integer)
     * payment due based on the factor from JMAS status: (String) 'Paid' or 'Unpaid'
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private java.util.List<MessagesBill> bills;

    static {
        // hack to force ProGuard to consider MessagesBill used, since otherwise it would be stripped out
        // see http://code.google.com/p/google-api-java-client/issues/detail?id=528
        com.google.api.client.util.Data.nullOf(MessagesBill.class);
    }

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
     * Message containing the details of Bill urlsafe_key: (String) unique id creation_date: (String)
     * account_number: (String) balance: (Integer) m3 at the creation of the bill amount: (Integer)
     * payment due based on the factor from JMAS status: (String) 'Paid' or 'Unpaid'
     *
     * @return value or {@code null} for none
     */
    public java.util.List<MessagesBill> getBills() {
        return bills;
    }

    /**
     * Message containing the details of Bill urlsafe_key: (String) unique id creation_date: (String)
     * account_number: (String) balance: (Integer) m3 at the creation of the bill amount: (Integer)
     * payment due based on the factor from JMAS status: (String) 'Paid' or 'Unpaid'
     *
     * @param bills bills or {@code null} for none
     */
    public MessagesGetBillsResponse setBills(java.util.List<MessagesBill> bills) {
        this.bills = bills;
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
    public MessagesGetBillsResponse setError(java.lang.String error) {
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
    public MessagesGetBillsResponse setOk(java.lang.Boolean ok) {
        this.ok = ok;
        return this;
    }

    @Override
    public MessagesGetBillsResponse set(String fieldName, Object value) {
        return (MessagesGetBillsResponse) super.set(fieldName, value);
    }

    @Override
    public MessagesGetBillsResponse clone() {
        return (MessagesGetBillsResponse) super.clone();
    }

}
