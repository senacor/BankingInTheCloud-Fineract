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
package io.mifos.core.command.fixture;

import io.mifos.core.command.annotation.Aggregate;
import io.mifos.core.command.annotation.CommandHandler;
import io.mifos.core.command.annotation.EventEmitter;

@Aggregate
public class DomainAggregate {

  public DomainAggregate() {
    super();
  }

  @CommandHandler
  public void voidCommand(final VoidCommand voidCommand) {
    // do nothing
  }

  @CommandHandler
  public DomainObject returningCommand(final ReturningCommand returningCommand) {
    return returningCommand.getDomainObject();
  }

  @CommandHandler
  @EventEmitter(selectorName = "operation", selectorValue = "void")
  public String voidWithEventCommand(final VoidWithEventCommand voidWithEventCommand) {
    return "event";
  }

  @CommandHandler
  @EventEmitter(selectorName = "operation", selectorValue = "returning")
  public DomainObject returningWithEventCommand(
      final ReturningWithEventCommand returningWithEventCommand) {
    return returningWithEventCommand.getDomainObject();
  }

  @CommandHandler
  public void erroneous(final ErroneousDomainCommand erroneousDomainCommand) throws DomainException {
    throw new DomainException();
  }
}
