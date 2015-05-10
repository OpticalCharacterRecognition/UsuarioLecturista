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
 * Message containing the information of a meter account_number: (String)
 * <p/>
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the backend. For a detailed explanation see:
 * <a href="http://code.google.com/p/google-http-java-client/wiki/JSON">http://code.google.com/p/google-http-java-client/wiki/JSON</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class MessagesGetMeter extends com.google.api.client.json.GenericJson {

    /**
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key("account_number")
    private java.lang.String accountNumber;

    /**
     * @return value or {@code null} for none
     */
    public java.lang.String getAccountNumber() {
        return accountNumber;
    }

    /**
     * @param accountNumber accountNumber or {@code null} for none
     */
    public MessagesGetMeter setAccountNumber(java.lang.String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    @Override
    public MessagesGetMeter set(String fieldName, Object value) {
        return (MessagesGetMeter) super.set(fieldName, value);
    }

    @Override
    public MessagesGetMeter clone() {
        return (MessagesGetMeter) super.clone();
    }

}
