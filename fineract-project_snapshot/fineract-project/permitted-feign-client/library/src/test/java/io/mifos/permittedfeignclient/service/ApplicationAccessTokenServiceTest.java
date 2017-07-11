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
package io.mifos.permittedfeignclient.service;

import io.mifos.anubis.config.TenantSignatureRepository;
import io.mifos.anubis.token.TenantRefreshTokenSerializer;
import io.mifos.anubis.token.TokenSerializationResult;
import io.mifos.core.lang.ApplicationName;
import io.mifos.core.lang.AutoTenantContext;
import io.mifos.core.lang.TenantContextHolder;
import io.mifos.core.lang.security.RsaKeyPairFactory;
import io.mifos.identity.api.v1.client.IdentityManager;
import io.mifos.identity.api.v1.domain.Authentication;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author Myrle Krantz
 */
public class ApplicationAccessTokenServiceTest {
  private static final String APP_NAME = "app-v1";
  private static final String BEARER_TOKEN_MOCK = "bearer token mock";
  private static final String USER_NAME = "user";
  private static final String TENANT_NAME = "tenant";

  @Test
  public void testHappyCase() {
    final ApplicationName applicationNameMock = Mockito.mock(ApplicationName.class);
    Mockito.when(applicationNameMock.toString()).thenReturn(APP_NAME);

    final TenantSignatureRepository tenantSignatureRepositoryMock = Mockito.mock(TenantSignatureRepository.class);
    final Optional<RsaKeyPairFactory.KeyPairHolder> keyPair = Optional.of(RsaKeyPairFactory.createKeyPair());
    Mockito.when(tenantSignatureRepositoryMock.getLatestApplicationSigningKeyPair()).thenReturn(keyPair);

    final IdentityManager identityManagerMock = Mockito.mock(IdentityManager.class);
    Mockito.when(identityManagerMock.refresh(Mockito.anyString()))
            .thenReturn(new Authentication(BEARER_TOKEN_MOCK, "accesstokenexpiration", "refreshtokenexpiration", null));

    final TenantRefreshTokenSerializer tenantRefreshTokenSerializerMock = Mockito.mock(TenantRefreshTokenSerializer.class);
    Mockito.when(tenantRefreshTokenSerializerMock.build(Mockito.anyObject()))
            .thenReturn(new TokenSerializationResult(BEARER_TOKEN_MOCK, LocalDateTime.now()));

    final Logger loggerMock = Mockito.mock(Logger.class);

    final ApplicationAccessTokenService testSubject = new ApplicationAccessTokenService(
            applicationNameMock,
            tenantSignatureRepositoryMock,
            identityManagerMock,
            tenantRefreshTokenSerializerMock,
            loggerMock);

    try (final AutoTenantContext ignored1 = new AutoTenantContext(TENANT_NAME)) {
      final String accessTokenWithoutCallEndpointSet = testSubject.getAccessToken(USER_NAME, TenantContextHolder.checkedGetIdentifier());
      Assert.assertEquals(BEARER_TOKEN_MOCK, accessTokenWithoutCallEndpointSet);

      final String accessToken = testSubject.getAccessToken(USER_NAME, TenantContextHolder.checkedGetIdentifier(), "blah");
      Assert.assertEquals(BEARER_TOKEN_MOCK, accessToken);

      final String accessTokenAgain = testSubject.getAccessToken(USER_NAME, TenantContextHolder.checkedGetIdentifier(), "blah");
      Assert.assertEquals(BEARER_TOKEN_MOCK, accessTokenAgain);
    }
  }
}