package pt.tecnico.distledger.server.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceOuterClass.*;

public class NamingServerService {

    private final String namingServerAddress = "localhost:5001";
    private final ManagedChannel channel;
    private NamingServerServiceGrpc.NamingServerServiceBlockingStub stub;

    public NamingServerService() {
        channel = ManagedChannelBuilder.forTarget(namingServerAddress).usePlaintext().build();
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

    public void delete(String serviceName, String address){
        try{
            DeleteResponse result = stub.delete(DeleteRequest.newBuilder()
                    .setServiceName(serviceName)
                    .setAddress(address)
                    .build());
            System.out.println(result == null ? "null" : "OK");
        }
        catch (StatusRuntimeException e){
            System.out.println(e.getStatus().getDescription());
        }
    }

}
