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
package io.mifos.rhythm.service.internal.service;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author Myrle Krantz
 */
public class DrummerTest {

  @Test
  public void incrementToAlignment() {
    final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
    final LocalDateTime tomorrow = Drummer.incrementToAlignment(now, 3);

    Assert.assertEquals(tomorrow.minusDays(1).truncatedTo(ChronoUnit.DAYS), now.truncatedTo(ChronoUnit.DAYS));
    Assert.assertEquals(3, tomorrow.getHour());
  }

  @Test
  public void getNumberOfBeatPublishesNeeded() {
    final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
    final long eventsNeeded3 = Drummer.getNumberOfBeatPublishesNeeded(now, now.minus(3, ChronoUnit.DAYS));
    Assert.assertEquals(3, eventsNeeded3);

    final long eventsNeededPast = Drummer.getNumberOfBeatPublishesNeeded(now, now.plus(1, ChronoUnit.DAYS));
    Assert.assertEquals(0, eventsNeededPast);

    final long eventsNeededNow = Drummer.getNumberOfBeatPublishesNeeded(now, now.minus(2, ChronoUnit.MINUTES));
    Assert.assertEquals(1, eventsNeededNow);
  }

  @Test
  public void checkBeatForPublishAllBeatsSucceed() {
    final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
    final Optional<LocalDateTime> ret = Drummer.checkBeatForPublishHelper(now, 0, now.minus(3, ChronoUnit.DAYS), x -> true);
    Assert.assertEquals(Optional.of(Drummer.incrementToAlignment(now, 0)), ret);
  }

  @Test
  public void checkBeatForPublishFirstFails() {
    final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
    final LocalDateTime nextBeat = now.minus(3, ChronoUnit.DAYS);
    @SuppressWarnings("unchecked") final Predicate<LocalDateTime> produceBeatsMock = Mockito.mock(Predicate.class);
    Mockito.when(produceBeatsMock.test(nextBeat)).thenReturn(false);
    final Optional<LocalDateTime> ret = Drummer.checkBeatForPublishHelper(now, 0, nextBeat, produceBeatsMock);
    Assert.assertEquals(Optional.of(nextBeat), ret);
  }

  @Test
  public void checkBeatForPublishSecondFails() {
    final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
    final LocalDateTime nextBeat = now.minus(3, ChronoUnit.DAYS);
    final LocalDateTime secondBeat = Drummer.incrementToAlignment(nextBeat, 0);
    @SuppressWarnings("unchecked") final Predicate<LocalDateTime> produceBeatsMock = Mockito.mock(Predicate.class);
    Mockito.when(produceBeatsMock.test(nextBeat)).thenReturn(true);
    Mockito.when(produceBeatsMock.test(secondBeat)).thenReturn(false);
    final Optional<LocalDateTime> ret = Drummer.checkBeatForPublishHelper(now, 0, nextBeat, produceBeatsMock);
    Assert.assertEquals(Optional.of(secondBeat), ret);
  }

  @Test
  public void checkBeatForPublishNoneNeeded() {
    final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
    final Optional<LocalDateTime> ret = Drummer.checkBeatForPublishHelper(now, 0, now.plus(1, ChronoUnit.DAYS),
            x -> { Assert.fail("Pubish shouldn't be called"); return true; });
    Assert.assertEquals(Optional.empty(), ret);
  }
}