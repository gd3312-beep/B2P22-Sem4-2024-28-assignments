package com.sem4.assignments.problem4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PlagiarismDetector {
    private final int nGramSize;
    private final ConcurrentMap<String, Set<String>> ngramToDocumentIds = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Set<String>> documentToNgrams = new ConcurrentHashMap<>();

    public PlagiarismDetector(int nGramSize) {
        if (nGramSize < 2) {
            throw new IllegalArgumentException("nGramSize must be at least 2");
        }
        this.nGramSize = nGramSize;
    }

    public void indexDocument(String documentId, String content) {
        String normalizedDocumentId = normalizeDocumentId(documentId);
        Set<String> ngrams = extractNgrams(content);
        documentToNgrams.put(normalizedDocumentId, ngrams);

        for (String ngram : ngrams) {
            ngramToDocumentIds.computeIfAbsent(ngram, key -> ConcurrentHashMap.newKeySet()).add(normalizedDocumentId);
        }
    }

    public AnalysisReport analyzeDocument(String submittedDocumentId, String content) {
        String normalizedDocumentId = normalizeDocumentId(submittedDocumentId);
        Set<String> extractedNgrams = extractNgrams(content);
        Map<String, Integer> matchCounts = new HashMap<>();

        for (String ngram : extractedNgrams) {
            Set<String> candidateDocuments = ngramToDocumentIds.getOrDefault(ngram, Collections.emptySet());
            for (String candidateDocument : candidateDocuments) {
                if (!candidateDocument.equals(normalizedDocumentId)) {
                    matchCounts.merge(candidateDocument, 1, Integer::sum);
                }
            }
        }

        List<SimilarityResult> results = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : matchCounts.entrySet()) {
            String candidateDocument = entry.getKey();
            int matches = entry.getValue();
            int candidateNgramCount = documentToNgrams.getOrDefault(candidateDocument, Collections.emptySet()).size();
            double similarity = computeSimilarityPercent(matches, extractedNgrams.size(), candidateNgramCount);
            boolean suspicious = similarity >= 15.0;
            boolean plagiarismDetected = similarity >= 60.0;
            results.add(new SimilarityResult(candidateDocument, matches, similarity, suspicious, plagiarismDetected));
        }

        results.sort(Comparator
                .comparingDouble(SimilarityResult::similarityPercent)
                .thenComparingInt(SimilarityResult::matchingNgrams)
                .reversed());

        return new AnalysisReport(extractedNgrams.size(), results);
    }

    private double computeSimilarityPercent(int matches, int countA, int countB) {
        int denominator = Math.max(Math.max(countA, countB), 1);
        return (matches * 100.0) / denominator;
    }

    private Set<String> extractNgrams(String content) {
        String normalized = content == null ? "" : content.toLowerCase().replaceAll("[^a-z0-9\\s]", " ").trim();
        if (normalized.isBlank()) {
            return Collections.emptySet();
        }

        String[] words = normalized.split("\\s+");
        if (words.length < nGramSize) {
            return Collections.emptySet();
        }

        Set<String> ngrams = new HashSet<>();
        for (int i = 0; i <= words.length - nGramSize; i++) {
            StringBuilder builder = new StringBuilder();
            for (int j = 0; j < nGramSize; j++) {
                if (j > 0) {
                    builder.append(' ');
                }
                builder.append(words[i + j]);
            }
            ngrams.add(builder.toString());
        }

        return ngrams;
    }

    private String normalizeDocumentId(String documentId) {
        return documentId == null ? "" : documentId.trim();
    }

    public record AnalysisReport(int extractedNgrams, List<SimilarityResult> matches) { }

    public record SimilarityResult(
            String documentId,
            int matchingNgrams,
            double similarityPercent,
            boolean suspicious,
            boolean plagiarismDetected
    ) { }
}
