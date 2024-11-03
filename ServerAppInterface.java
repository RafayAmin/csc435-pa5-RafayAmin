package csc435.app;

import java.util.Scanner;

public class ServerAppInterface {
    private final ServerProcessingEngine engine; // Reference to the processing engine

    public ServerAppInterface(ServerProcessingEngine engine) {
        this.engine = engine;
    }

    public void readCommands() {
        Scanner sc = new Scanner(System.in);
        String command;

        while (true) {
            System.out.print("> "); // Prompt for user input
            command = sc.nextLine(); // Read command from the user

            // Check if the command is to quit
            if (command.equalsIgnoreCase("quit")) {
                System.out.println("Shutting down the server...");
                engine.shutdown(); // Shutdown the processing engine
                break; // Exit the loop
            } 
            // Check if the command is for indexing a file
            else if (command.startsWith("index ")) {
                String filePath = command.substring(6).trim(); // Extract the file path
                engine.computeIndex(filePath); // Call computeIndex on the engine
                System.out.println("Indexing initiated for file: " + filePath);
            } 
            // Check if the command is for searching a term
            else if (command.startsWith("search ")) {
                String searchTerm = command.substring(7).trim(); // Extract the search term
                String results = engine.computeSearch(searchTerm); // Call computeSearch on the engine
                System.out.println("Search results: " + results);
            } 
            // Handle unrecognized commands
            else {
                System.out.println("Unrecognized command! Available commands: 'index <file>', 'search <term>', 'quit'.");
            }
        }

        sc.close(); // Close the scanner resource
    }
}
