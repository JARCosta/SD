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

	private final static String host = "localhost";
	private final static String serviceName = "DistLedgerServerService";

    public static void main(String[] args) throws IOException, InterruptedException{

        System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		final String port = args[0];
		final String qualifier = args[1];
		final String address = host + ":" + port;

		// Register server in naming server
		NamingServerService namingServerService = new NamingServerService();
		namingServerService.register(serviceName, qualifier, address);

		// Create new server state
		ServerState ledger = new ServerState();

		final BindableService userService = new UserServiceImpl(ledger);
		final BindableService adminService = new AdminServiceImpl(ledger);

        // Create a new server to listen on port
        Server server = ServerBuilder.forPort(Integer.parseInt(port))
				.addService(userService)
				.addService(adminService)
				.build();

		server.start();
		System.out.println("Server started");

		System.out.println("Press enter to shutdown");
		System.in.read();

		// Delete server from naming server
		namingServerService.delete(serviceName, address);
		namingServerService.shutdownNowChannel();
		server.shutdown();
		server.awaitTermination();
	}

}

