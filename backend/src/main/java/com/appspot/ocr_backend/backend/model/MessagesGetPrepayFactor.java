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
 * Message asking for Prepay events for an account m3_to_prepay: (String) Amount of m3 to prepay,
 * this allows for different factors depending on the amount to prepay.
 * <p/>
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the backend. For a detailed explanation see:
 * <a href="http://code.google.com/p/google-http-java-client/wiki/JSON">http://code.google.com/p/google-http-java-client/wiki/JSON</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class MessagesGetPrepayFactor extends com.google.api.client.json.GenericJson {

    /**
     * The value may be {@code null}.
     */
    @com.google.api.client.util.Key("m3_to_prepay")
    @com.google.api.client.json.JsonString
    private java.lang.Long m3ToPrepay;

    /**
     * @return value or {@code null} for none
     */
    public java.lang.Long getM3ToPrepay() {
        return m3ToPrepay;
    }

    /**
     * @param m3ToPrepay m3ToPrepay or {@code null} for none
     */
    public MessagesGetPrepayFactor setM3ToPrepay(java.lang.Long m3ToPrepay) {
        this.m3ToPrepay = m3ToPrepay;
        return this;
    }

    @Override
    public MessagesGetPrepayFactor set(String fieldName, Object value) {
        return (MessagesGetPrepayFactor) super.set(fieldName, value);
    }

    @Override
    public MessagesGetPrepayFactor clone() {
        return (MessagesGetPrepayFactor) super.clone();
    }

}