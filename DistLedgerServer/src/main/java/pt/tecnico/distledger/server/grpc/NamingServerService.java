package pt.tecnico.distledger.server.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.NamingServerServiceGrpc;
//import pt.ulisboa.tecnico.distledger.contract.distledgerserver.NamingServerService.*;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.NamingServerServiceOuterClass.RegisterResponse;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.NamingServerServiceOuterClass.RegisterRequest;

public class NamingServerService {
    private NamingServerServiceGrpc.NamingServerServiceBlockingStub stub;
    private final ManagedChannel channel;

    public NamingServerService(String host, int port) {
        final String target = host + ":" + port;
//        Debug.debug("Target: " + target);

        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        stub = NamingServerServiceGrpc.newBlockingStub(channel);
    }

    public void shutdownNowChannel() {
        channel.shutdownNow();
    }

    public void register(String serviceName, String qualifier, String address){
        try{
            RegisterResponse result = stub.register(RegisterRequest.newBuilder()
                    .setServiceName(serviceName)
                    .setQualifier(qualifier)
                    .setAddress(address)
                    .build());
            System.out.println(result == null ? "null" : "OK");
        }
        catch (StatusRuntimeException e){
            System.out.println(e.getStatus().getDescription());
        }
    }
/*
    public void delete(){

    }

    public void lookup(){

    }
*/
}
