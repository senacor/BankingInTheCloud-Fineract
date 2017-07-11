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
package io.mifos.anubis.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.mifos.anubis.api.v1.TokenConstants;
import io.mifos.core.lang.security.RsaKeyPairFactory;
import io.mifos.core.test.domain.TimeStampChecker;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author Myrle Krantz, Markus Geiss
 */
public class SystemAccessTokenSerializerTest {



  public SystemAccessTokenSerializerTest() {
    super();
  }

  private static final String APPLICATION_NAME = "mifosio-core";
  private static final String TEST_TENANT = "token_test";
  private static final String ROLE = "y";
  private static final int SECONDS_TO_LIVE = 15;
  private static RsaKeyPairFactory.KeyPairHolder keyPairHolder;

  @BeforeClass
  public static void initialize()
  {
    keyPairHolder = RsaKeyPairFactory.createKeyPair();
  }

  @SuppressWarnings({"unchecked"})
  @Test
  public void shouldCreateValidSystemToken() throws Exception {

    final SystemAccessTokenSerializer.Specification specification
        = new SystemAccessTokenSerializer.Specification()
            .setKeyTimestamp("123456")
            .setTargetApplicationName(APPLICATION_NAME)
            .setPrivateKey(keyPairHolder.privateKey())
            .setRole(ROLE)
            .setSecondsToLive(SECONDS_TO_LIVE)
            .setTenant(TEST_TENANT);

    final SystemAccessTokenSerializer testSubject = new SystemAccessTokenSerializer();

    final TimeStampChecker timeStampChecker = TimeStampChecker.inTheFuture(Duration.ofSeconds(SECONDS_TO_LIVE));
    final TokenSerializationResult systemToken = testSubject.build(specification);

    Assert.assertNotNull(systemToken);

    final LocalDateTime expiration = systemToken.getExpiration();
    timeStampChecker.assertCorrect(expiration);

    final Jwt<Header, Claims> parsedToken = Jwts
        .parser()
        .setSigningKey(keyPairHolder.publicKey())
        .parse(systemToken.getToken().substring("Bearer ".length()).trim());


    Assert.assertNotNull(parsedToken);
    Assert.assertEquals(TokenType.SYSTEM.getIssuer(), parsedToken.getBody().get("iss"));
    Assert.assertEquals(APPLICATION_NAME, parsedToken.getBody().get("aud"));
    Assert.assertEquals(TEST_TENANT, parsedToken.getBody().get("sub"));
    final Integer issued = (Integer) parsedToken.getBody().get("iat");
    Assert.assertNotNull(issued);
    final Integer expires = (Integer) parsedToken.getBody().get("exp");
    Assert.assertNotNull(expires);
    Assert.assertTrue(expires > issued);
    final String timestamp = parsedToken.getBody().get(TokenConstants.JWT_SIGNATURE_TIMESTAMP_CLAIM, String.class);
    Assert.assertEquals("123456", timestamp);
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidSecondsToLiveCausesException()
  {
    final SystemAccessTokenSerializer.Specification specification
        = new SystemAccessTokenSerializer.Specification()
        .setTargetApplicationName(APPLICATION_NAME)
        .setPrivateKey(keyPairHolder.privateKey())
        .setRole("y")
        .setSecondsToLive(0)
        .setTenant(TEST_TENANT);

    final SystemAccessTokenSerializer testSubject = new SystemAccessTokenSerializer();

    testSubject.build(specification);

  }
}
