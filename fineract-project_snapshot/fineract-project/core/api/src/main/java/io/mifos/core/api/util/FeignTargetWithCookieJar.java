/*
 * Copyright 2017 The Mifos Initiative.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mifos.core.api.util;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class FeignTargetWithCookieJar<T> {
  private final T feignTarget;
  private final CookieInterceptingClient cookieInterceptor;

  FeignTargetWithCookieJar(final T feignTarget, final CookieInterceptingClient cookieInterceptor) {
    this.feignTarget = feignTarget;
    this.cookieInterceptor = cookieInterceptor;
  }

  public void putCookie(final String relativeUrl, final String cookieName, final String cookieValue) {
    this.cookieInterceptor.putCookie(relativeUrl, cookieName, cookieValue);
  }

  public T getFeignTarget() {
    return feignTarget;
  }
}
