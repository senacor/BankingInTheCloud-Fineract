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

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings("WeakerAccess")
public class ApplicationName {
  final private String serviceName;
  final private String versionString;

  private ApplicationName(final String serviceName, final String versionString) {
    this.serviceName = serviceName;
    this.versionString = versionString;
  }

  public static ApplicationName fromSpringApplicationName(final String springApplicationName) {
    return parse(springApplicationName);
  }

  public static ApplicationName appNameWithVersion(final String serviceName, final String versionString) {
    return new ApplicationName(serviceName, versionString);
  }

  static ApplicationName parse(final String springApplicationNameString) {
    if (springApplicationNameString.length() > 64) {
      throw new IllegalArgumentException("Spring application name strings for mifos io applications should be 64 characters or less.");
    }

    final Pattern applicationNamePattern = Pattern.compile(
        "^(/??(?<name>\\p{Lower}[\\p{Lower}_]+)(?:-v(?<version>\\d[\\d\\._]*))?)$");

    final Matcher applicationNameMatcher = applicationNamePattern.matcher(springApplicationNameString);
    if (!applicationNameMatcher.matches()) {
      throw new IllegalArgumentException(
          "This is not a spring application name string for a mifos io application: "
              + springApplicationNameString);
    }
    String versionString = applicationNameMatcher.group("version");
    if (versionString == null) {
      throw new IllegalArgumentException("Application name: '" + springApplicationNameString + "' requires a version.  For example 'amit/v1'.");
    }

    final String serviceName = applicationNameMatcher.group("name");
    return new ApplicationName(serviceName, versionString);
  }

  public String getServiceName() {
    return serviceName;
  }

  public String getVersionString() {
    return versionString;
  }

  @Override
  public String toString() {
    return serviceName + "-v" + versionString;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof ApplicationName))
      return false;
    ApplicationName that = (ApplicationName) o;
    return Objects.equals(serviceName, that.serviceName) && Objects
        .equals(versionString, that.versionString);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serviceName, versionString);
  }

}
