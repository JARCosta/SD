package pt.tecnico.distledger.userclient.grpc;

import java.util.List;

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

    public List<Integer> createAccount(String username, List<Integer> TS) {        
        try{
            CreateAccountResponse result = stub.createAccount(CreateAccountRequest.newBuilder()
                    .setUserId(username)
                    .addAllPrevTS(TS)
                    .build());
            System.out.println(result == null ? "null" : "OK");
            return result.getTSList();
        }
        catch (StatusRuntimeException e){
            System.out.println(e.getStatus().getDescription());
        }
        return TS;
    }

    public List<Integer> balance(String username, List<Integer> TS) {
        try{
            BalanceResponse result = stub.balance(BalanceRequest.newBuilder()
                    .setUserId(username)
                    .addAllPrevTS(TS)
                    .build());
            System.out.println(result == null ? "null" : "OK");
            if(result.getValue() > 0)
                System.out.println(result.getValue());
        }
        catch (StatusRuntimeException e){
            System.out.println(e.getStatus().getDescription());
        }
        return TS;
    }


    public List<Integer> transferTo(String from, String dest, int amount, List<Integer> TS) {
        try{
            TransferToResponse result = stub.transferTo(TransferToRequest.newBuilder()
                    .setAccountFrom(from)
                    .setAccountTo(dest)
                    .setAmount(amount)
                    .addAllPrevTS(TS)
                    .build());
            System.out.println(result == null ? "null" : "OK");
            return result.getTSList();
        }
        catch (StatusRuntimeException e){
            System.out.println(e.getStatus().getDescription());
        }
        return TS;
    }

}
