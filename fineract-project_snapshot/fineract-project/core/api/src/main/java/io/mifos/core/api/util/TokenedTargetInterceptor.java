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

import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings("WeakerAccess")
public class TokenedTargetInterceptor implements RequestInterceptor {

  @Override
  public void apply(final RequestTemplate template) {
    UserContextHolder.getUserContext()
            .map(UserContext::getAccessToken)
            .ifPresent(token -> template.header(ApiConstants.AUTHORIZATION_HEADER, token));
    UserContextHolder.getUserContext()
            .map(UserContext::getUser)
            .ifPresent(user -> template.header(ApiConstants.USER_HEADER, user));
  }
}
