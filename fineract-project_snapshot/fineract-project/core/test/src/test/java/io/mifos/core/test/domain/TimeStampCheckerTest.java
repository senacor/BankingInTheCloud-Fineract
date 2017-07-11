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
package io.mifos.core.test.domain;

import io.mifos.core.lang.DateConverter;
import org.junit.Assert;
import org.junit.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 *
 * @author Myrle Krantz
 */
public class TimeStampCheckerTest {
  @Test
  public void roughlyNow() throws Exception {
    final TimeStampChecker checker = TimeStampChecker.roughlyNow();
    final LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
    final String nowAsString = DateConverter.toIsoString(now);
    checker.assertCorrect(nowAsString);

    final LocalDateTime fiveSecondsAgo = now.minus(5, ChronoUnit.SECONDS);
    final String fiveSecondsAgoAsString = DateConverter.toIsoString(fiveSecondsAgo);
    Assert.assertFalse(checker.isCorrect(fiveSecondsAgoAsString));
  }

  @Test
  public void inTheFuture() throws Exception {
    final TimeStampChecker checker = TimeStampChecker.inTheFuture(Duration.ofMinutes(5));
    final LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
    final LocalDateTime fiveMinutesFromNow = now.plus(5, ChronoUnit.MINUTES);
    final String fiveMinutesFromNowAsString = DateConverter.toIsoString(fiveMinutesFromNow);
    checker.assertCorrect(fiveMinutesFromNowAsString);

    final String nowAsString = DateConverter.toIsoString(now);
    Assert.assertFalse(checker.isCorrect(nowAsString));

  }

  @Test
  public void allowSomeWiggleRoom() throws Exception {
    final TimeStampChecker checker = TimeStampChecker.allowSomeWiggleRoom(Duration.ofSeconds(30));
    final LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
    final LocalDateTime roughlyNow = now.minus(25, ChronoUnit.SECONDS);
    final String roughlyNowAsString = DateConverter.toIsoString(roughlyNow);
    checker.assertCorrect(roughlyNowAsString);

    final LocalDateTime fiveMinutesFromNow = now.plus(5, ChronoUnit.MINUTES);
    final String fiveMinutesFromNowAsString = DateConverter.toIsoString(fiveMinutesFromNow);
    Assert.assertFalse(checker.isCorrect(fiveMinutesFromNowAsString));

  }

  @Test
  public void justLocalDateTime() throws Exception {
    final TimeStampChecker checker = TimeStampChecker.roughlyNow();
    final LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
    checker.assertCorrect(now);
  }

}