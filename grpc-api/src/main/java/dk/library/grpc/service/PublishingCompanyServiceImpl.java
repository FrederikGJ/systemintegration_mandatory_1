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

    private final PublishingCompanyRepository companies;

    public PublishingCompanyServiceImpl(PublishingCompanyRepository companies) {
        this.companies = companies;
    }

    @Override
    public void listPublishingCompanies(ListPublishingCompaniesRequest request,
                                        StreamObserver<ListPublishingCompaniesResponse> responseObserver) {
        var response = ListPublishingCompaniesResponse.newBuilder();
        companies.findAll().forEach(company -> response.addPublishingCompanies(ProtoMapper.toProto(company)));
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getPublishingCompany(GetPublishingCompanyRequest request,
                                     StreamObserver<PublishingCompany> responseObserver) {
        var company = companies.findById(request.getId()).orElseThrow(() -> notFound(request.getId()));
        responseObserver.onNext(ProtoMapper.toProto(company));
        responseObserver.onCompleted();
    }

    @Override
    public void createPublishingCompany(CreatePublishingCompanyRequest request,
                                        StreamObserver<PublishingCompany> responseObserver) {
        validate(request.getPublishingCompany());
        var created = companies.create(ProtoMapper.toModel(request.getPublishingCompany()));
        responseObserver.onNext(ProtoMapper.toProto(created));
        responseObserver.onCompleted();
    }

    @Override
    public void updatePublishingCompany(UpdatePublishingCompanyRequest request,
                                        StreamObserver<PublishingCompany> responseObserver) {
        validate(request.getPublishingCompany());
        var company = ProtoMapper.toModel(request.getPublishingCompany());
        if (!companies.update(company)) {
            throw notFound(company.id());
        }
        responseObserver.onNext(ProtoMapper.toProto(company));
        responseObserver.onCompleted();
    }

    @Override
    public void deletePublishingCompany(DeletePublishingCompanyRequest request,
                                        StreamObserver<Empty> responseObserver) {
        if (!companies.delete(request.getId())) {
            throw notFound(request.getId());
        }
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private static void validate(PublishingCompany company) {
        if (company.getName().isBlank()) {
            throw Status.INVALID_ARGUMENT.withDescription("name må ikke være tom").asRuntimeException();
        }
    }

    private static StatusRuntimeException notFound(int id) {
        return Status.NOT_FOUND.withDescription("Forlag " + id + " findes ikke").asRuntimeException();
    }
}
