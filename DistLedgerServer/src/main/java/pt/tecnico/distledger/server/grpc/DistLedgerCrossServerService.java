package pt.tecnico.distledger.server.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
//import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceOuterClass.*;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.*;



// servico usado pelo servidor A
public class DistLedgerCrossServerService {

    private final ManagedChannel channel;
    private DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub stub;

    public DistLedgerCrossServerService(String target) {
        /*fazer lookup do servidor B para obter localhost:2002*/
        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        stub = DistLedgerCrossServerServiceGrpc.newBlockingStub(channel);
    }

    public void propagateState(DistLedgerCommonDefinitions.Operation op){
        try{
            PropagateStateResponse result = stub.propagateState(PropagateStateRequest.newBuilder()
                    .setOperation(op)
                    .build());
            System.out.println(result == null ? "null" : "OK");
        }
        catch (StatusRuntimeException e){
            System.out.println(e.getStatus().getDescription());
        }
    }

}
