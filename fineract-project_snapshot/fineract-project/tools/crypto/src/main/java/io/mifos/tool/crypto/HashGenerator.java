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
package io.mifos.tool.crypto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.util.EncodingUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@Component
public class HashGenerator {

  private final Environment environment;

  @Autowired
  public HashGenerator(final Environment environment) {
    super();
    this.environment = environment;
  }

  @Nonnull
  public byte[] hash(@Nonnull final String password, @Nonnull final byte[] salt,
                     @Nonnegative final int iterationCount, @Nonnegative final int length) {
    Assert.notNull(password, "Password must be given!");
    Assert.notNull(salt, "Salt must be given!");
    Assert.isTrue(iterationCount > 0, "Iteration count must be greater than zero!");
    Assert.isTrue(length > 0, "Length must be greater than zero!");

    try {
      final PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, length);
      final SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      final byte[] encodedHash = secretKeyFactory.generateSecret(pbeKeySpec).getEncoded();
      return encodedHash;
    } catch (final NoSuchAlgorithmException | InvalidKeySpecException ex) {
      throw new IllegalStateException(ex);
    }
  }

  public boolean isEqual(@Nonnull final byte[] knownHash, @Nonnull final byte[] password, @Nonnull final byte[] secret,
                         @Nonnull final byte[] salt, @Nonnegative final int iterationCount, @Nonnegative final int length) {
    Assert.notNull(knownHash, "Known hash must be given!");
    Assert.notNull(password, "Password must be given!");
    Assert.notNull(salt, "Salt must be given!");
    Assert.isTrue(iterationCount > 0, "Iteration count must be greater than zero!");
    Assert.isTrue(length > 0, "Length must be greater than zero!");
    final byte[] internalSalt = EncodingUtils.concatenate(salt, secret);

    final byte[] computedHash = this.hash(Base64Utils.encodeToString(password), internalSalt, iterationCount, length);

    return MessageDigest.isEqual(knownHash, computedHash);
  }
}
