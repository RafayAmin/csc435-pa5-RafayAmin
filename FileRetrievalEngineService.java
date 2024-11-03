package csc435.app;

import io.grpc.stub.StreamObserver;
import java.util.ArrayList; 
import java.util.HashMap; 
import java.util.List; 
import java.util.Map; 

public class FileRetrievalEngineService extends FileRetrievalEngineGrpc.FileRetrievalEngineImplBase {
    private final IndexStore store;

    public FileRetrievalEngineService(IndexStore store) {
        this.store = store;
    }

    @Override
    public void computeIndex(IndexReq request, StreamObserver<IndexRep> responseObserver) {
        try {
            String documentPath = request.getDocumentPath();
            Map<String, Long> wordFrequencies = request.getWordFrequenciesMap();

            // Store the document and update the index with the word frequencies
            long documentNumber = store.putDocument(documentPath);
            store.updateIndex(documentNumber, new HashMap<>(wordFrequencies));

            // Build the response message
            IndexRep response = IndexRep.newBuilder()
                    .setAck("Indexing completed successfully for " + documentPath)
                    .build();
            responseObserver.onNext(response);
        } catch (Exception e) {
            responseObserver.onError(e); // Pass the error to the client
            return; // Exit the method early
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void computeSearch(SearchReq request, StreamObserver<SearchRep> responseObserver) {
        try {
            List<String> searchTerms = request.getTermsList();
            Map<String, Long> results = new HashMap<>();

            // Perform search for each term and accumulate results
            for (String term : searchTerms) {
                ArrayList<DocFreqPair> pairs = store.lookupIndex(term);
                for (DocFreqPair pair : pairs) {
                    String document = store.getDocument(pair.documentNumber);
                    results.put(document, results.getOrDefault(document, 0L) + pair.wordFrequency);
                }
            }

            // Build the SearchRep response
            SearchRep.Builder responseBuilder = SearchRep.newBuilder();
            responseBuilder.putAllSearchResults(results);
            responseObserver.onNext(responseBuilder.build());
        } catch (Exception e) {
            responseObserver.onError(e); // Pass the error to the client
            return; // Exit the method early
        } finally {
            responseObserver.onCompleted();
        }
    }
}
