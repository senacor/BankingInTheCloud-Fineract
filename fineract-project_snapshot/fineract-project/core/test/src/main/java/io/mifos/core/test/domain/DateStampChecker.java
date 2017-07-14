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
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Support class for testing that the correct time stamp is returned
 *
 * @author Myrle Krantz
 */
@SuppressWarnings("WeakerAccess")
public class DateStampChecker {
  private final LocalDate expectedDateStamp;
  private final int maximumDelta;

  public static DateStampChecker inTheFuture(final int offset)
  {
    return new DateStampChecker(LocalDate.now(Clock.systemUTC()).plusDays(offset), 0);
  }

  private DateStampChecker(final LocalDate expectedDateStamp, final int maximumDelta) {
    this.expectedDateStamp = expectedDateStamp;
    this.maximumDelta = maximumDelta;
  }

  public void assertCorrect(final String dateStamp)
  {
    Assert.assertTrue("Delta from expected should have been less than " +
                    maximumDelta + ". Timestamp string was " + dateStamp + ".",
            isCorrect(dateStamp));
  }

  public boolean isCorrect(final String dateStamp) {
    final LocalDate parsedDateStamp = DateConverter.dateFromIsoString(dateStamp);

    final long deltaFromExpected = parsedDateStamp.until(expectedDateStamp, ChronoUnit.DAYS);

    return deltaFromExpected <= maximumDelta;
  }
}
