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
package io.mifos.core.command.domain;

import io.mifos.core.command.annotation.EventEmitter;
import io.mifos.core.lang.TenantContextHolder;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public final class CommandHandlerHolder {

  private final Object aggregate;
  private final Method method;
  private final EventEmitter eventEmitter;
  private final Class<?>[] exceptionTypes;
  private final Consumer<Object> logStart;
  private final Consumer<Object> logFinish;

  public CommandHandlerHolder(final Object aggregate, final Method method, final EventEmitter eventEmitter,
                              final Class<?>[] exceptionTypes,
                              final Consumer<Object> logStart,
                              final Consumer<Object> logFinish) {
    super();
    this.aggregate = aggregate;
    this.method = method;
    this.eventEmitter = eventEmitter;
    this.exceptionTypes = exceptionTypes;
    this.logStart = logStart;
    this.logFinish = logFinish;
  }

  public Object aggregate() {
    return aggregate;
  }

  public Method method() {
    return method;
  }

  public EventEmitter eventEmitter() {
    return eventEmitter;
  }

  public Class<?>[] exceptionTypes() {
    return exceptionTypes;
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  public void logStart(final Object command) {
    logStart.accept(command);
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  public void logFinish(final Object command) {
    logFinish.accept(command);
  }
}
