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
 * on 2015-03-18 at 05:10:07 UTC 
 * Modify at your own risk.
 */

package com.appspot.ocr_backend.backend.model;

/**
 * Message containing the details of Prepay event urlsafe_key: (String) unique id creation_date:
 * (String) account_number: (String) balance: (Integer) m3 at the creation of the bill prepay:
 * (Integer) amount of m3 to be prepaid amount: (Integer) payment due based on the factor from JMAS
 * <p/>
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the backend. For a detailed explanation see:
 * <a href="http://code.google.com/p/google-http-java-client/wiki/JSON">http://code.google.com/p/google-http-java-client/wiki/JSON</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class MessagesPrepay extends com.google.api.client.json.GenericJson {

    /**
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key("account_number")
    private java.lang.String accountNumber;

    /**
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    private java.lang.Double amount;

    /**
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    @com.google.api.client.json.JsonString
    private java.lang.Long balance;

    /**
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key("creation_date")
    private com.google.api.client.util.DateTime creationDate;

    /**
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key
    @com.google.api.client.json.JsonString
    private java.lang.Long prepay;

    /**
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key("urlsafe_key")
    private java.lang.String urlsafeKey;

    /**
     * @return value or {@code null} for none
     */
    public java.lang.String getAccountNumber() {
        return accountNumber;
    }

    /**
     * @param accountNumber accountNumber or {@code null} for none
     */
    public MessagesPrepay setAccountNumber(java.lang.String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    /**
     * @return value or {@code null} for none
     */
    public java.lang.Double getAmount() {
        return amount;
    }

    /**
     * @param amount amount or {@code null} for none
     */
    public MessagesPrepay setAmount(java.lang.Double amount) {
        this.amount = amount;
        return this;
    }

    /**
     * @return value or {@code null} for none
     */
    public java.lang.Long getBalance() {
        return balance;
    }

    /**
     * @param balance balance or {@code null} for none
     */
    public MessagesPrepay setBalance(java.lang.Long balance) {
        this.balance = balance;
        return this;
    }

    /**
     * @return value or {@code null} for none
     */
    public com.google.api.client.util.DateTime getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate creationDate or {@code null} for none
     */
    public MessagesPrepay setCreationDate(com.google.api.client.util.DateTime creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    /**
     * @return value or {@code null} for none
     */
    public java.lang.Long getPrepay() {
        return prepay;
    }

    /**
     * @param prepay prepay or {@code null} for none
     */
    public MessagesPrepay setPrepay(java.lang.Long prepay) {
        this.prepay = prepay;
        return this;
    }

    /**
     * @return value or {@code null} for none
     */
    public java.lang.String getUrlsafeKey() {
        return urlsafeKey;
    }

    /**
     * @param urlsafeKey urlsafeKey or {@code null} for none
     */
    public MessagesPrepay setUrlsafeKey(java.lang.String urlsafeKey) {
        this.urlsafeKey = urlsafeKey;
        return this;
    }

    @Override
    public MessagesPrepay set(String fieldName, Object value) {
        return (MessagesPrepay) super.set(fieldName, value);
    }

    @Override
    public MessagesPrepay clone() {
        return (MessagesPrepay) super.clone();
    }

}
