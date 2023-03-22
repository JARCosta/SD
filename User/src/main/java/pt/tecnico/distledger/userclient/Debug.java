package pt.tecnico.distledger.userclient;

public class Debug {
    /** Set flag to true to print debug messages.
     * The flag can be set using the -Ddebug command line option. */
    private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

    /** Helper method to print debug messages. */
    public static void debug(String debugMessage) {
        if (DEBUG_FLAG)
            System.err.println(debugMessage);
    }
}
