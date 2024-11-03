package csc435.app;

public class ServerProcessingEngine {
    private final IndexStore store; // Reference to the IndexStore
    private RPCServerWorker rpcWorker; // gRPC Server worker object
    private Thread grpcServerThread; // Thread running the gRPC server

    public ServerProcessingEngine(IndexStore store) {
        this.store = store;
    }

    // Initialize and start the gRPC Server
    public void initialize(int serverPort) {
        if (rpcWorker == null && grpcServerThread == null) {
            rpcWorker = new RPCServerWorker(store, serverPort);
            grpcServerThread = new Thread(rpcWorker);
            grpcServerThread.start(); // Start the gRPC server thread
            System.out.printf("gRPC Server initialized and started on port %d%n", serverPort);
        } else {
            System.out.println("gRPC Server is already initialized.");
        }
    }

    // Shutdown the gRPC Server
    public void shutdown() {
        if (rpcWorker != null) {
            rpcWorker.shutdown(); // Call shutdown on the RPCServerWorker
        }
        if (grpcServerThread != null) {
            try {
                grpcServerThread.join(); // Wait for the server thread to finish
            } catch (InterruptedException e) {
                System.err.println("Error while shutting down gRPC server thread: " + e.getMessage());
                Thread.currentThread().interrupt(); // Restore interrupted status
            }
            grpcServerThread = null; // Clear reference to the thread
        }
        rpcWorker = null; // Clear reference to the worker
        System.out.println("gRPC Server shutdown completed.");
    }

    // Initiate indexing of a document
    public void computeIndex(String filePath) {
        // Here you would implement the logic to add the file to the IndexStore
        System.out.println("Indexing file at path: " + filePath);
        // Potentially call methods on the store to handle indexing
    }

    // Perform a search and return results
    public String computeSearch(String searchTerm) {
        // Implement logic to perform search and retrieve results from IndexStore
        System.out.println("Searching for term: " + searchTerm);
        // Placeholder for actual search logic
        return "Search results for term: " + searchTerm; // Modify this to return real results
    }
}
