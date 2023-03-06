package pt.tecnico.distledger.server;
import static java.lang.System.*;

import java.io.IOException;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
// import pt.tecnico.distledger.userclient.grpc.UserService;

public class ServerMain {

    public static void main(String[] args) throws IOException, InterruptedException{

        /* TODO */

        System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}



		final int port = Integer.parseInt(args[0]);
		final BindableService impl = new UserServiceImpl();

        // Create a new server to listen on port
        Server server = ServerBuilder.forPort(port).addService(impl).build();

		// Start the server
		server.start();

		// Server threads are running in the background.
		System.out.println("Server started");

		// Do not exit the main thread. Wait until server is terminated.
		server.awaitTermination();
    }

}

