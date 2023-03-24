package pt.tecnico.distledger.server.domain;

import java.util.*;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.ServerMain;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.*;

public class DistLedgerCrossServerService {

    private ServerState serverState;

    public DistLedgerCrossServerService(ServerState serverState) {
        this.serverState = serverState;
    }

    public void propagateState(PropagateStateRequest request, StreamObserver<PropagateStateResponse> responseObserver) {
        System.out.println("Received PropagateStateRequest");
        
        serverState.receiveOperation(request.getOperation());

        PropagateStateResponse response = PropagateStateResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
