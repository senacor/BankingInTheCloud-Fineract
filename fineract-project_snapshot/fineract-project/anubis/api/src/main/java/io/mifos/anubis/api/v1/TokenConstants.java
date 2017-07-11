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
package io.mifos.anubis.api.v1;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings("unused")
public interface TokenConstants {
  String NO_AUTHENTICATION = "N/A";
  String PREFIX = "Bearer ";

  String JWT_SIGNATURE_TIMESTAMP_CLAIM = "/mifos.io/signatureTimestamp";
  String JWT_ENDPOINT_SET_CLAIM = "/mifos.io/endpointSet";
  String JWT_CONTENT_CLAIM = "/mifos.io/tokenContent";
  String JWT_SOURCE_APPLICATION_CLAIM = "/mifos.io/sourceApplication";

  String REFRESH_TOKEN_COOKIE_NAME = "org.apache.fineract.refreshToken";
}
