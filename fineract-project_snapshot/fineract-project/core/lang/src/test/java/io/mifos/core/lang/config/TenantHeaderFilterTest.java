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
package io.mifos.core.lang.config;

import io.mifos.core.lang.TenantContextHolder;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class TenantHeaderFilterTest {

  private static final String TEST_TENANT = "test";

  public TenantHeaderFilterTest() {
    super();
  }

  @Test
  public void shouldFilterTenant() throws Exception {
    final HttpServletRequest mockedRequest = Mockito.mock(HttpServletRequest.class);
    Mockito.when(mockedRequest.getHeader(TenantHeaderFilter.TENANT_HEADER)).thenReturn(TEST_TENANT);

    final HttpServletResponse mockedResponse = Mockito.mock(HttpServletResponse.class);

    final FilterChain mockedFilterChain = (request, response) -> {
      Assert.assertTrue(TenantContextHolder.identifier().isPresent());
      //noinspection OptionalGetWithoutIsPresent
      Assert.assertEquals(TEST_TENANT, TenantContextHolder.identifier().get());
    };

    final TenantHeaderFilter tenantHeaderFilter = new TenantHeaderFilter();
    tenantHeaderFilter.doFilter(mockedRequest, mockedResponse, mockedFilterChain);

    Assert.assertFalse(TenantContextHolder.identifier().isPresent());
    Mockito.verifyZeroInteractions(mockedResponse);
  }

  @Test
  public void shouldNotFilterTenantMissingHeader() throws Exception {
    final HttpServletRequest mockedRequest = Mockito.mock(HttpServletRequest.class);
    Mockito.when(mockedRequest.getHeader(TenantHeaderFilter.TENANT_HEADER)).thenReturn(null);

    final HttpServletResponse mockedResponse = Mockito.mock(HttpServletResponse.class);

    final FilterChain mockedFilterChain =
        (request, response) -> Assert.assertFalse(TenantContextHolder.identifier().isPresent());

    final TenantHeaderFilter tenantHeaderFilter = new TenantHeaderFilter();
    tenantHeaderFilter.doFilter(mockedRequest, mockedResponse, mockedFilterChain);

    Assert.assertFalse(TenantContextHolder.identifier().isPresent());
    Mockito.verify(mockedResponse, Mockito.times(1)).sendError(Mockito.eq(400), Mockito.anyString());
  }
}
