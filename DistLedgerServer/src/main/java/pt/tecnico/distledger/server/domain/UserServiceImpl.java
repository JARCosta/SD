package pt.tecnico.distledger.server.domain;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.Debug;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc.UserServiceImplBase;

import static io.grpc.Status.*;

public class UserServiceImpl extends UserServiceImplBase{
    private ServerState ledger;

    public UserServiceImpl(ServerState serverState) {
        this.ledger = serverState;
    }

    @Override
    public synchronized void balance(BalanceRequest request, StreamObserver<BalanceResponse> responseObserver) {
        Debug.debug("Received balance request for " + request.getUserId() + ".");

        int res = ledger.getBalance(request.getUserId());
        switch (res) {
            case -1:
                responseObserver.onError(
                        new Exception(CANCELLED.withDescription("UNAVAILABLE").asRuntimeException()));
                break;
            case -2:
                responseObserver.onError(
                        new Exception(NOT_FOUND.withDescription("User not found").asRuntimeException()));
                break;

            default:
                BalanceResponse response = BalanceResponse.newBuilder()
                        .setValue(ledger.getBalance(request.getUserId()))
                        .build();
                Debug.debug("Sending response.");
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                Debug.debug("Request handled.");
                break;
        }
        
    }
    
    @Override
    public synchronized void createAccount(CreateAccountRequest request,
                                           StreamObserver<CreateAccountResponse> responseObserver) {
        Debug.debug("Received create account request with name " + request.getUserId() + ".");

        int res = ledger.createAccount(request.getUserId());
        switch (res) {
            case 0:
                CreateAccountResponse response = CreateAccountResponse.newBuilder().build();
                Debug.debug("Sending response.");
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                Debug.debug("Request handled.");
                break;
            case -1:
                responseObserver.onError(
                        new Exception(CANCELLED.withDescription("UNAVAILABLE").asRuntimeException()));
                break;
            case -2:
                responseObserver.onError(
                        new Exception(ALREADY_EXISTS.withDescription("User already exists").asRuntimeException()));
                break;

            default:
                responseObserver.onError(
                        new Exception(UNKNOWN.withDescription("Failed to create account").asRuntimeException()));
                break;
        }

    }

    @Override
    public synchronized void deleteAccount(DeleteAccountRequest request,
                                           StreamObserver<DeleteAccountResponse> responseObserver) {
        Debug.debug("Received delete account request for " + request.getUserId() + ".");

        int res = ledger.deleteAccount(request.getUserId());
        switch (res) {
            case 0:
                DeleteAccountResponse response = DeleteAccountResponse.newBuilder().build();
                Debug.debug("Sending response.");
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                Debug.debug("Request handled.");
                break;
            case -1:
                responseObserver.onError(
                        new Exception(CANCELLED.withDescription("UNAVAILABLE").asRuntimeException()));
            case -2:
                responseObserver.onError(
                        new Exception(NOT_FOUND.withDescription("User not found").asRuntimeException()));
                break;
            case -3:
                responseObserver.onError(
                        new Exception(INVALID_ARGUMENT.withDescription("Balance must be 0").asRuntimeException()));
                break;
        
            default:
                responseObserver.onError(
                        new Exception(UNKNOWN.withDescription("Failed to delete account").asRuntimeException()));
                break;
        }
        
    }

    @Override
    public synchronized void transferTo(TransferToRequest request,
                                        StreamObserver<TransferToResponse> responseObserver) {
        Debug.debug("Received transfer request of " + request.getAmount()
                + " from " + request.getAccountFrom() + " to " + request.getAccountTo() + ".");

        int res = ledger.transferTo(request.getAccountFrom(), request.getAccountTo(), request.getAmount());
        switch (res) {
            case 0:
                TransferToResponse response = TransferToResponse.newBuilder().build();
                Debug.debug("Sending response.");
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                Debug.debug("Request handled.");
                break;
            case -1:
                responseObserver.onError(
                        new Exception(CANCELLED.withDescription("UNAVAILABLE").asRuntimeException()));
                break;
            case -2:
                responseObserver.onError(
                        new Exception(NOT_FOUND.withDescription("User not found").asRuntimeException()));
                break;
            case -3:
                responseObserver.onError(
                        new Exception(INVALID_ARGUMENT.withDescription("Can't transfer to same account").asRuntimeException()));
                break;
            case -4:
                responseObserver.onError(
                        new Exception(INVALID_ARGUMENT.withDescription("Invalid amount").asRuntimeException()));
                break;
            case -5:
                responseObserver.onError(
                        new Exception(INVALID_ARGUMENT.withDescription("Not enough balance").asRuntimeException()));
                break;

            default:
                responseObserver.onError(
                        new Exception(UNKNOWN.withDescription("Transfer failed").asRuntimeException()));
                break;
        }

    }

}
