package BetProcessor;

/**
 * The Main class contains the entry point for the betting system application.
 * It creates a Repository object, processes the data, and writes the results to a file.
 */
public class Main {

    /**
     * The main method serves as the entry point for the betting system application.
     * It creates a Repository object, reads and processes data, and writes results to a file.
     * File paths are declared as constants in Repository class.
     *
     * @param args The command-line arguments (not used in this application).
     */
    public static void main(String[] args) {
        Repository r = new Repository();
        r.writeResultFile();
    }
}
