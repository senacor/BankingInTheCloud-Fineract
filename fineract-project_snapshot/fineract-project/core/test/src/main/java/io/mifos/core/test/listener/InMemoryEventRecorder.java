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
import io.mifos.core.lang.TenantContextHolder;
import org.slf4j.Logger;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author Myrle Krantz
 */
class InMemoryEventRecorder implements EventRecorder {

  private final long maxWait;
  private final BlockingDeque<EventRecord> blockingDeque;
  private final Gson gson;
  private final Logger logger;

  InMemoryEventRecorder(final long maxWait, final Gson gson, final Logger logger) {
    this.maxWait = maxWait;
    this.blockingDeque = new LinkedBlockingDeque<>();
    this.gson = gson;
    this.logger = logger;
  }

  @Override
  public <T> boolean wait(final String operation, final T payloadMatcher) throws InterruptedException {
    return waitForMatch(operation, new Function() {
      @Override
      public Object apply(Object x) {
        return x.equals(payloadMatcher);
      }

      @Override
      public String toString() {
        return payloadMatcher.toString();
      }
    });
  }

  @Override
  public <T> boolean waitForMatch(String operation, Function<T, Boolean> payloadMatcher) throws InterruptedException {
    final long startTime = new Date().getTime();
    long waitedSoFar = 0;

    try (final Cleanup cleanup = new Cleanup()) {
      boolean found = false;
      while (!found) {
        final EventRecord event = this.blockingDeque.poll(Math.max(0, maxWait - waitedSoFar), TimeUnit.MILLISECONDS);

        final long now = new Date().getTime();
        waitedSoFar = now - startTime;

        if (event != null) {
          //noinspection unchecked
          found = (TenantContextHolder.identifier().map(x -> x.equals(event.getTenant())).orElse(false) &&
                  (event.getOperation().equals(operation)) &&
                  payloadMatcher.apply((T)event.getPayloadObject()));

          if (!found)
            cleanup.addStep(() -> blockingDeque.putLast(event));
        }

        if ((waitedSoFar > maxWait) && blockingDeque.isEmpty()) {
          logger.info("Waited {} milliseconds, and event {} with payload {} wasn't observed",
                  waitedSoFar, operation, payloadMatcher);
          return false;
        }
      }

      return true;
    }
  }

  @Override
  public <T> void event(final String tenant, final String operation, final String payload, final Class<T> clazz) {
    final T payloadObject = gson.fromJson(payload, clazz);
    this.blockingDeque.add(new EventRecord(tenant, operation, payloadObject));
  }

  public void clear() {
    this.blockingDeque.clear();
  }

  private interface Step {

    void clean() throws InterruptedException;
  }

  private class Cleanup implements AutoCloseable {

    private List<Step> steps = new LinkedList<>();

    Cleanup() {
      super();
    }

    @Override
    public void close() throws InterruptedException {
      cleanup();
    }

    void addStep(final Step newFirstStep) {
      steps.add(0, newFirstStep);
    }

    private void cleanup() throws InterruptedException {
      for (final Step step : steps) {
        step.clean();
      }
    }
  }
}
