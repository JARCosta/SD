package pt.tecnico.distledger.server.domain;

import io.grpc.stub.StreamObserver;
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
    public synchronized void getLedgerState(getLedgerStateRequest request, StreamObserver<getLedgerStateResponse> responseObserver) {
        
        LedgerState.Builder ledgerState = LedgerState.newBuilder();
        for (Operation op : ledger.getOperations()) {
            OperationType type;
            DistLedgerCommonDefinitions.Operation operation;
            System.out.println("operation " + op.getClass().getName() + (op.getClass().getName() == "pt.tecnico.distledger.server.domain.operation.CreateOp") + (op.getClass().getName() == "pt.tecnico.distledger.server.domain.operation.DeleteAccount") + (op.getClass().getName() == "pt.tecnico.distledger.server.domain.operation.TransferOp"));
            if(op.getClass().getName() == "pt.tecnico.distledger.server.domain.operation.CreateOp"){
                type = DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT;
                operation = DistLedgerCommonDefinitions.Operation.newBuilder().setType(type).setUserId(op.getAccount()).build();
            } else if(op.getClass().getName() == "pt.tecnico.distledger.server.domain.operation.DeleteOp"){
                type = DistLedgerCommonDefinitions.OperationType.OP_DELETE_ACCOUNT;
                operation = DistLedgerCommonDefinitions.Operation.newBuilder().setType(type).setUserId(op.getAccount()).build();
            } else if (op.getClass().getName() == "pt.tecnico.distledger.server.domain.operation.TransferOp"){
                type = DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO;
                operation = DistLedgerCommonDefinitions.Operation.newBuilder().setType(type).setUserId(op.getAccount()).setDestUserId(((TransferOp) op).getDestAccount()).setAmount(((TransferOp) op).getAmount()).build();
            }else {
                type = DistLedgerCommonDefinitions.OperationType.OP_UNSPECIFIED;
                operation = DistLedgerCommonDefinitions.Operation.newBuilder().setType(type).setUserId(op.getAccount()).build();
            }
            ledgerState.addLedger(operation);
        }
        getLedgerStateResponse response = getLedgerStateResponse.newBuilder().setLedgerState(ledgerState.build()).build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void activate(ActivateRequest request, StreamObserver<ActivateResponse> responseObserver) {
        int res = ledger.activate();
        switch (res) {
            case 0:
                ActivateResponse response = ActivateResponse.newBuilder().build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                break;
            case -1:
                responseObserver.onError(new Exception(CANCELLED.withDescription("Server already actived").asRuntimeException()));
                break;
        
            default:
                responseObserver.onError(new Exception(UNKNOWN.withDescription("Failed to activate").asRuntimeException()));
                break;
        }

    }

    @Override
    public void deactivate(DeactivateRequest request, StreamObserver<DeactivateResponse> responseObserver) {
        int res = ledger.deactivate();
        switch (res) {
            case 0:
                DeactivateResponse response = DeactivateResponse.newBuilder().build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                break;
            case -1:
                responseObserver.onError(new Exception(CANCELLED.withDescription("Server already deactived").asRuntimeException()));
                break;
        
            default:
                responseObserver.onError(new Exception(UNKNOWN.withDescription("Failed to deactivate").asRuntimeException()));
                break;
        }
    }
}
