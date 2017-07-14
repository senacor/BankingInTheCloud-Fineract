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

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.Semaphore;

public class TenantContextHolderTest {

  private static String TEST_IDENTIFIER = "test";

  public TenantContextHolderTest() {
    super();
  }

  @After
  public void clearTenantContext() {
    TenantContextHolder.clear();
  }

  @Test
  public void shouldSetTenantIdentifier() {
    TenantContextHolder.setIdentifier(TEST_IDENTIFIER);
    Assert.assertTrue(TenantContextHolder.identifier().isPresent());
    @SuppressWarnings("OptionalGetWithoutIsPresent") final String identifier = TenantContextHolder.identifier().get();
    Assert.assertEquals(TEST_IDENTIFIER, identifier);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowIfNotSet()
  {
    TenantContextHolder.clear();
    TenantContextHolder.checkedGetIdentifier();
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldNotSetTenantIdentifierAlreadyAssigned() {
    TenantContextHolder.setIdentifier(TEST_IDENTIFIER);
    TenantContextHolder.setIdentifier(TEST_IDENTIFIER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldNotSetTenantIdentifierNull() {
    //noinspection ConstantConditions
    TenantContextHolder.setIdentifier(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldNotSetTenantIdentifierEmpty() {
    TenantContextHolder.setIdentifier("");
  }

  //Correct execution of commands in the command bus depend on the value of tenant being unchanged, even if the
  // original jetty thread is returned to the thread pool before the command is executed.
  @Test()
  public void settingParentThreadValueShouldNotChangeChildThreadValueAfterFork() throws InterruptedException {
    TenantContextHolder.setIdentifier(TEST_IDENTIFIER);

    final ChildRunnable child = new ChildRunnable();
    child.parentSignal.acquire();
    final Thread childThread = new Thread(child);
    childThread.start();
    TenantContextHolder.clear();
    TenantContextHolder.setIdentifier(TEST_IDENTIFIER + "somethingelse");
    final boolean tenantChanged = TenantContextHolder.identifier().map(x -> !x.equals(TEST_IDENTIFIER)).orElse(false);
    Assert.assertTrue(tenantChanged);
    do {
      child.parentSignal.release();
      child.parentSignal.acquire();
    } while (!child.tenantChecked);
    Assert.assertTrue(child.tenantUnchanged);
  }

  private static class ChildRunnable implements Runnable {
    private boolean tenantUnchanged = false;
    private boolean tenantChecked = false;
    private Semaphore parentSignal = new Semaphore(1);

    @Override
    public void run() {
      try {
        parentSignal.acquire();
        tenantUnchanged = TenantContextHolder.identifier().map(x -> x.equals(TEST_IDENTIFIER)).orElse(false);
        tenantChecked = true;
        parentSignal.release();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }


}
