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
package io.mifos.core.cassandra.util;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ParseUtils;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.exceptions.InvalidTypeException;
import io.mifos.core.lang.DateConverter;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;

public class LocalDateTimeCodec extends TypeCodec<LocalDateTime> {

  public LocalDateTimeCodec() {
    super(DataType.timestamp(), LocalDateTime.class);
  }

  @Override
  public ByteBuffer serialize(final LocalDateTime value, final ProtocolVersion protocolVersion) throws InvalidTypeException {
    final Long epochMillis = DateConverter.toEpochMillis(value);
    return TypeCodec.bigint().serializeNoBoxing(epochMillis, protocolVersion);
  }

  @Override
  public LocalDateTime deserialize(final ByteBuffer bytes, final ProtocolVersion protocolVersion) throws InvalidTypeException {
    final Long epochMillis = TypeCodec.bigint().deserializeNoBoxing(bytes, protocolVersion);
    return DateConverter.fromEpochMillis(epochMillis);
  }

  @Override
  public LocalDateTime parse(final String value) throws InvalidTypeException {
    final String toParse;
    if (ParseUtils.isQuoted(value)) {
      toParse = ParseUtils.unquote(value);
    } else {
      toParse = value;
    }

    if (ParseUtils.isLongLiteral(toParse)) {
      return DateConverter.fromEpochMillis(Long.parseLong(toParse));
    } else {
      return DateConverter.fromIsoString(toParse);
    }
  }

  @Override
  public String format(final LocalDateTime value) throws InvalidTypeException {
    return ParseUtils.quote(DateConverter.toIsoString(value));
  }
}
