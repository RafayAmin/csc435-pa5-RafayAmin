package csc435.app;

import java.util.ArrayList;
import java.util.List;

class BenchmarkWorker implements Runnable {
    // Declare a ClientProcessingEngine
    private ClientProcessingEngine clientEngine;
    private String datasetPath;
    private String serverIP;
    private int serverPort;

    public BenchmarkWorker(String datasetPath, String serverIP, int serverPort) {
        this.datasetPath = datasetPath;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        // Create a ClientProcessingEngine
        clientEngine = new ClientProcessingEngine();
        try {
            // Connect the ClientProcessingEngine to the server
            clientEngine.connect(serverIP, serverPort);

            // Index the dataset
            IndexResult indexResult = clientEngine.indexFiles(datasetPath);
            System.out.printf("Indexing completed for %s. Execution Time: %.2f seconds, Total Bytes Read: %d bytes\n",
                    datasetPath, indexResult.executionTime, indexResult.totalBytesRead);

            // Perform search operations
            search();
        } catch (Exception e) {
            System.err.println("Error during benchmarking: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    public void search() {
        // Example search terms
        ArrayList<String> searchTerms = new ArrayList<>();
        searchTerms.add("exampleTerm");
        SearchResult searchResult = clientEngine.searchFiles(searchTerms);
        System.out.printf("Search completed. Execution Time: %.2f seconds, Results: %d documents\n",
                searchResult.executionTime, searchResult.documentFrequencies.size());
    }

    public void disconnect() {
        // Disconnect the ClientProcessingEngine from the server
        if (clientEngine != null) {
            clientEngine.shutdown();
        }
    }
}

public class FileRetrievalBenchmark {
    public static void main(String[] args) {
        String serverIP;
        int serverPort;
        int numberOfClients;
        ArrayList<String> clientsDatasetPath = new ArrayList<>();

        // Extract the arguments from args
        if (args.length < 3) {
            System.err.println("Usage: java FileRetrievalBenchmark <serverIP> <serverPort> <numberOfClients> <datasetPath1> <datasetPath2> ...");
            return;
        }

        serverIP = args[0];
        serverPort = Integer.parseInt(args[1]);
        numberOfClients = Integer.parseInt(args[2]);

        for (int i = 3; i < args.length; i++) {
            clientsDatasetPath.add(args[i]);
        }

        // Measure the execution start time
        long startTime = System.currentTimeMillis();

        List<Thread> workerThreads = new ArrayList<>();
        List<BenchmarkWorker> workers = new ArrayList<>();

        for (String datasetPath : clientsDatasetPath) {
            // Create and start benchmark worker threads equal to the number of clients
            BenchmarkWorker worker = new BenchmarkWorker(datasetPath, serverIP, serverPort);
            Thread thread = new Thread(worker);
            workers.add(worker); // Keep a reference to the worker
            workerThreads.add(thread);
            thread.start();
        }

        // Join the benchmark worker threads
        for (Thread thread : workerThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.err.println("Thread interrupted: " + e.getMessage());
            }
        }

        // Measure the execution stop time and print the performance
        long endTime = System.currentTimeMillis();
        double executionTime = (endTime - startTime) / 1000.0;
        System.out.printf("Total Execution Time: %.2f seconds\n", executionTime);
        
        // Run search queries on the first client (if available)
        if (!workers.isEmpty()) {
            workers.get(0).search();  // Call search on the first worker
        }

        // Disconnect all clients (all benchmark worker threads)
        for (BenchmarkWorker worker : workers) {
            worker.disconnect();
        }
    }
}
