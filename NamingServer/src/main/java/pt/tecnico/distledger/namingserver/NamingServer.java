package pt.tecnico.distledger.namingserver;

import java.io.IOException;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import pt.tecnico.distledger.namingserver.domain.*;

import java.util.Map;
import java.util.HashMap;

public class NamingServer {

    public static void main(String[] args) throws IOException, InterruptedException {

        Map<String, ServiceEntry> services = new HashMap<String, ServiceEntry>();

        final int port = 5001;

        final BindableService namingServerService = new NamingServerServiceImpl(services);

        Server namingServer = ServerBuilder
                .forPort(port)
                .addService(namingServerService)
                .build();

        namingServer.start();

        System.out.println("Naming server started");

        namingServer.awaitTermination();
    }

}
