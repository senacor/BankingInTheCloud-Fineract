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

import io.mifos.core.lang.ServiceException;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.web.util.NestedServletException;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServiceExceptionFilterTest {

  public ServiceExceptionFilterTest() {
    super();
  }

  @Test
  public void shouldProceedNoServiceException() throws Exception {
    final HttpServletRequest mockedRequest = Mockito.mock(HttpServletRequest.class);
    final HttpServletResponse mockedResponse = Mockito.mock(HttpServletResponse.class);
    final FilterChain mockedFilterChain = Mockito.mock(FilterChain.class);

    final ServiceExceptionFilter serviceExceptionFilter = new ServiceExceptionFilter();
    serviceExceptionFilter.doFilter(mockedRequest, mockedResponse, mockedFilterChain);

    Mockito.verifyZeroInteractions(mockedResponse);
    Mockito.verify(mockedFilterChain, Mockito.times(1)).doFilter(mockedRequest, mockedResponse);
  }

  @Test
  public void shouldFilterServiceException() throws Exception {
    final String errorMessage = "Should fail.";

    final HttpServletRequest mockedRequest = Mockito.mock(HttpServletRequest.class);
    final HttpServletResponse mockedResponse = Mockito.mock(HttpServletResponse.class);

    final FilterChain mockedFilterChain = (request, response) -> {
      throw new NestedServletException(errorMessage, ServiceException.conflict(errorMessage));
    };

    final ServiceExceptionFilter serviceExceptionFilter = new ServiceExceptionFilter();
    serviceExceptionFilter.doFilter(mockedRequest, mockedResponse, mockedFilterChain);

    Mockito.verify(mockedResponse, Mockito.times(1)).sendError(Mockito.eq(409), Mockito.eq(errorMessage));
  }
}
