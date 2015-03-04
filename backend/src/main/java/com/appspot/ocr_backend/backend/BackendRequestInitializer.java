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
 * on 2015-02-27 at 02:27:09 UTC 
 * Modify at your own risk.
 */

package com.appspot.ocr_backend.backend;

/**
 * Backend request initializer for setting properties like key and userIp.
 *
 * <p>
 * The simplest usage is to use it to set the key parameter:
 * </p>
 *
 * <pre>
 public static final GoogleClientRequestInitializer KEY_INITIALIZER =
 new BackendRequestInitializer(KEY);
 * </pre>
 *
 * <p>
 * There is also a constructor to set both the key and userIp parameters:
 * </p>
 *
 * <pre>
 public static final GoogleClientRequestInitializer INITIALIZER =
 new BackendRequestInitializer(KEY, USER_IP);
 * </pre>
 *
 * <p>
 * If you want to implement custom logic, extend it like this:
 * </p>
 *
 * <pre>
 public static class MyRequestInitializer extends BackendRequestInitializer {

 {@literal @}Override
 public void initializeBackendRequest(BackendRequest{@literal <}?{@literal >} request)
 throws IOException {
 // custom logic
 }
 }
 * </pre>
 *
 * <p>
 * Finally, to set the key and userIp parameters and insert custom logic, extend it like this:
 * </p>
 *
 * <pre>
 public static class MyRequestInitializer2 extends BackendRequestInitializer {

 public MyKeyRequestInitializer() {
 super(KEY, USER_IP);
 }

 {@literal @}Override
 public void initializeBackendRequest(BackendRequest{@literal <}?{@literal >} request)
 throws IOException {
 // custom logic
 }
 }
 * </pre>
 *
 * <p>
 * Subclasses should be thread-safe.
 * </p>
 *
 * @since 1.12
 */
public class BackendRequestInitializer extends com.google.api.client.googleapis.services.json.CommonGoogleJsonClientRequestInitializer {

    public BackendRequestInitializer() {
        super();
    }

    /**
     * @param key API key or {@code null} to leave it unchanged
     */
    public BackendRequestInitializer(String key) {
        super(key);
    }

    /**
     * @param key    API key or {@code null} to leave it unchanged
     * @param userIp user IP or {@code null} to leave it unchanged
     */
    public BackendRequestInitializer(String key, String userIp) {
        super(key, userIp);
    }

    @Override
    public final void initializeJsonRequest(com.google.api.client.googleapis.services.json.AbstractGoogleJsonClientRequest<?> request) throws java.io.IOException {
        super.initializeJsonRequest(request);
        initializeBackendRequest((BackendRequest<?>) request);
    }

    /**
     * Initializes Backend request.
     * <p/>
     * <p>
     * Default implementation does nothing. Called from
     * {@link #initializeJsonRequest(com.google.api.client.googleapis.services.json.AbstractGoogleJsonClientRequest)}.
     * </p>
     *
     * @throws java.io.IOException I/O exception
     */
    protected void initializeBackendRequest(BackendRequest<?> request) throws java.io.IOException {
  }
}
