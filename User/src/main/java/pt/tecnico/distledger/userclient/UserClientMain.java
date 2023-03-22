package pt.tecnico.distledger.userclient;

import pt.tecnico.distledger.userclient.grpc.UserService;
import pt.tecnico.distledger.userclient.grpc.NamingServerService;

import java.util.List;

public class UserClientMain {

    public static List<String> lookup(){

        String host = "localhost";
        int namingServerPort = 5001;
        NamingServerService namingServerService = new NamingServerService(host, namingServerPort);

        String serviceName = "DistLedgerServerService";
        String qualifier = "A";
        List<String> servers = namingServerService.lookup(serviceName, qualifier);

        return servers;
    }
    
    public static void main(String[] args) {

        System.out.println(UserClientMain.class.getSimpleName());
/*
        // receive and print arguments
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        // check arguments
        if (args.length != 2) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: mvn exec:java -Dexec.args=<host> <port>");
            return;
        }

        final String host = args[0];
        final int port = Integer.parseInt(args[1]);
        */

        final List<String> servers = lookup();

        if (servers.isEmpty()) {
            Debug.debug("No servers with the name and/or qualifier");
            System.exit(0);
        }

        UserService userService = new UserService(servers.get(0));
        CommandParser parser = new CommandParser(userService);
        parser.parseInput();
        
		// A Channel should be shutdown before stopping the process.
		userService.shutdownNowChannel();

    }
}
