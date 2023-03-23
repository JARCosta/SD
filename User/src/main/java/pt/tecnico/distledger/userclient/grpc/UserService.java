package pt.tecnico.distledger.userclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.distledger.userclient.Debug;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.*;

public class UserService {
    private UserServiceGrpc.UserServiceBlockingStub stub;
    private final ManagedChannel channel;
    
    public UserService(String target) {
//        final String target = host + ":" + port;
        Debug.debug("Target: " + target);

        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        stub = UserServiceGrpc.newBlockingStub(channel);
    }

    public void shutdownNowChannel() {
        channel.shutdownNow();
    }

    public void createAccount(String username) {        
        try{
            CreateAccountResponse result = stub.createAccount(CreateAccountRequest.newBuilder()
                    .setUserId(username)
                    .build());
            System.out.println(result == null ? "null" : "OK");
        }
        catch (StatusRuntimeException e){
            System.out.println(e.getStatus().getDescription());
        }
    }

    public void deleteAccount(String username) {
        try{
            DeleteAccountResponse result = stub.deleteAccount(DeleteAccountRequest.newBuilder()
                    .setUserId(username)
                    .build());
            System.out.println(result == null ? "null" : "OK");
        }
        catch (StatusRuntimeException e){
            System.out.println(e.getStatus().getDescription());
        }
    }


    public void balance(String username) {
        try{
            BalanceResponse result = stub.balance(BalanceRequest.newBuilder()
                    .setUserId(username)
                    .build());
            System.out.println(result == null ? "null" : "OK");
            if(result.getValue() > 0)
                System.out.println(result.getValue());
        }
        catch (StatusRuntimeException e){
            System.out.println(e.getStatus().getDescription());
        }
    }


    public void transferTo(String from, String dest, int amount) {
        try{
            TransferToResponse result = stub.transferTo(TransferToRequest.newBuilder()
                    .setAccountFrom(from)
                    .setAccountTo(dest)
                    .setAmount(amount)
                    .build());
            System.out.println(result == null ? "null" : "OK");
        }
        catch (StatusRuntimeException e){
            System.out.println(e.getStatus().getDescription());
        }
    }

}
