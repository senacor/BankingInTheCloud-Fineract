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

import io.mifos.core.lang.validation.constraints.ValidIdentifier;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author Myrle Krantz
 */
public class CheckIdentifier implements ConstraintValidator<ValidIdentifier, String> {
   private int maximumLength = 32;
   private boolean optional = false;
   @Override
   public void initialize(final ValidIdentifier constraint) {
      maximumLength = constraint.maxLength();
      optional = constraint.optional();
   }

   @Override
   public boolean isValid(final String obj, final ConstraintValidatorContext context) {
      if (obj == null)
         return optional;

      return validate(obj, maximumLength);
   }

   static boolean validate(final String obj, final int maximumLength) {
      if (obj.length() < 2)
         return false;

      if (obj.length() > maximumLength)
         return false;

      try {
         return encode(obj).equals(obj);
      }
      catch (UnsupportedEncodingException e) {
         return false; //If we can't encode with UTF-8, then there are no valid names.
      }
   }

   static private String encode(String identifier) throws UnsupportedEncodingException {
      return URLEncoder.encode(identifier, "UTF-8");
   }
}
