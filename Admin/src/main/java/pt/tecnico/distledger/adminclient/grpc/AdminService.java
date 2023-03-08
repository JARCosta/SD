package pt.tecnico.distledger.adminclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.*;
public class AdminService {

    /* TODO: The gRPC client-side logic should be here.
        This should include a method that builds a channel and stub,
        as well as individual methods for each remote operation of this service. */
    private AdminServiceGrpc.AdminServiceBlockingStub stub;

    public AdminService(String host, int port) {
        final String target = host + ":" + port;
        createStub(target);
    }
    
    public void createStub(final String target) {
		final ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        stub = AdminServiceGrpc.newBlockingStub(channel);
    }

    public void activate(){
        try{
            ActivateResponse result =  stub.activate(ActivateRequest.newBuilder().build());
            System.out.println(result == null ? "null" : "OK");
        }
        catch (StatusRuntimeException e){
            System.out.println(e.getStatus().getDescription());
        }
    }

    public void deactivate(){
        try{
            DeactivateResponse result =  stub.deactivate(DeactivateRequest.newBuilder().build());
            System.out.println(result == null ? "null" : "OK");
        }
        catch (StatusRuntimeException e){
            System.out.println(e.getStatus().getDescription());
        }
    }

    public void getLedgerState() {
        try{
            getLedgerStateResponse result =  stub.getLedgerState(getLedgerStateRequest.newBuilder().build());
            System.out.println(result == null ? "null" : "OK");
            // System.out.println(result.getState());
        }
        catch (StatusRuntimeException e){
            System.out.println(e.getStatus().getDescription());
        }
    }


}
