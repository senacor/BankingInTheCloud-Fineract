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

import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.mifos.anubis.api.v1.TokenConstants;
import io.mifos.anubis.api.v1.domain.TokenContent;
import io.mifos.core.lang.security.RsaKeyPairFactory;
import io.mifos.core.test.domain.TimeStampChecker;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;

/**
 * @author Myrle Krantz, Markus Geiss
 */
public class TenantAccessTokenSerializerTest {

  private static final int SECONDS_TO_LIVE = 15;
  private static final String USER = "who";
  private static final TokenContent EXAMPLE_TOKEN_CONTENT
      = new TokenContent(Collections.emptyList());
  private static RsaKeyPairFactory.KeyPairHolder keyPairHolder;

  @BeforeClass
  public static void initialize()
  {
    keyPairHolder = RsaKeyPairFactory.createKeyPair();
  }

  @SuppressWarnings({"unchecked"})
  @Test
  public void shouldCreateValidAccessToken() throws Exception
  {
    final TenantAccessTokenSerializer.Specification specification
        = new TenantAccessTokenSerializer.Specification()
            .setKeyTimestamp("1234567")
        .setUser(USER)
        .setSourceApplication("doo-v1")
        .setTokenContent(EXAMPLE_TOKEN_CONTENT)
        .setPrivateKey(keyPairHolder.privateKey())
        .setSecondsToLive(SECONDS_TO_LIVE);

    final TenantAccessTokenSerializer testSubject = new TenantAccessTokenSerializer(new Gson());

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
    Assert.assertEquals(TokenType.TENANT.getIssuer(), parsedToken.getBody().get("iss"));
    Assert.assertEquals(USER, parsedToken.getBody().get("sub"));
    final Integer issued = (Integer) parsedToken.getBody().get("iat");
    Assert.assertNotNull(issued);
    final Integer expires = (Integer) parsedToken.getBody().get("exp");
    Assert.assertNotNull(expires);
    Assert.assertTrue(expires > issued);
    final String timestamp = parsedToken.getBody().get(TokenConstants.JWT_SIGNATURE_TIMESTAMP_CLAIM, String.class);
    Assert.assertEquals("1234567", timestamp);
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidSecondsToLiveCausesException()
  {
    final TenantAccessTokenSerializer.Specification specification
        = new TenantAccessTokenSerializer.Specification()
        .setUser(USER)
        .setSourceApplication("doo-v1")
        .setTokenContent(EXAMPLE_TOKEN_CONTENT)
        .setPrivateKey(keyPairHolder.privateKey())
        .setSecondsToLive(0);

    final TenantAccessTokenSerializer testSubject = new TenantAccessTokenSerializer(new Gson());

    testSubject.build(specification);
  }
}