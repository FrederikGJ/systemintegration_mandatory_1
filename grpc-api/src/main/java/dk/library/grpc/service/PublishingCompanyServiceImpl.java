package dk.library.grpc.service;

import com.google.protobuf.Empty;
import dk.library.db.repository.PublishingCompanyRepository;
import dk.library.grpc.proto.CreatePublishingCompanyRequest;
import dk.library.grpc.proto.DeletePublishingCompanyRequest;
import dk.library.grpc.proto.GetPublishingCompanyRequest;
import dk.library.grpc.proto.ListPublishingCompaniesRequest;
import dk.library.grpc.proto.ListPublishingCompaniesResponse;
import dk.library.grpc.proto.PublishingCompany;
import dk.library.grpc.proto.PublishingCompanyServiceGrpc;
import dk.library.grpc.proto.UpdatePublishingCompanyRequest;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

public final class PublishingCompanyServiceImpl extends PublishingCompanyServiceGrpc.PublishingCompanyServiceImplBase {
}
