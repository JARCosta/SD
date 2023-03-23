package pt.tecnico.distledger.server.grpc;

import java.util.List;

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

    public List<String> lookup(String serviceName, String qualifier){
        try{
            LookupResponse result = stub.lookup(LookupRequest.newBuilder()
                    .setServiceName(serviceName)
                    .setQualifier(qualifier)
                    .build());
            System.out.println(result == null ? "null" : result.getServersList());
            return result.getServersList();
        }
        catch (StatusRuntimeException e){
            System.out.println(e.getStatus().getDescription());
            throw e;
        }
    }

}
