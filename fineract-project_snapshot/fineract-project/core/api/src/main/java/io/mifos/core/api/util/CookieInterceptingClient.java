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

import feign.*;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Keeps the cookies for this client and appends them to requests.
 * See also CookieInterceptor.
 *
 * @author Myrle Krantz
 */
class CookieInterceptingClient extends Client.Default {
  final CookieManager cookieManager;
  private final String target;

  CookieInterceptingClient(final String target) {
    this(target, new CookieManager());
  }

  CookieInterceptingClient(final String target, final CookieManager cookieManager)
  {
    super(null, null);
    this.cookieManager = cookieManager;
    this.target = target;
  }

  RequestInterceptor getCookieInterceptor() {
    return new CookieInterceptor();
  }

  void putCookie(final String relativeUrl, final String cookieName, final String cookieValue) {
    try {
      final Map<String, List<String>> map = new HashMap<>();
      map.put("Set-Cookie", Collections.singletonList(cookieName + "=" + cookieValue));
      cookieManager.put(mapUriType(target + relativeUrl), map);
    } catch (final IOException e) {
      throw new IllegalStateException("Mapping cookies failed unexpectedly.");
    }
  }

  private class CookieInterceptor implements RequestInterceptor {
    @Override
    public void apply(final RequestTemplate template) {
      try {
        final Map<String, List<String>> cookieHeaders =
                cookieManager.get(mapUriType(target + template.url()), mapHeadersType(template.headers()));
        cookieHeaders.entrySet().forEach(entry -> template.header(entry.getKey(), entry.getValue()));
      } catch (final IOException e) {
        throw new IllegalStateException("Mapping cookies failed unexpectedly.");
      }
    }
  }

  /**
   * Seam for testing
   */
  Response superExecute(final Request request, final Request.Options options) throws IOException {
    return super.execute(request, options);
  }

  @Override
  public Response execute(final Request request, final Request.Options options) throws IOException {
    final Response ret = superExecute(request, options);
    cookieManager.put(mapUriType(request.url()), mapHeadersType(ret.headers()));
    return ret;
  }

  private static URI mapUriType(final String url) {
    return URI.create(url);
  }

  private static Map<String, List<String>> mapHeadersType(final Map<String, Collection<String>> headers) {
    final HashMap<String, List<String>> ret = new HashMap<>();
    headers.entrySet().forEach(entry ->
            ret.put(entry.getKey(), changeCollectionToList(entry.getValue())));
    return ret;
  }

  private static List<String> changeCollectionToList(final Collection<String> value) {
    return value.stream().collect(Collectors.toList());
  }
}