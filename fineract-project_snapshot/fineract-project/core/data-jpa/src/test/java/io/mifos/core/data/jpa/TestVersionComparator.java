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
package io.mifos.core.data.jpa;

import io.mifos.core.data.jpa.core.VersionComparator;
import liquibase.changelog.ChangeSet;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TestVersionComparator {

  public TestVersionComparator() {
    super();
  }

  @Test
  public void shouldCompareVersionEquals() throws Exception {
    final ChangeSet firstChangeSet = new ChangeSet("1.2.3", null, false, false, null, null, null, null);
    final ChangeSet secondChangeSet = new ChangeSet("1.2.3", null, false, false, null, null, null, null);

    Assert.assertEquals(0, VersionComparator.compare(firstChangeSet, secondChangeSet));
  }

  @Test
  public void shouldCompareVersionGreaterMajorFirst() throws Exception {
    final ChangeSet firstChangeSet = new ChangeSet("2.2.3", null, false, false, null, null, null, null);
    final ChangeSet secondChangeSet = new ChangeSet("1.2.3", null, false, false, null, null, null, null);

    Assert.assertEquals(1, VersionComparator.compare(firstChangeSet, secondChangeSet));
  }

  @Test
  public void shouldCompareVersionGreaterMajorSecond() throws Exception {
    final ChangeSet firstChangeSet = new ChangeSet("1.2.3", null, false, false, null, null, null, null);
    final ChangeSet secondChangeSet = new ChangeSet("2.2.3", null, false, false, null, null, null, null);

    Assert.assertEquals(-1, VersionComparator.compare(firstChangeSet, secondChangeSet));
  }

  @Test
  public void shouldCompareVersionGreaterMinorFirst() throws Exception {
    final ChangeSet firstChangeSet = new ChangeSet("1.3.3", null, false, false, null, null, null, null);
    final ChangeSet secondChangeSet = new ChangeSet("1.2.3", null, false, false, null, null, null, null);

    Assert.assertEquals(1, VersionComparator.compare(firstChangeSet, secondChangeSet));
  }

  @Test
  public void shouldCompareVersionGreaterMinorSecond() throws Exception {
    final ChangeSet firstChangeSet = new ChangeSet("1.2.3", null, false, false, null, null, null, null);
    final ChangeSet secondChangeSet = new ChangeSet("1.3.3", null, false, false, null, null, null, null);

    Assert.assertEquals(-1, VersionComparator.compare(firstChangeSet, secondChangeSet));
  }

  @Test
  public void shouldCompareVersionGreaterFixFirst() throws Exception {
    final ChangeSet firstChangeSet = new ChangeSet("1.2.4", null, false, false, null, null, null, null);
    final ChangeSet secondChangeSet = new ChangeSet("1.2.3", null, false, false, null, null, null, null);

    Assert.assertEquals(1, VersionComparator.compare(firstChangeSet, secondChangeSet));
  }

  @Test
  public void shouldCompareVersionGreaterFixSecond() throws Exception {
    final ChangeSet firstChangeSet = new ChangeSet("1.2.3", null, false, false, null, null, null, null);
    final ChangeSet secondChangeSet = new ChangeSet("1.2.4", null, false, false, null, null, null, null);

    Assert.assertEquals(-1, VersionComparator.compare(firstChangeSet, secondChangeSet));
  }

  @Test
  public void shouldCompareVersionUnevenLengthFixFirst() throws Exception {
    final ChangeSet firstChangeSet = new ChangeSet("1.2.1", null, false, false, null, null, null, null);
    final ChangeSet secondChangeSet = new ChangeSet("1.2", null, false, false, null, null, null, null);

    Assert.assertEquals(1, VersionComparator.compare(firstChangeSet, secondChangeSet));
  }

  @Test
  public void shouldCompareVersionUnevenLengthFixSecond() throws Exception {
    final ChangeSet firstChangeSet = new ChangeSet("1.2", null, false, false, null, null, null, null);
    final ChangeSet secondChangeSet = new ChangeSet("1.2.1", null, false, false, null, null, null, null);

    Assert.assertEquals(-1, VersionComparator.compare(firstChangeSet, secondChangeSet));
  }

  @Test
  public void shouldFindLatestVersion() throws Exception {
    String latestVersion = "2.5.0";
    final List<ChangeSet> versions = Arrays.asList(
        new ChangeSet("1.0.0", null, false, false, null, null, null, null),
        new ChangeSet("2.3.1", null, false, false, null, null, null, null),
        new ChangeSet("1.2.4", null, false, false, null, null, null, null),
        new ChangeSet(latestVersion, null, false, false, null, null, null, null),
        new ChangeSet("1.7.5", null, false, false, null, null, null, null),
        new ChangeSet("2.4.7", null, false, false, null, null, null, null),
        new ChangeSet("1.5.9", null, false, false, null, null, null, null)
    );

    final Optional<ChangeSet> changeSet = versions.stream().max(VersionComparator::compare);
    if (changeSet.isPresent()) {
      Assert.assertEquals(latestVersion, changeSet.get().getId());
    } else {
      Assert.fail();
    }
  }
}
