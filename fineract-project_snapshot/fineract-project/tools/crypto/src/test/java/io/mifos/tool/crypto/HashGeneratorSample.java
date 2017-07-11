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

import io.mifos.tool.crypto.config.EnableCrypto;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.util.EncodingUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.util.Base64Utils;

import java.nio.file.Files;
import java.nio.file.Paths;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
    classes = {
        HashGeneratorSample.SampleSpringConfiguration.class
    },
    loader = AnnotationConfigContextLoader.class
)
public class HashGeneratorSample {

  @Configuration
  @EnableCrypto
  public static class SampleSpringConfiguration {

    public SampleSpringConfiguration() {
      super();
    }
  }

  private static final String USERNAME = "nebuchadnezzar";
  private static final String TENANT = "sippar";
  private static final String DOMAIN = "babylon.ad";
  private static final String PASSWORD = "m4duk";
  private static final int ITERATION_COUNT = 4096;
  private static final int HASH_LENGTH = 256;

  @Autowired
  private SaltGenerator saltGenerator;

  @Autowired
  private HashGenerator hashGenerator;

  public HashGeneratorSample() {
    super();
  }

  @Test
  public void clientPasswordHashSpecification() throws Exception {
    // 1. create user specific salt, username+tenant+TLD, Base64 URL encoded
    final StringBuilder saltBuilder = new StringBuilder();
    saltBuilder.append(USERNAME).append(TENANT).append(DOMAIN);
    final byte[] salt = Base64Utils.encode(saltBuilder.toString().getBytes());

    // 2. encode user password URL safe with Base64
    final String encodedPassword = Base64Utils.encodeToString(PASSWORD.getBytes());

    // 3. create user password hash, to be send to service
    final byte[] hash = this.hashGenerator.hash(encodedPassword, salt, ITERATION_COUNT, HASH_LENGTH);

    Assert.assertNotNull(hash);
    Assert.assertTrue(hash.length > 0);
  }

  @Test
  public void servicePasswordHashSpecification() throws Exception {
    // 0. retrieve user password
    final byte[] password = Files.readAllBytes(
        Paths.get(getClass().getClassLoader()
            .getResource(".password")
            .toURI()));

    // 1. read stored secret
    final byte[] storedSecret = Files.readAllBytes(
        Paths.get(getClass().getClassLoader()
            .getResource(".secret")
            .toURI()));

    // 2. create variable salt, to be stored with user info
    final byte[] variableSalt = this.saltGenerator.createRandomSalt();

    // 3. concatenate variable salt and secret to create unique salt for this user
    final byte[] salt = EncodingUtils.concatenate(variableSalt, storedSecret);

    // 4. create hash to be stored with user info
    final byte[] hash = this.hashGenerator.hash(Base64Utils.encodeToString(password), salt, ITERATION_COUNT, HASH_LENGTH);
    Assert.assertNotNull(hash);
    Assert.assertTrue(hash.length > 0);
  }

  @Test
  public void clientPasswordIsEqual() throws Exception {
    // 0. retrieve user password
    final byte[] password = Files.readAllBytes(
        Paths.get(getClass().getClassLoader()
            .getResource(".password")
            .toURI()));

    // 1. read stored secret
    final byte[] storedSecret = Files.readAllBytes(
        Paths.get(getClass().getClassLoader()
            .getResource(".secret")
            .toURI()));

    // 2. read stored variable salt from user info
    final byte[] storedSalt = Files.readAllBytes(
        Paths.get(getClass().getClassLoader()
            .getResource(".salt")
            .toURI()));

    // 3. read stored hash from user info
    final byte[] storedHash = Files.readAllBytes(
        Paths.get(getClass().getClassLoader()
            .getResource(".hash")
            .toURI()));

    // 4. check if password is equal with stored hash
    Assert.assertTrue(this.hashGenerator.isEqual(storedHash, password, storedSecret, storedSalt, ITERATION_COUNT, HASH_LENGTH));
  }
}
