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
package io.mifos.anubis.api.v1.domain;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public enum AllowedOperation {
  @SerializedName("READ")
  READ {
    @Override public boolean containsHttpMethod(String httpMethod) {
      return httpMethod.equalsIgnoreCase("get") || httpMethod.equalsIgnoreCase("head");
    }
  }, //GET, HEAD
  @SerializedName("CHANGE")
  CHANGE {
    @Override public boolean containsHttpMethod(String httpMethod) {
      return httpMethod.equalsIgnoreCase("post") || httpMethod.equalsIgnoreCase("put");
    }
  }, //POST, PUT
  @SerializedName("DELETE")
  DELETE {
    @Override public boolean containsHttpMethod(String httpMethod) {
      return httpMethod.equalsIgnoreCase("delete");
    }
  }; //DELETE

  public static final Set<AllowedOperation> ALL = Collections.unmodifiableSet(
      new HashSet<AllowedOperation>() {{add(READ); add(CHANGE); add(DELETE);}});

  public abstract boolean containsHttpMethod(final String httpMethod);
}
