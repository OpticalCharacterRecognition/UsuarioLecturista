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
 * Message containing the information of a Reading account_number: (String) measure: (Integer)
 * <p/>
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the backend. For a detailed explanation see:
 * <a href="http://code.google.com/p/google-http-java-client/wiki/JSON">http://code.google.com/p/google-http-java-client/wiki/JSON</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class MessagesNewReading extends com.google.api.client.json.GenericJson {

    /**
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key("account_number")
    private java.lang.String accountNumber;

    /**
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key("image_name")
    private java.lang.String imageName;

    /**
     * @return value or {@code null} for none
     */
    public java.lang.String getAccountNumber() {
        return accountNumber;
    }

    /**
     * @param accountNumber accountNumber or {@code null} for none
     */
    public MessagesNewReading setAccountNumber(java.lang.String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    /**
     * @return value or {@code null} for none
     */
    public java.lang.String getImageName() {
        return imageName;
    }

    /**
     * @param imageName imageName or {@code null} for none
     */
    public MessagesNewReading setImageName(java.lang.String imageName) {
        this.imageName = imageName;
        return this;
    }

    @Override
    public MessagesNewReading set(String fieldName, Object value) {
        return (MessagesNewReading) super.set(fieldName, value);
    }

    @Override
    public MessagesNewReading clone() {
        return (MessagesNewReading) super.clone();
    }

}
