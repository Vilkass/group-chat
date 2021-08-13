package config;

public class ServerConfig {

    public static final String ADDRESS = "localhost";
    public static final int PORT = 9090;

    // Server commands
    public static String HELP_COMMAND = "/help";
    public static String ALL_USERS = "/allUsers";
    public static String DISCONNECT = "/disconnect";
    public static String ALL_COMMANDS = "\n =======================\n Commands Available:\n "+
            ALL_USERS+" - Displays all users online"+
            "\n =======================";

}
