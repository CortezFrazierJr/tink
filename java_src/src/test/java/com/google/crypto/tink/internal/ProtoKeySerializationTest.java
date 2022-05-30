// Copyright 2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////

package com.google.crypto.tink.internal;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertThrows;

import com.google.crypto.tink.proto.KeyData.KeyMaterialType;
import com.google.crypto.tink.proto.OutputPrefixType;
import com.google.protobuf.ByteString;
import java.security.GeneralSecurityException;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@code ProtoKeySerialization} */
@RunWith(JUnit4.class)
public final class ProtoKeySerializationTest {
  @Test
  public void testCreationAndValues_basic() throws Exception {
    ProtoKeySerialization serialization =
        ProtoKeySerialization.create(
            "myTypeUrl",
            ByteString.copyFrom(new byte[] {10, 11, 12}),
            KeyMaterialType.SYMMETRIC,
            OutputPrefixType.RAW,
            Optional.empty());

    assertThat(serialization.getValue()).isEqualTo(ByteString.copyFrom(new byte[] {10, 11, 12}));
    assertThat(serialization.getKeyMaterialType()).isEqualTo(KeyMaterialType.SYMMETRIC);
    assertThat(serialization.getOutputPrefixType()).isEqualTo(OutputPrefixType.RAW);
    assertThat(serialization.getTypeUrl()).isEqualTo("myTypeUrl");
    assertThat(serialization.getIdRequirement().isPresent()).isFalse();
    assertThat(serialization.getObjectIdentifier())
        .isEqualTo(ByteArray.copyOf("myTypeUrl".getBytes(UTF_8)));
  }

  @Test
  public void testIdRequirement_present() throws Exception {
    final String typeUrl = "myTypeUrl";
    final ByteString value = ByteString.copyFrom(new byte[] {10, 11, 12});
    final KeyMaterialType keyMaterialType = KeyMaterialType.SYMMETRIC;

    ProtoKeySerialization serialization =
        ProtoKeySerialization.create(
            typeUrl, value, keyMaterialType, OutputPrefixType.TINK, Optional.of(123));
    assertThat(serialization.getOutputPrefixType()).isEqualTo(OutputPrefixType.TINK);
    assertThat(serialization.getIdRequirement()).isEqualTo(Optional.of(123));
  }

  @Test
  public void testIdRequirement_presentMustMatchoutputPrefixType() throws Exception {
    final String typeUrl = "myTypeUrl";
    final ByteString value = ByteString.copyFrom(new byte[] {10, 11, 12});
    final KeyMaterialType keyMaterialType = KeyMaterialType.SYMMETRIC;

    ProtoKeySerialization.create(
        typeUrl, value, keyMaterialType, OutputPrefixType.RAW, Optional.empty());
    ProtoKeySerialization.create(
        typeUrl, value, keyMaterialType, OutputPrefixType.TINK, Optional.of(123));
    ProtoKeySerialization.create(
        typeUrl, value, keyMaterialType, OutputPrefixType.CRUNCHY, Optional.of(123));
    ProtoKeySerialization.create(
        typeUrl, value, keyMaterialType, OutputPrefixType.LEGACY, Optional.of(123));

    assertThrows(
        GeneralSecurityException.class,
        () ->
            ProtoKeySerialization.create(
                typeUrl, value, keyMaterialType, OutputPrefixType.RAW, Optional.of(123)));
    assertThrows(
        GeneralSecurityException.class,
        () ->
            ProtoKeySerialization.create(
                typeUrl, value, keyMaterialType, OutputPrefixType.TINK, Optional.empty()));
    assertThrows(
        GeneralSecurityException.class,
        () ->
            ProtoKeySerialization.create(
                typeUrl, value, keyMaterialType, OutputPrefixType.CRUNCHY, Optional.empty()));
    assertThrows(
        GeneralSecurityException.class,
        () ->
            ProtoKeySerialization.create(
                typeUrl, value, keyMaterialType, OutputPrefixType.LEGACY, Optional.empty()));
  }
}