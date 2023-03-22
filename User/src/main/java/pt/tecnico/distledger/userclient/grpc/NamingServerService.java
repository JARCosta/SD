package pt.tecnico.distledger.userclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;

import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceOuterClass;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceOuterClass.LookupResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceOuterClass.LookupRequest;

import java.util.List;

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

    public List<String> lookup(String serviceName, String qualifier){
        try{
            LookupResponse result = stub.lookup(LookupRequest.newBuilder()
                    .setServiceName(serviceName)
                    .setQualifier(qualifier)
                    .build());
            System.out.println(result == null ? "null" : "OK");

            List<String> servers = result.getServersList();
            for (String server : servers) {
                System.out.println(server);
            }

            return servers;
        }
        catch (StatusRuntimeException e){
            System.out.println(e.getStatus().getDescription());
            return null;
        }
    }

}