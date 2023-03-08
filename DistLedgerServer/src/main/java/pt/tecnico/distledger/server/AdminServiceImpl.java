package pt.tecnico.distledger.server;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc.AdminServiceImplBase;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.*;

import static io.grpc.Status.*;

public class AdminServiceImpl extends AdminServiceImplBase{

    @Override
    public void getLedgerState(getLedgerStateRequest request, StreamObserver<getLedgerStateResponse> responseObserver) {
        getLedgerStateResponse response = getLedgerStateResponse.newBuilder()/*.setLedger(ledger.toString())*/.build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void activate(ActivateRequest request, StreamObserver<ActivateResponse> responseObserver) {
        ActivateResponse response = ActivateResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deactivate(DeactivateRequest request, StreamObserver<DeactivateResponse> responseObserver) {
        DeactivateResponse response = DeactivateResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
