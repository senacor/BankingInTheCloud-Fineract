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
package io.mifos.core.test.listener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.mifos.core.lang.TenantContextHolder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.Objects;

/**
 * @author Myrle Krantz
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
    classes = {InMemoryEventRecorderTest.TestConfiguration.class},
    loader = AnnotationConfigContextLoader.class)
public class InMemoryEventRecorderTest {

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Autowired
  EventRecorder eventRecorder;


  @Test
  public void shouldNotFindEventAfterClear() throws InterruptedException {
    final String tenant = "x";
    final String line = "could not put humpty together again";
    final String lunch = "egg salad";

    generateEvent(tenant, line, lunch);

    eventRecorder.clear();

    TenantContextHolder.setIdentifier(tenant);
    final boolean found = eventRecorder.wait(line, new TestPayloadObject(lunch));
    TenantContextHolder.clear();
    Assert.assertFalse(found);
  }

  @Test
  public void shouldNotFindEventWhichDidNotOccur() throws InterruptedException {
    final boolean found = eventRecorder.wait("bleblablub", new TestPayloadObject("ceasar dressing"));
    Assert.assertFalse(found);
  }

  @Test
  public void shouldNotFindEventWhenEventWithWrongTenantOccurs() throws InterruptedException {
    final String line = "all the king's horses";
    final String dressing = "vinegar dressing";

    generateEvent("y", line, dressing);

    TenantContextHolder.setIdentifier("x");
    final boolean found = eventRecorder.wait(line, new TestPayloadObject(dressing));
    TenantContextHolder.clear();

    Assert.assertFalse(found);
  }

  @Test
  public void shouldNotFindEventWhenEventWithWrongOperationOccurs() throws InterruptedException {
    final String tenant = "x";
    final String dressing = "italian dressing";

    generateEvent(tenant, "had a great fall", dressing);

    TenantContextHolder.setIdentifier(tenant);
    final boolean found = eventRecorder.wait("humpty dumpty", new TestPayloadObject(dressing));
    TenantContextHolder.clear();

    Assert.assertFalse(found);
  }

  @Test
  public void shouldNotFindEventWhenEventWithWrongPayloadOccurs() throws InterruptedException {
    final String tenant = "x";
    final String line = "and all the king's men";

    generateEvent(tenant, line, "ranch dressing");

    TenantContextHolder.setIdentifier(tenant);
    final boolean found = eventRecorder.wait(line,
        new TestPayloadObject("blue cheese dressing"));
    TenantContextHolder.clear();

    Assert.assertFalse(found);
  }

  @Test
  public void shouldFindEventWhenEverythingMatches() throws InterruptedException {
    final String tenant = "x";
    final String line = "could not put humpty together again";
    final String lunch = "egg salad";

    generateEvent(tenant, line, lunch);

    TenantContextHolder.setIdentifier(tenant);
    final boolean yum = eventRecorder.wait(line, new TestPayloadObject(lunch));
    TenantContextHolder.clear();
    Assert.assertTrue(yum);
  }

  @Test
  public void shouldFindEventWhenTwoAmongSeveral() throws InterruptedException {
    final String tenant = "x";
    final String operation = "tickle";
    final String payloadParamater = "funnyBone";
    for (int i = 0; i < 20; i++) {
      generateEvent(tenant, operation + i, payloadParamater + i);
    }

    TenantContextHolder.setIdentifier(tenant);
    boolean found = eventRecorder.wait("tickle15", new TestPayloadObject("funnyBone15"));
    TenantContextHolder.clear();
    Assert.assertTrue(found);

    TenantContextHolder.setIdentifier(tenant);
    found = eventRecorder.wait("tickle11", new TestPayloadObject("funnyBone11"));
    TenantContextHolder.clear();
    Assert.assertTrue(found);
  }

  private void generateEvent(
      final String tenant,
      final String operation,
      final String payloadParameter) {
    final Gson gson = new GsonBuilder().create();
    eventRecorder.event(tenant, operation,
        gson.toJson(new TestPayloadObject(payloadParameter)),
        TestPayloadObject.class);
  }

  @Configuration
  @EnableEventRecording(maxWait = 1L)
  static public class TestConfiguration {

    public TestConfiguration() {
    }
  }

  private class TestPayloadObject {

    private final String param;

    private TestPayloadObject(String param) {
      this.param = param;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (!(o instanceof TestPayloadObject))
        return false;
      TestPayloadObject that = (TestPayloadObject) o;
      return Objects.equals(param, that.param);
    }

    @Override
    public int hashCode() {
      return Objects.hash(param);
    }
  }
}
