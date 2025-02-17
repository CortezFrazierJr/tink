// Copyright 2020 Google LLC
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

package com.google.crypto.tink.testing;

import com.google.crypto.tink.PublicKeySign;
import com.google.crypto.tink.PublicKeyVerify;
import com.google.crypto.tink.testing.proto.CreationRequest;
import com.google.crypto.tink.testing.proto.CreationResponse;
import com.google.crypto.tink.testing.proto.SignatureGrpc.SignatureImplBase;
import com.google.crypto.tink.testing.proto.SignatureSignRequest;
import com.google.crypto.tink.testing.proto.SignatureSignResponse;
import com.google.crypto.tink.testing.proto.SignatureVerifyRequest;
import com.google.crypto.tink.testing.proto.SignatureVerifyResponse;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import java.security.GeneralSecurityException;

/** Implements a gRPC Signature Testing service. */
public final class SignatureServiceImpl extends SignatureImplBase {

  public SignatureServiceImpl() throws GeneralSecurityException {
  }

  @Override
  public void createPublicKeySign(
      CreationRequest request, StreamObserver<CreationResponse> responseObserver) {
    Util.createPrimitiveForRpc(request, responseObserver, PublicKeySign.class);
  }

  @Override
  public void createPublicKeyVerify(
      CreationRequest request, StreamObserver<CreationResponse> responseObserver) {
    Util.createPrimitiveForRpc(request, responseObserver, PublicKeyVerify.class);
  }

  private SignatureSignResponse sign(SignatureSignRequest request) throws GeneralSecurityException {
    try {
      PublicKeySign signer = Util.parseBinaryProtoKeyset(request.getPrivateKeyset()).getPrimitive(PublicKeySign.class);
      byte[] signatureValue = signer.sign(request.getData().toByteArray());
      return SignatureSignResponse.newBuilder().setSignature(ByteString.copyFrom(signatureValue)).build();
    } catch (GeneralSecurityException e)  {
      return SignatureSignResponse.newBuilder().setErr(e.toString()).build();
    }
  }

  @Override
  public void sign(
      SignatureSignRequest request, StreamObserver<SignatureSignResponse> responseObserver) {
    try {
      responseObserver.onNext(sign(request));
      responseObserver.onCompleted();
    } catch (GeneralSecurityException e) {
      responseObserver.onError(e);
    }
  }

  private SignatureVerifyResponse verify(SignatureVerifyRequest request)
      throws GeneralSecurityException {
    try {
      PublicKeyVerify verifier =
          Util.parseBinaryProtoKeyset(request.getPublicKeyset())
              .getPrimitive(PublicKeyVerify.class);
      verifier.verify(request.getSignature().toByteArray(), request.getData().toByteArray());
      return SignatureVerifyResponse.getDefaultInstance();
    } catch (GeneralSecurityException e) {
      return SignatureVerifyResponse.newBuilder().setErr(e.toString()).build();
    }
  }

  @Override
  public void verify(
      SignatureVerifyRequest request, StreamObserver<SignatureVerifyResponse> responseObserver) {
    try {
      responseObserver.onNext(verify(request));
      responseObserver.onCompleted();
    } catch (GeneralSecurityException e) {
      responseObserver.onError(e);
    }
  }
}
