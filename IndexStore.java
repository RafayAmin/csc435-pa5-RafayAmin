package csc435.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Data structure that stores a document number and the number of times a word/term appears in the document
class DocFreqPair {
    public final long documentNumber;
    public final long wordFrequency;

    public DocFreqPair(long documentNumber, long wordFrequency) {
        this.documentNumber = documentNumber;
        this.wordFrequency = wordFrequency;
    }
}

public class IndexStore {
    private final ConcurrentHashMap<Long, String> documentMap;
    private final ConcurrentHashMap<String, ArrayList<DocFreqPair>> termInvertedIndex;

    // Locks for synchronizing access to documentMap and termInvertedIndex
    private final Lock documentMapLock = new ReentrantLock();
    private final Lock termInvertedIndexLock = new ReentrantLock();

    public IndexStore() {
        documentMap = new ConcurrentHashMap<>();
        termInvertedIndex = new ConcurrentHashMap<>();
    }

    public long putDocument(String documentPath) {
        long documentNumber;

        // Use lock to ensure only one thread can access this method at a time
        documentMapLock.lock();
        try {
            documentNumber = documentMap.size() + 1; // Generate a unique document number
            documentMap.put(documentNumber, documentPath);
        } finally {
            documentMapLock.unlock(); // Ensure the lock is released
        }

        return documentNumber;
    }

    public String getDocument(long documentNumber) {
        return documentMap.get(documentNumber);
    }

    public void updateIndex(long documentNumber, HashMap<String, Long> wordFrequencies) {
        // Use lock to ensure only one thread can access this method at a time
        termInvertedIndexLock.lock();
        try {
            for (String term : wordFrequencies.keySet()) {
                long frequency = wordFrequencies.get(term);
                termInvertedIndex.computeIfAbsent(term, k -> new ArrayList<>())
                        .add(new DocFreqPair(documentNumber, frequency));
            }
        } finally {
            termInvertedIndexLock.unlock(); // Ensure the lock is released
        }
    }

    public ArrayList<DocFreqPair> lookupIndex(String term) {
        return new ArrayList<>(termInvertedIndex.getOrDefault(term, new ArrayList<>()));
    }
}
