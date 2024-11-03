package csc435.app;

import csc435.app.FileRetrievalEngineGrpc.FileRetrievalEngineBlockingStub;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class IndexResult {
    public double executionTime;
    public long totalBytesRead;

    public IndexResult(double executionTime, long totalBytesRead) {
        this.executionTime = executionTime;
        this.totalBytesRead = totalBytesRead;
    }
}

class DocPathFreqPair {
    public String documentPath;
    public long wordFrequency;

    public DocPathFreqPair(String documentPath, long wordFrequency) {
        this.documentPath = documentPath;
        this.wordFrequency = wordFrequency;
    }
}

class SearchResult {
    public double executionTime;
    public List<DocPathFreqPair> documentFrequencies;

    public SearchResult(double executionTime, List<DocPathFreqPair> documentFrequencies) {
        this.executionTime = executionTime;
        this.documentFrequencies = documentFrequencies;
    }
}

public class ClientProcessingEngine {
    // TO-DO keep track of the connection
    private ManagedChannel channel;
    private FileRetrievalEngineBlockingStub stub;

    public ClientProcessingEngine() { }

    public IndexResult indexFiles(String folderPath) {
        if (stub == null) {
            throw new IllegalStateException("Client is not connected. Call connect() before using this method.");
        }

        // TO-DO get the start time
        long startTime = System.currentTimeMillis();

        // TO-DO crawl the folder path and extract all file paths
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            throw new IllegalArgumentException("Provided folder path is invalid or does not exist.");
        }

        long totalBytesProcessed = 0;
        File[] files = folder.listFiles();
        IndexResult result = new IndexResult(0.0, 0);

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    totalBytesProcessed += file.length();
                    
                    // TO-DO extract all alphanumeric terms that are larger than 2 characters
                    HashMap<String, Long> termFrequencies = new HashMap<>();
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] terms = line.split("\\W+");
                            for (String term : terms) {
                                if (term.length() > 2) {
                                    termFrequencies.put(term, termFrequencies.getOrDefault(term, 0L) + 1);
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        continue; // Skip this file if there’s an error reading it
                    }

                    // TO-DO perform a remote procedure call to the server by calling the gRPC client stub
                    IndexReq request = IndexReq.newBuilder()
                            .setClientId(1)
                            .setDocumentPath(file.getAbsolutePath())
                            .putAllWordFrequencies(termFrequencies)
                            .build();

                    try {
                        IndexRep response = stub.computeIndex(request);
                        System.out.println("Indexing response: " + response.getAck());
                    } catch (Exception e) {
                        System.err.println("Error during indexing: " + e.getMessage());
                    }
                }
            }
        }

        // TO-DO get the stop time and calculate the execution time
        long endTime = System.currentTimeMillis();
        result.executionTime = (endTime - startTime) / 1000.0;
        result.totalBytesRead = totalBytesProcessed;

        // TO-DO return the execution time and the total number of bytes read
        return result;
    }

    public SearchResult searchFiles(List<String> terms) {
        if (stub == null) {
            throw new IllegalStateException("Client is not connected. Call connect() before using this method.");
        }

        // TO-DO get the start time
        long startTime = System.currentTimeMillis();
        SearchResult result = new SearchResult(0.0, new ArrayList<>());

        // TO-DO perform a remote procedure call to the server by calling the gRPC client stub
        SearchReq request = SearchReq.newBuilder()
                .addAllTerms(terms)
                .build();

        try {
            SearchRep response = stub.computeSearch(request);
            for (var entry : response.getSearchResultsMap().entrySet()) {
                result.documentFrequencies.add(new DocPathFreqPair(entry.getKey(), entry.getValue()));
            }
        } catch (Exception e) {
            System.err.println("Error during search: " + e.getMessage());
        }

        // TO-DO get the stop time and calculate the execution time
        long endTime = System.currentTimeMillis();
        result.executionTime = (endTime - startTime) / 1000.0;

        // TO-DO return the execution time and the top 10 documents and frequencies
        return result;
    }

    public void connect(String serverIP, int serverPort) {
        // TO-DO create communication channel with the gRPC Server
        channel = ManagedChannelBuilder.forAddress(serverIP, serverPort)
                .usePlaintext()
                .build();

        // TO-DO create gRPC client stub
        stub = FileRetrievalEngineGrpc.newBlockingStub(channel);
    }

    public void shutdown() {
        if (channel != null) {
            channel.shutdown();
            System.out.println("gRPC Client channel shut down.");
        }
    }
}
