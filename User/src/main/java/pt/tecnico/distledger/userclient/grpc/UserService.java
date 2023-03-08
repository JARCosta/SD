package pt.tecnico.distledger.userclient.grpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.*;


public class UserService {

    /* TODO: The gRPC client-side logic should be here.
        This should include a method that builds a channel and stub,
        as well as individual methods for each remote operation of this service. */
    private UserServiceGrpc.UserServiceBlockingStub stub;
    
    public UserService(String host, int port) {
        final String target = host + ":" + port;
        createStub(target);
    }

    public void createStub(final String target) {

		// Channel is the abstraction to connect to a service endpoint.
		// Let us use plaintext communication because we do not have certificates.
		final ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();

		// It is up to the client to determine whether to block the call.
		// Here we create a blocking stub, but an async stub,
		// or an async stub with Future are always possible.
        stub = UserServiceGrpc.newBlockingStub(channel);

    }

    public void createAccount(String username) {        
        try{
            CreateAccountResponse result =  stub.createAccount(CreateAccountRequest.newBuilder().setUserId(username).build());
            System.out.println(result == null ? "null" : "OK");
        }
        catch (StatusRuntimeException e){
            System.out.println(e.getStatus().getDescription());
        }
    }

    public void deleteAccount(String username) {
        try{
            DeleteAccountResponse result =  stub.deleteAccount(DeleteAccountRequest.newBuilder().setUserId(username).build());
            System.out.println(result == null ? "null" : "OK");
        }
        catch (StatusRuntimeException e){
            System.out.println(e.getStatus().getDescription());
        }
    }


    public void balance(String username) {
        try{
            BalanceResponse result = stub.balance(BalanceRequest.newBuilder().setUserId(username).build());
            System.out.println(result == null ? "null" : "OK");
            System.out.println(result.getValue());
        }
        catch (StatusRuntimeException e){
            System.out.println(e.getStatus().getDescription());
        }
    }


    public void transferTo(String from, String dest, int amount) {
        try{
            TransferToResponse result = stub.transferTo(TransferToRequest.newBuilder().setAccountFrom(from).setAccountTo(dest).setAmount(amount).build());
            System.out.println(result == null ? "null" : "OK");
        }
        catch (StatusRuntimeException e){
            System.out.println(e.getStatus().getDescription());
        }
    }

}
