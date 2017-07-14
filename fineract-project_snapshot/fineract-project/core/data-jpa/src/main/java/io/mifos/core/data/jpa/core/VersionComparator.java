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
package io.mifos.core.data.jpa.core;

import liquibase.changelog.ChangeSet;

public class VersionComparator {

  private VersionComparator() {
    super();
  }

  public static int compare(final ChangeSet firstChangeSet, final ChangeSet secondChangeSet) {
    final String firstId = firstChangeSet.getId();
    final String secondId = secondChangeSet.getId();

    final String[] splitFirstId = firstId.split("\\.");
    final String[] splitSecondId = secondId.split("\\.");

    int index = 0;

    while (index < splitFirstId.length
        && index < splitSecondId.length
        && splitFirstId[index].equals(splitSecondId[index])) {
      index++;
    }

    if (index < splitFirstId.length && index < splitSecondId.length) {
      return Integer.valueOf(splitFirstId[index]).compareTo(Integer.valueOf(splitSecondId[index]));
    }

    return Integer.valueOf(splitFirstId.length).compareTo(Integer.valueOf(splitSecondId.length));
  }
}
