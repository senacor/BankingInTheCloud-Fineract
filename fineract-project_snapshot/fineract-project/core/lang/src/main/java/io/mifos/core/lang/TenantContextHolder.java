/*
 * Copyright 2017 The Mifos Initiative
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
package io.mifos.core.lang;

import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import java.util.Optional;

public final class TenantContextHolder {

  private static final InheritableThreadLocal<String> THREAD_LOCAL = new InheritableThreadLocal<>();

  private TenantContextHolder() {
    super();
  }

  @Nonnull
  public static Optional<String> identifier() {
    return Optional.ofNullable(TenantContextHolder.THREAD_LOCAL.get());
  }

  @SuppressWarnings("WeakerAccess")
  public static String checkedGetIdentifier() {
    return identifier().orElseThrow(() -> new IllegalStateException("Tenant context not set."));
  }

  public static void setIdentifier(@Nonnull final String identifier) {
    Assert.notNull(identifier, "A tenant identifier must be given.");
    Assert.hasLength(identifier, "A tenant identifier must have at least one character.");
    Assert.isNull(TenantContextHolder.THREAD_LOCAL.get(), "Tenant identifier already set.");
    TenantContextHolder.THREAD_LOCAL.set(identifier);
  }

  public static void clear() {
    TenantContextHolder.THREAD_LOCAL.remove();
  }
}
