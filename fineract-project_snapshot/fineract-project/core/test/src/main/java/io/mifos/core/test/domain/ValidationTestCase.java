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
package io.mifos.core.test.domain;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Support class for testing correct validation of API domain objects.
 *
 * @author Myrle Krantz
 *
 * @param <T> the type of domain object being tested.
 */
@SuppressWarnings("WeakerAccess")
public class ValidationTestCase<T> {
  private final String description;
  private boolean valid = true;
  private Consumer<T> adjustment = x -> {};

  public ValidationTestCase(final String description)
  {
    this.description = description;
  }

  public ValidationTestCase adjustment(final Consumer<T> adjustment) {
    this.adjustment = adjustment;
    return this;
  }

  public ValidationTestCase valid(boolean newVal) {
    valid = newVal;
    return this;
  }

  public Consumer<T> getAdjustment() {
    return adjustment;
  }

  public boolean check(T testSubject) {

    final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    final Validator validator = factory.getValidator();
    final Set<ConstraintViolation<T>> errors = validator.validate(testSubject);

    if (valid)
      return errors.size() == 0;
    else
      return errors.size() != 0;
  }

  @Override
  public String toString() {
    return "TestCase{" +
            "description='" + description + '\'' +
            '}';
  }

  public void applyAdjustment(T testSubject) {
    adjustment.accept(testSubject);
  }
}
