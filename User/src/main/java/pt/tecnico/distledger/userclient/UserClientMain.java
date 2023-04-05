package pt.tecnico.distledger.userclient;


public class UserClientMain {
    
    public static void main(String[] args) {

        System.out.println(UserClientMain.class.getSimpleName());

        CommandParser parser = new CommandParser();
        parser.parseInput();
        
    }
}
