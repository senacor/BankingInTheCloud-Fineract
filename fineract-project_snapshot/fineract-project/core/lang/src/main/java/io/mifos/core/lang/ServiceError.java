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
package io.mifos.core.lang;

@SuppressWarnings("WeakerAccess")
public final class ServiceError {

  private final int code;
  private final String message;

  private ServiceError(final int code, final String message) {
    super();
    this.code = code;
    this.message = message;
  }

  public static Builder create(final int code) {
    return new Builder(code);
  }

  public int getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public static final class Builder {

    private final int code;
    private String message;

    public Builder(final int code) {
      super();
      this.code = code;
    }

    public Builder message(final String message) {
      this.message = message;
      return this;
    }

    public ServiceError build() {
      return new ServiceError(this.code, this.message);
    }
  }

  @Override
  public String toString() {
    return "ServiceError{" +
            "code=" + code +
            ", message='" + message + '\'' +
            '}';
  }
}
