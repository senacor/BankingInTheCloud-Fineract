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
package io.mifos.core.lang;

import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author Myrle Krantz
 */
public class DateConverterTest {
  private final static Instant momentInTime = Instant.ofEpochSecond(1481281507);

  @Test
  public void dateToIsoString() throws Exception {
    final Date date = Date.from(momentInTime);
    final String isoString = DateConverter.toIsoString(date);
    Assert.assertEquals("2016-12-09T11:05:07Z", isoString);
  }

  @Test
  public void dateFromIsoString() throws Exception {
    final LocalDate date = DateConverter.dateFromIsoString("2017-07-13Z");
    final String isoString = DateConverter.toIsoString(date);
    Assert.assertEquals("2017-07-13Z", isoString);
  }

  @Test
  public void localDateTimeToIsoString() throws Exception {
    final LocalDateTime localDateTime = LocalDateTime.ofInstant(momentInTime, ZoneId.of("UTC"));
    final String isoString = DateConverter.toIsoString(localDateTime);
    Assert.assertEquals("2016-12-09T11:05:07Z", isoString);

    final LocalDateTime roundTrip = DateConverter.fromIsoString(isoString);
    Assert.assertEquals(localDateTime, roundTrip);
  }

  @Test
  public void localDateTimeToEpochMillis() throws Exception {
    final LocalDateTime localDateTime = LocalDateTime.ofInstant(momentInTime, ZoneId.of("UTC"));
    final Long epochMillis = DateConverter.toEpochMillis(localDateTime);
    Assert.assertEquals(Long.valueOf(1481281507000L), epochMillis);

    final LocalDateTime roundTrip = DateConverter.fromEpochMillis(epochMillis);
    Assert.assertEquals(localDateTime, roundTrip);
  }

  @Test
  public void localDateToIsoString() throws Exception {
    final LocalDateTime localDateTime = LocalDateTime.ofInstant(momentInTime, ZoneId.of("UTC"));
    final LocalDate localDate = DateConverter.toLocalDate(localDateTime);
    final String isoString = DateConverter.toIsoString(localDate);
    Assert.assertEquals("2016-12-09Z", isoString);
  }

  @Test
  public void localDateToEpochDay() throws Exception {
    final LocalDateTime localDateTime = LocalDateTime.ofInstant(momentInTime, ZoneId.of("UTC"));
    final LocalDate localDate = DateConverter.toLocalDate(localDateTime);
    final Long epochDay = DateConverter.toEpochDay(localDate);
    Assert.assertEquals(Long.valueOf(17144), epochDay);
  }
}