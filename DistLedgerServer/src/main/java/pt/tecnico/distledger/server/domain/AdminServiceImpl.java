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
    public void getLedgerState(getLedgerStateRequest request, StreamObserver<getLedgerStateResponse> responseObserver) {
        
        LedgerState.Builder ledgerState = LedgerState.newBuilder();
        for (Operation op : ledger.getOperations()) {
            OperationType type;
            DistLedgerCommonDefinitions.Operation operation;
            switch (op.getClass().getName()) {
                case "CreateAccount":
                    type = DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT;
                    operation = DistLedgerCommonDefinitions.Operation.newBuilder().setType(type).setUserId(op.getAccount()).build();
                case "DeleteAccount":
                    type = DistLedgerCommonDefinitions.OperationType.OP_DELETE_ACCOUNT;
                    operation = DistLedgerCommonDefinitions.Operation.newBuilder().setType(type).setUserId(op.getAccount()).build();
                case "TransferOp":
                    type = DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO;
                    operation = DistLedgerCommonDefinitions.Operation.newBuilder().setType(type).setUserId(op.getAccount()).setDestUserId(((TransferOp) op).getDestAccount()).setAmount(((TransferOp) op).getAmount()).build();
                default:
                    type = DistLedgerCommonDefinitions.OperationType.OP_UNSPECIFIED;
                    operation = DistLedgerCommonDefinitions.Operation.newBuilder().setType(type).setUserId(op.getAccount()).build();
            }

            operation = DistLedgerCommonDefinitions.Operation.newBuilder().setType(type).setUserId(op.getAccount())./*setDestUserId(null).setAmount(0).*/build();
            ledgerState.addLedger(operation);
        }
        getLedgerStateResponse response = getLedgerStateResponse.newBuilder().setLedgerState(ledgerState.build()).build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void activate(ActivateRequest request, StreamObserver<ActivateResponse> responseObserver) {
        ledger.activate();
        ActivateResponse response = ActivateResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deactivate(DeactivateRequest request, StreamObserver<DeactivateResponse> responseObserver) {
        ledger.deactivate();
        DeactivateResponse response = DeactivateResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
