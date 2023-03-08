package pt.tecnico.distledger.server.domain;

import io.grpc.Server;
import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc.UserServiceImplBase;

import static io.grpc.Status.*;

public class UserServiceImpl extends UserServiceImplBase{
    private ServerState ledger;

    public UserServiceImpl(ServerState serverState) {
        this.ledger = serverState;
    }

    @Override
    public void balance(BalanceRequest request, StreamObserver<BalanceResponse> responseObserver) {

        if(!ledger.accountExists(request.getUserId())){
            responseObserver.onError(new Exception(NOT_FOUND.withDescription("User not found").asRuntimeException()));
        } else {

            BalanceResponse response = BalanceResponse.newBuilder().setValue(ledger.getBalance(request.getUserId())).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
    
    @Override
    public void createAccount(CreateAccountRequest request, StreamObserver<CreateAccountResponse> responseObserver) {
        
        if(ledger.createAccount(request.getUserId()) != 0){
            responseObserver.onError(new Exception(ALREADY_EXISTS.withDescription("User already exists").asRuntimeException()));
        } else {
            CreateAccountResponse response = CreateAccountResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

    }

    @Override
    public void deleteAccount(DeleteAccountRequest request, StreamObserver<DeleteAccountResponse> responseObserver) {
        int res = ledger.deleteAccount(request.getUserId());
        switch (res) {
            case 0:
                DeleteAccountResponse response = DeleteAccountResponse.newBuilder().build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                break;
            case -1:
                responseObserver.onError(new Exception(NOT_FOUND.withDescription("User not found").asRuntimeException()));
                break;
            case -2:
                responseObserver.onError(new Exception(INVALID_ARGUMENT.withDescription("Balance must be 0").asRuntimeException()));
                break;
        
            default:
                responseObserver.onError(new Exception(UNKNOWN.withDescription("Failed to delete account").asRuntimeException()));
                break;
        }
        
    }

    @Override
    public void transferTo(TransferToRequest request, StreamObserver<TransferToResponse> responseObserver) {
        int res = ledger.transferTo(request.getAccountFrom(), request.getAccountTo(), request.getAmount());

        switch (res) {
            case 0:
                TransferToResponse response = TransferToResponse.newBuilder().build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                break;
            case -1:
                responseObserver.onError(new Exception(NOT_FOUND.withDescription("User not found").asRuntimeException()));
                break;
            case -2:
                responseObserver.onError(new Exception(INVALID_ARGUMENT.withDescription("Can't transfer to same account").asRuntimeException()));
                break;
            case -3:
                responseObserver.onError(new Exception(INVALID_ARGUMENT.withDescription("Invalid amount").asRuntimeException()));
                break;
            case -4:
                responseObserver.onError(new Exception(INVALID_ARGUMENT.withDescription("Not enough balance").asRuntimeException()));
                break;

            default:
                responseObserver.onError(new Exception(UNKNOWN.withDescription("Transfer failed").asRuntimeException()));
                break;
        }
    }


}
