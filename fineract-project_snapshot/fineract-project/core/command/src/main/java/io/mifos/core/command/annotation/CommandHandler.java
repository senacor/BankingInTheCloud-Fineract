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
package io.mifos.core.command.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface CommandHandler {
  /**
   *
   * @return the log level with which to log a command which will be sent to a command handler. By default this is
   * NONE. The level should be left at NONE for sensitive data which do not belong in the log file, such as
   * passwords, financial transactions, and customer names and addresses. The logfile contents will include what
   * is output by "toString" of the command, so any fine-tuning of the contents of the logfile can be performed there.
   * For commands which happen frequently this can produce a lot of output. Consider using a log level of DEBUG or
   * TRACE if you decide to log a command of that sort. No log level of ERROR, or WARNING is offered here, because
   * a command is not an error.
   */
  CommandLogLevel logStart() default CommandLogLevel.NONE;

  /**
   *
   * @return the log level with which to log the result of a command which emits an event. Leave this as it's default
   * NONE, if the command is not event-emitting.  The level should also be left at NONE for sensitive data which do
   * not belong in the log file, however you should consider not putting such data in the event queue in the first
   * place. The logfile contents will include the output of the events "toString". For commands which happen frequently,
   * this can produce a lot of output.  Consider not using INFO as a log level for a command of that sort. If a command
   * handler is exited via an exception, no event is logged, but the exception may be logged regardless of this setting.
   */
  CommandLogLevel logFinish() default CommandLogLevel.NONE;
}
