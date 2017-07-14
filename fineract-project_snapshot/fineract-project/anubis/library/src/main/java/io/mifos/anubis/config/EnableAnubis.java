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
package io.mifos.anubis.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@SuppressWarnings({"unused", "WeakerAccess"})
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Import({
    AnubisConfiguration.class,
    AnubisImportSelector.class,
    AnubisSecurityConfigurerAdapter.class
})
public @interface EnableAnubis {
  boolean provideSignatureRestController() default true;
  boolean provideSignatureStorage() default true;
  boolean generateEmptyInitializeEndpoint() default false;
}
