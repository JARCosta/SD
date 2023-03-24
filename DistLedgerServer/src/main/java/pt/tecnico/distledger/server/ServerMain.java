package pt.tecnico.distledger.server;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import pt.tecnico.distledger.server.domain.AdminServiceImpl;
import pt.tecnico.distledger.server.domain.DistLedgerCrossServerServiceImpl;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.UserServiceImpl;
import pt.tecnico.distledger.server.grpc.NamingServerService;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;

public class ServerMain {

	private final static String host = "localhost";
	private final static String serviceName = "DistLedgerServerService";

	public static void main(String[] args) throws IOException, InterruptedException {

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

		ServerState ledger;
		// Create new server state
		if (qualifier.equals("A")) {
			ledger = new ServerState(namingServerService, serviceName, qualifier);
		} else {
			ledger = new ServerState();
		}

		final BindableService userService = new UserServiceImpl(ledger);
		final BindableService adminService = new AdminServiceImpl(ledger);
		final BindableService distLedgerCrossServerServiceImpl = new DistLedgerCrossServerServiceImpl(ledger);

		// Create a new server to listen on port
		Server server = ServerBuilder.forPort(Integer.parseInt(port)).addService(userService).addService(adminService).addService(distLedgerCrossServerServiceImpl)
				.build();

		server.start();
		System.out.println("Server started");

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Press enter to confirm shutdown");
			try {
				System.in.read();
				// Delete server from naming server
				namingServerService.delete(serviceName, address);
				namingServerService.shutdownNowChannel();
				server.shutdown();
				server.awaitTermination();
			} catch (IOException | InterruptedException e) {
				System.err.println(e.getMessage());
			}
		}));
	
		// Wait for termination signal
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
		}
	}

}
