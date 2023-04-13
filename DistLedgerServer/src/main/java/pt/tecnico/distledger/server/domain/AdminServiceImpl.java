package pt.tecnico.distledger.server.domain;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.Debug;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc.AdminServiceImplBase;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.LedgerState;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.OperationType;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.*;

import static io.grpc.Status.*;


public class AdminServiceImpl extends AdminServiceImplBase{

    private ServerState ledger;

    public AdminServiceImpl(ServerState serverState) {
        this.ledger = serverState;
    }

    @Override
    public synchronized void getLedgerState(getLedgerStateRequest request,
                                            StreamObserver<getLedgerStateResponse> responseObserver) {
        Debug.debug("Received get ledger state request.");

        LedgerState.Builder ledgerState = LedgerState.newBuilder();
        for (Operation op : ledger.getOperations()) {
            OperationType type;
            DistLedgerCommonDefinitions.Operation operation;
            if(op.getClass().getName() == "pt.tecnico.distledger.server.domain.operation.CreateOp"){
                type = DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT;
                operation = DistLedgerCommonDefinitions.Operation.newBuilder()
                        .setType(type)
                        .setUserId(op.getAccount())
                        .build();
            } else if (op.getClass().getName() == "pt.tecnico.distledger.server.domain.operation.TransferOp"){
                type = DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO;
                operation = DistLedgerCommonDefinitions.Operation.newBuilder()
                        .setType(type)
                        .setUserId(op.getAccount())
                        .setDestUserId(((TransferOp) op).getDestAccount())
                        .setAmount(((TransferOp) op).getAmount())
                        .build();
            } else {
                type = DistLedgerCommonDefinitions.OperationType.OP_UNSPECIFIED;
                operation = DistLedgerCommonDefinitions.Operation.newBuilder()
                        .setType(type)
                        .setUserId(op.getAccount())
                        .build();
            }
            ledgerState.addLedger(operation);
        }

        getLedgerStateResponse response = getLedgerStateResponse.newBuilder()
                .setLedgerState(ledgerState.build())
                .build();

        Debug.debug("Sending response.");
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        Debug.debug("Request handled.");
    }

    @Override
    public synchronized void activate(ActivateRequest request, StreamObserver<ActivateResponse> responseObserver) {
        Debug.debug("Received activate server request.");

        int res = ledger.activate(request.getQualifier());
        switch (res) {
            case 0:
                ActivateResponse response = ActivateResponse.newBuilder().build();

                Debug.debug("Sending response.");
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                Debug.debug("Request handled.");
                break;
            case -1:
                responseObserver.onError(
                        new Exception(CANCELLED.withDescription("Server already actived").asRuntimeException()));
                break;
        
            default:
                responseObserver.onError(
                        new Exception(UNKNOWN.withDescription("Failed to activate").asRuntimeException()));
                break;
        }

    }

    @Override
    public synchronized void deactivate(DeactivateRequest request,
                                        StreamObserver<DeactivateResponse> responseObserver) {
        Debug.debug("Received deactivate server request.");

        int res = ledger.deactivate(request.getQualifier());
        switch (res) {
            case 0:
                DeactivateResponse response = DeactivateResponse.newBuilder().build();

                Debug.debug("Sending response.");
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                Debug.debug("Request handled.");
                break;
            case -1:
                responseObserver.onError(
                        new Exception(CANCELLED.withDescription("Server already deactived").asRuntimeException()));
                break;
        
            default:
                responseObserver.onError(
                        new Exception(UNKNOWN.withDescription("Failed to deactivate").asRuntimeException()));
                break;
        }
    }

    @Override
    public synchronized void gossip(GossipRequest request, StreamObserver<GossipResponse> responseObserver) {
        Debug.debug("Received gossip request.");

        int res = ledger.gossip(request.getTarget());
        switch (res) {
            case 0:
                GossipResponse response = GossipResponse.newBuilder().build();

                Debug.debug("Sending response.");
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                Debug.debug("Request handled.");
                break;
            case -1:                        // TODO: review this all, its auto generated
                responseObserver.onError(
                        new Exception(CANCELLED.withDescription("Server not actived").asRuntimeException()));
                break;
        
            default:
                responseObserver.onError(
                        new Exception(UNKNOWN.withDescription("Failed to activate").asRuntimeException()));
                break;
        }
    }
}
