package pt.tecnico.distledger.server;

import java.io.IOException;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import pt.tecnico.distledger.server.domain.AdminServiceImpl;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.UserServiceImpl;
import pt.tecnico.distledger.server.grpc.NamingServerService;

public class ServerMain {

	public static void registerInNamingServer(String port, String qualifier){

		String host = "localhost";
		String address = host + ":" + port;
		Debug.debug("Address: " + address);

		String serviceName = "DistLedgerServerService";

		int namingServerPort = 5001;
		NamingServerService namingServerService = new NamingServerService(host, namingServerPort);

		namingServerService.register(serviceName, qualifier, address);

		namingServerService.shutdownNowChannel();

	}

    public static void main(String[] args) throws IOException, InterruptedException{

        System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// register in naming server
		registerInNamingServer(args[0], args[1]);

		ServerState ledger = new ServerState();

		final int port = Integer.parseInt(args[0]);
		final BindableService userService = new UserServiceImpl(ledger);
		final BindableService adminService = new AdminServiceImpl(ledger);

        // Create a new server to listen on port
        Server server = ServerBuilder.forPort(port).addService(userService).addService(adminService).build();

		// Start the server
		server.start();

		// Server threads are running in the background.
		System.out.println("Server started");

		// Do not exit the main thread. Wait until server is terminated.
		server.awaitTermination();
    }
}

