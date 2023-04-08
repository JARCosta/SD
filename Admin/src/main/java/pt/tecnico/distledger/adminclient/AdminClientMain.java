package pt.tecnico.distledger.adminclient;


public class AdminClientMain {
    
    public static void main(String[] args) {

        System.out.println(AdminClientMain.class.getSimpleName());

        CommandParser parser = new CommandParser();
        parser.parseInput();

    }
}
