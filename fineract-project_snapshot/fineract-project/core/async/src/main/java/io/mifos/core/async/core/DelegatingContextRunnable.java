package io.mifos.core.async.core;

import io.mifos.core.api.util.UserContext;
import io.mifos.core.api.util.UserContextHolder;
import io.mifos.core.lang.TenantContextHolder;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class DelegatingContextRunnable implements Runnable {

  private final Runnable delegate;
  private final Optional<String> optionalTenantIdentifier;
  private final Optional<UserContext> optionalUserContext;

  DelegatingContextRunnable(final Runnable delegate, final String tenantIdentifier,
                            final UserContext userContext) {
    super();
    this.delegate = delegate;
    this.optionalTenantIdentifier = Optional.ofNullable(tenantIdentifier);
    this.optionalUserContext = Optional.ofNullable(userContext);
  }

  @Override
  public void run() {
    try {
      TenantContextHolder.clear();
      optionalTenantIdentifier.ifPresent(TenantContextHolder::setIdentifier);

      UserContextHolder.clear();
      optionalUserContext.ifPresent(UserContextHolder::setUserContext);

      this.delegate.run();
    } finally {
      TenantContextHolder.clear();
      UserContextHolder.clear();
    }
  }
}
