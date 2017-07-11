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

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TenantContextJavaConfiguration {

  public TenantContextJavaConfiguration() {
    super();
  }

  @Bean
  public FilterRegistrationBean tenantFilterRegistration() {
    final TenantHeaderFilter tenantHeaderFilter = new TenantHeaderFilter();
    final FilterRegistrationBean registration = new FilterRegistrationBean();
    registration.setFilter(tenantHeaderFilter);
    registration.addUrlPatterns("/*");
    registration.setName("tenantHeaderFilter");
    registration.setOrder(Integer.MIN_VALUE); //Before all other filters.  Especially the security filter.
    return registration;
  }
}
