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
package io.mifos.anubis.api.v1.validation;

import io.mifos.core.lang.DateConverter;
import org.junit.Assert;
import org.junit.Test;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * @author Myrle Krantz
 */
public class CheckKeyTimestampTest {
  @Test
  public void testValid()
  {
    final CheckKeyTimestamp testSubject = new CheckKeyTimestamp();

    String utcNowAsString = DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC()));
    Assert.assertTrue(testSubject.isValid(utcNowAsString, null));
  }

  @Test
  public void testNull()
  {
    final CheckKeyTimestamp testSubject = new CheckKeyTimestamp();

    Assert.assertFalse(testSubject.isValid(null, null));
  }


  @Test
  public void testGobbledyGook()
  {
    final CheckKeyTimestamp testSubject = new CheckKeyTimestamp();

    Assert.assertFalse(testSubject.isValid("gobbledygook", null));
  }

  @Test
  public void testInitializeDoesntThrowException()
  {
    final CheckKeyTimestamp testSubject = new CheckKeyTimestamp();

    testSubject.initialize(null);
  }
}