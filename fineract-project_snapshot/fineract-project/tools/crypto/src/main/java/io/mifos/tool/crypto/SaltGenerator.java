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

import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import javax.annotation.Nonnull;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Component
public final class SaltGenerator {

  public SaltGenerator() {
    super();
  }

  @Nonnull
  public byte[] createRandomSalt() {
    try {
      final SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
      final byte[] salt = new byte[32];
      secureRandom.nextBytes(salt);
      return Base64Utils.encode(salt);
    } catch (final NoSuchAlgorithmException nsaex) {
      throw new IllegalStateException(nsaex);
    }
  }
}
