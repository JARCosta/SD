package pt.tecnico.distledger.userclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.distledger.userclient.Debug;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.*;

public class UserService {
    private UserServiceGrpc.UserServiceBlockingStub stub_User_A;
    private UserServiceGrpc.UserServiceBlockingStub stub_A_B;
    private final ManagedChannel channel1;
    private final ManagedChannel channel2;
    
    public UserService(String targetA, String targetB) {
//        final String target = host + ":" + port;
        Debug.debug("Target: " + targetA);

        channel1 = ManagedChannelBuilder.forTarget(targetA).usePlaintext().build();
        channel2 = ManagedChannelBuilder.forTarget(targetB).usePlaintext().build(); 
        stub_User_A = UserServiceGrpc.newBlockingStub(channel1);
        stub_A_B = UserServiceGrpc.newBlockingStub(channel2);
        
    }

    public void shutdownNowChannel() {
        channel1.shutdownNow();
    }

    public void createAccount(String username) {        
        try{
            CreateAccountResponse result_A = stub_User_A.createAccount(CreateAccountRequest.newBuilder()
                    .setUserId(username)
                    .build());

            stub_A_B.createAccount(CreateAccountRequest.newBuilder().setUserId(username).build());


            System.out.println(result_A == null ? "null" : "OK");
        }
        catch (StatusRuntimeException e){
            System.out.println(e.getStatus().getDescription());
        }
    }

    public void deleteAccount(String username) {
        try{
            DeleteAccountResponse result_A = stub_User_A.deleteAccount(DeleteAccountRequest.newBuilder()
                    .setUserId(username)
                    .build());
            System.out.println(result_A == null ? "null" : "OK");
        }
        catch (StatusRuntimeException e){
            System.out.println(e.getStatus().getDescription());
        }
    }


    public void balance(String username) {
        try{
            BalanceResponse result_A = stub_User_A.balance(BalanceRequest.newBuilder()
                    .setUserId(username)
                    .build());
            System.out.println(result_A == null ? "null" : "OK");
            if(result_A.getValue() > 0)
                System.out.println(result_A.getValue());
        }
        catch (StatusRuntimeException e){
            System.out.println(e.getStatus().getDescription());
        }
    }


    public void transferTo(String from, String dest, int amount) {
        try{
            TransferToResponse result_A = stub_User_A.transferTo(TransferToRequest.newBuilder()
                    .setAccountFrom(from)
                    .setAccountTo(dest)
                    .setAmount(amount)
                    .build());
            System.out.println(result_A == null ? "null" : "OK");
        }
        catch (StatusRuntimeException e){
            System.out.println(e.getStatus().getDescription());
        }
    }

}
