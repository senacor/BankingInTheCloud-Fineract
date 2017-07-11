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
package io.mifos.core.lang.security;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.security.PublicKey;

/**
 * @author Myrle Krantz
 */
public class RsaPublicKeyBuilderTest {

  @Test
  public void shouldReadPublicKeyCorrectly() {
    final RsaKeyPairFactory.KeyPairHolder keyPair = RsaKeyPairFactory.createKeyPair();
    final BigInteger exp = keyPair.getPublicKeyExp();
    final BigInteger mod = keyPair.getPublicKeyMod();

    final PublicKey publicKey =
        new RsaPublicKeyBuilder().setPublicKeyExp(exp).setPublicKeyMod(mod).build();

    Assert.assertEquals(publicKey, keyPair.publicKey());
  }

  @Test(expected = IllegalStateException.class)
  public void shouldFailToReadBrokenPublicKey() {
    final BigInteger exp = BigInteger.valueOf(-1);
    final BigInteger mod = BigInteger.valueOf(-1);

    new RsaPublicKeyBuilder().setPublicKeyExp(exp).setPublicKeyMod(mod).build();
  }
}
