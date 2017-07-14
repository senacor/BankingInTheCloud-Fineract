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

import java.time.LocalDate;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class DateOfBirth {

  private Integer year;
  private Integer month;
  private Integer day;

  public DateOfBirth() {
    super();
  }

  public static DateOfBirth fromLocalDate(final LocalDate localDate) {
    final DateOfBirth dateOfBirth = new DateOfBirth();
    dateOfBirth.setYear(localDate.getYear());
    dateOfBirth.setMonth(localDate.getMonthValue());
    dateOfBirth.setDay(localDate.getDayOfMonth());
    return dateOfBirth;
  }

  public Integer getYear() {
    return this.year;
  }

  public void setYear(final Integer year) {
    this.year = year;
  }

  public Integer getMonth() {
    return this.month;
  }

  public void setMonth(final Integer month) {
    this.month = month;
  }

  public Integer getDay() {
    return this.day;
  }

  public void setDay(final Integer day) {
    this.day = day;
  }

  public LocalDate toLocalDate() {
    return LocalDate.of(
        this.year,
        this.month != null ? this.month : 1,
        this.day != null ? this.day : 1
    );
  }
}
