package pt.tecnico.distledger.server.domain;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceImplBase;

public class DistLedgerCrossServerServiceImpl extends DistLedgerCrossServerServiceImplBase{

    private ServerState serverState;

    public DistLedgerCrossServerServiceImpl(ServerState serverState) {
        this.serverState = serverState;
    }

    @Override
    public synchronized void propagateState(PropagateStateRequest request, StreamObserver<PropagateStateResponse> responseObserver) {
        System.out.println("Received PropagateStateRequest");
        
        serverState.receiveOperation(request.getOperation());

        PropagateStateResponse response = PropagateStateResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
