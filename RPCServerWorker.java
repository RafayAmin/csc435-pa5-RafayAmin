package csc435.app;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RPCServerWorker implements Runnable {
    private final IndexStore store;
    private final int port; // The port on which the gRPC server will listen
    private Server grpcServer; // The gRPC server instance

    public RPCServerWorker(IndexStore store, int port) {
        this.store = store;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            // Build and start the gRPC server
            grpcServer = ServerBuilder.forPort(port)
                .addService(new FileRetrievalEngineService(store)) // Register the service
                .build()
                .start();

            System.out.printf("gRPC Server started on port %d%n", port);

            // Keep the server running
            grpcServer.awaitTermination();

        } catch (IOException e) {
            System.err.printf("Failed to start gRPC Server on port %d: %s%n", port, e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("gRPC Server interrupted, shutting down...");
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
    }

    public void shutdown() {
        if (grpcServer != null && !grpcServer.isShutdown()) {
            grpcServer.shutdown(); // Initiate a graceful shutdown
            try {
                // Wait for the server to terminate within the timeout
                if (!grpcServer.awaitTermination(30, TimeUnit.SECONDS)) {
                    System.out.println("gRPC Server is taking too long to shut down, forcing shutdown...");
                    grpcServer.shutdownNow(); // Force shutdown if it takes too long
                }
                System.out.println("gRPC Server has been shut down successfully.");
            } catch (InterruptedException e) {
                System.err.println("Shutdown interrupted, forcing shutdown...");
                grpcServer.shutdownNow(); // Force shutdown on interrupt
                Thread.currentThread().interrupt(); // Restore interrupted status
            }
        } else {
            System.out.println("gRPC Server is already shut down.");
        }
    }
}
