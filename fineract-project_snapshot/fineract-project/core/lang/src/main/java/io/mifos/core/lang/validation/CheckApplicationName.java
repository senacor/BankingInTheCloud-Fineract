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
package io.mifos.core.lang.validation;

import io.mifos.core.lang.ApplicationName;
import io.mifos.core.lang.validation.constraints.ValidApplicationName;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Myrle Krantz
 */
public class CheckApplicationName implements ConstraintValidator<ValidApplicationName, String> {
   public void initialize(final ValidApplicationName constraint) {
   }

   public boolean isValid(final String obj, final ConstraintValidatorContext context) {
      if (obj == null)
         return false;

      try {
         ApplicationName.fromSpringApplicationName(obj);
         return true;
      }
      catch (final IllegalArgumentException ignored)
      {
         return false;
      }
   }
}
