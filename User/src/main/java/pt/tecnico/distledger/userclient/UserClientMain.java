package pt.tecnico.distledger.userclient;

import pt.tecnico.distledger.userclient.grpc.UserService;
import pt.tecnico.distledger.userclient.grpc.NamingServerService;

import java.util.*;

public class UserClientMain {
    
    public static void main(String[] args) {

        System.out.println(UserClientMain.class.getSimpleName());

        CommandParser parser = new CommandParser();
        parser.parseInput();
        
    }
}
