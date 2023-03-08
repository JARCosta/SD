package pt.tecnico.distledger.server.domain;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc.UserServiceImplBase;

import static io.grpc.Status.*;

public class UserServiceImpl extends UserServiceImplBase{
    private ServerState ledger = new ServerState();

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

        if(ledger.deleteAccount(request.getUserId()) != 0){
            responseObserver.onError(new Exception(NOT_FOUND.withDescription("User not found").asRuntimeException()));
        } else {
            DeleteAccountResponse response = DeleteAccountResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void transferTo(TransferToRequest request, StreamObserver<TransferToResponse> responseObserver) {
        
        if(ledger.transferTo(request.getAccountFrom(), request.getAccountTo(), request.getAmount()) != 0){
            responseObserver.onError(new Exception(NOT_FOUND.withDescription("User not found").asRuntimeException()));
        } else {
            TransferToResponse response = TransferToResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }


}
