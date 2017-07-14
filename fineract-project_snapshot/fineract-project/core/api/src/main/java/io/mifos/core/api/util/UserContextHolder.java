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

import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class UserContextHolder {

  private static final InheritableThreadLocal<UserContext> THREAD_LOCAL = new InheritableThreadLocal<>();

  private UserContextHolder() {
  }

  @Nonnull
  public static String checkedGetAccessToken() {
    return Optional.ofNullable(UserContextHolder.THREAD_LOCAL.get())
            .map(UserContext::getAccessToken)
            .orElseThrow(IllegalStateException::new);
  }

  @Nonnull
  public static String checkedGetUser() {
    return Optional.ofNullable(UserContextHolder.THREAD_LOCAL.get())
            .map(UserContext::getUser)
            .map(UserContextHolder::cropIdentifier)
            .orElseThrow(IllegalStateException::new);
  }

  private static String cropIdentifier(final String identifier) {
    if (identifier.length() > 32)
      return identifier.substring(0, 32);
    else
      return identifier;
  }

  @Nonnull
  public static Optional<UserContext> getUserContext() {
    return Optional.ofNullable(UserContextHolder.THREAD_LOCAL.get());
  }

  public static void setAccessToken(@Nonnull final String user, @Nonnull final String accessToken) {
    Assert.notNull(user, "User may not be null.");
    Assert.notNull(accessToken, "Access token may not be null.");
    UserContextHolder.THREAD_LOCAL.set(new UserContext(user, accessToken));
  }

  public static void setUserContext(@Nonnull final UserContext userContext)
  {
    UserContextHolder.THREAD_LOCAL.set(userContext);
  }

  public static void clear() {
    UserContextHolder.THREAD_LOCAL.remove();
  }
}
