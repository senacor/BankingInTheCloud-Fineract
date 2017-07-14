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
package io.mifos.core.api.context;

import io.mifos.core.api.util.UserContext;
import io.mifos.core.api.util.UserContextHolder;

import java.util.Optional;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "WeakerAccess"})
public class AutoUserContext implements AutoCloseable {
  private final Optional<UserContext> previousUserContext;

  public AutoUserContext(final String userName, final String accessToken) {
    previousUserContext = UserContextHolder.getUserContext();

    UserContextHolder.setAccessToken(userName, accessToken);
  }

  @Override public void close() {
    UserContextHolder.clear();
    previousUserContext.ifPresent(UserContextHolder::setUserContext);
  }
}