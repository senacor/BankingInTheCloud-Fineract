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

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Support class for testing that the correct time stamp is returned
 *
 * @author Myrle Krantz
 */
@SuppressWarnings("WeakerAccess")
public class TimeStampChecker {
  private static final int DEFAULT_MAXIMUM_DELTA = 2;
  private final LocalDateTime expectedTimeStamp;
  private final Duration maximumDelta;

  public static TimeStampChecker roughlyNow()
  {
    return new TimeStampChecker(LocalDateTime.now(Clock.systemUTC()), Duration.ofSeconds(DEFAULT_MAXIMUM_DELTA));
  }

  public static TimeStampChecker inTheFuture(final Duration offset)
  {
    return inTheFutureWithWiggleRoom(offset, Duration.ofSeconds(DEFAULT_MAXIMUM_DELTA));
  }

  public static TimeStampChecker inTheFutureWithWiggleRoom(final Duration offset, final Duration maximumDelta)
  {
    return new TimeStampChecker(LocalDateTime.now(Clock.systemUTC()).plus(offset), maximumDelta);
  }

  public static TimeStampChecker allowSomeWiggleRoom(final Duration maximumDelta)
  {
    return new TimeStampChecker(LocalDateTime.now(Clock.systemUTC()), maximumDelta);
  }

  private TimeStampChecker(final LocalDateTime expectedTimeStamp, final Duration maximumDelta) {
    this.expectedTimeStamp = expectedTimeStamp;
    this.maximumDelta = maximumDelta;
  }

  public void assertCorrect(final String timeStamp)
  {
    Assert.assertTrue("Delta from expected should have been less than " +
                    maximumDelta + ". Timestamp string was " + timeStamp + ".",
            isCorrect(timeStamp));
  }

  public boolean isCorrect(final String timeStamp) {
    final LocalDateTime parsedTimeStamp = DateConverter.fromIsoString(timeStamp);
    return isCorrect(parsedTimeStamp);
  }

  public void assertCorrect(final LocalDateTime localDateTime)
  {
    Assert.assertTrue("Delta from expected should have been less than " +
                    maximumDelta + ". LocalDateTime was " + localDateTime + ".",
            isCorrect(localDateTime));
  }

  public boolean isCorrect(final LocalDateTime localDateTime) {

    final Duration deltaFromExpected = Duration.ofNanos(Math.abs(
            localDateTime.until(expectedTimeStamp, ChronoUnit.NANOS)));

    return deltaFromExpected.compareTo(maximumDelta) < 0;
  }
}