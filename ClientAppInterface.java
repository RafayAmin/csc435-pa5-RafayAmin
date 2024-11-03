package csc435.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ClientAppInterface {
    private ClientProcessingEngine engine;
    private boolean isConnected;  // Flag to track the connection state

    public ClientAppInterface(ClientProcessingEngine engine) {
        this.engine = engine;
        this.isConnected = false;  // Initially, the client is not connected
    }

    public void readCommands() {
        Scanner sc = new Scanner(System.in);
        String command;
        
        while (true) {
            System.out.print("> ");
            
            // Read command from the command line
            command = sc.nextLine().trim();

            // If the command is "quit", terminate the program
            if (command.equalsIgnoreCase("quit")) {
                System.out.println("Terminating the client application.");
                break;
            }

            // If the command begins with "connect", connect to the given server
            if (command.startsWith("connect")) {
                String[] parts = command.split(" ");
                if (parts.length == 3) {
                    String serverIP = parts[1];
                    String serverPortString = parts[2];
                    try {
                        int serverPort = Integer.parseInt(serverPortString); // Convert to int
                        engine.connect(serverIP, serverPort);
                        isConnected = true;  // Set the flag to true upon successful connection
                        System.out.println("Connected to server at " + serverIP + ":" + serverPort);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid port number: " + serverPortString);
                    } catch (Exception e) {
                        System.out.println("Failed to connect to server at " + serverIP + ":" + serverPortString);
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Invalid connect command. Usage: connect <server_ip> <server_port>");
                }
                continue;
            }
            
            // If the command begins with "index", index the files from the specified directory
            if (command.startsWith("index")) {
                if (!isConnected) {
                    System.out.println("Please connect to a server first.");
                    continue;
                }
                
                String[] parts = command.split(" ", 2);
                if (parts.length == 2) {
                    String directoryPath = parts[1];
                    try {
                        IndexResult result = engine.indexFiles(directoryPath); 
                        System.out.println("Directory indexed: " + directoryPath);
                        System.out.println("Execution Time: " + result.executionTime + " seconds");
                        System.out.println("Total Bytes Processed: " + result.totalBytesRead + " bytes");
                    } catch (Exception e) {
                        System.out.println("Failed to index directory: " + directoryPath);
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Invalid index command. Usage: index <directory_path>");
                }
                continue;
            }

            // If the command begins with "search", search for files that match the query
            if (command.startsWith("search")) {
                if (!isConnected) {
                    System.out.println("Please connect to a server first.");
                    continue;
                }
                
                String[] parts = command.split(" ", 2);
                if (parts.length == 2) {
                    ArrayList<String> searchTerms = new ArrayList<>(Arrays.asList(parts[1].split(" ")));
                    try {
                        SearchResult result = engine.searchFiles(searchTerms); 
                        System.out.println("Search Execution Time: " + result.executionTime + " seconds");
                        result.documentFrequencies.forEach(pair -> {
                            System.out.println("File: " + pair.documentPath + ", Frequency: " + pair.wordFrequency);
                        });
                    } catch (Exception e) {
                        System.out.println("Failed to search for terms: " + searchTerms);
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Invalid search command. Usage: search <term1> <term2> ...");
                }
                continue;
            }

            System.out.println("Unrecognized command! Available commands: 'connect <server_ip> <server_port>', 'index <directory_path>', 'search <term1> <term2> ...', 'quit'.");
        }

        sc.close();
    }
}
