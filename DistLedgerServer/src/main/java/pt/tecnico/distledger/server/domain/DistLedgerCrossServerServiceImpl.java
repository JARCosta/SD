package pt.tecnico.distledger.server.domain;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceImplBase;

// servico oferecido pelo servidor B
public class DistLedgerCrossServerServiceImpl extends DistLedgerCrossServerServiceImplBase{

    private ServerState serverState;

    public DistLedgerCrossServerServiceImpl(ServerState serverState) {
        this.serverState = serverState;
    }

    @Override
    public synchronized void propagateState(PropagateStateRequest request, StreamObserver<PropagateStateResponse> responseObserver) {
        System.out.println("Received PropagateStateRequest");
        
        serverState.updateServerState(request.getState());

        PropagateStateResponse response = PropagateStateResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
