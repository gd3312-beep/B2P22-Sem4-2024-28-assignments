package com.sem4.assignments.problem7;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class AutocompleteSystem {
    private final TrieNode root = new TrieNode();
    private final ConcurrentMap<String, AtomicInteger> queryFrequency = new ConcurrentHashMap<>();

    public void addQuery(String query) {
        updateFrequency(query, 1);
    }

    public void updateFrequency(String query, int delta) {
        String normalized = normalize(query);
        if (normalized.isBlank() || delta <= 0) {
            return;
        }

        queryFrequency.computeIfAbsent(normalized, key -> new AtomicInteger()).addAndGet(delta);

        synchronized (root) {
            TrieNode node = root;
            for (char ch : normalized.toCharArray()) {
                node = node.children.computeIfAbsent(ch, key -> new TrieNode());
                node.queries.add(normalized);
            }
        }
    }

    public List<Suggestion> search(String prefix, int topK) {
        String normalizedPrefix = normalize(prefix);
        if (topK <= 0) {
            return List.of();
        }

        Set<String> prefixMatches = new HashSet<>();
        synchronized (root) {
            TrieNode node = traverse(normalizedPrefix);
            if (node != null) {
                prefixMatches.addAll(node.queries);
            }
        }

        List<Suggestion> ranked = rank(prefixMatches, topK);
        if (ranked.size() < topK) {
            ranked = withTypoCorrections(normalizedPrefix, ranked, topK);
        }

        return ranked;
    }

    public int getFrequency(String query) {
        String normalized = normalize(query);
        return queryFrequency.getOrDefault(normalized, new AtomicInteger(0)).get();
    }

    private List<Suggestion> rank(Set<String> candidates, int topK) {
        return candidates.stream()
                .map(query -> new Suggestion(query, getFrequency(query)))
                .sorted(Comparator.comparingInt(Suggestion::frequency).reversed()
                        .thenComparing(Suggestion::query))
                .limit(topK)
                .collect(Collectors.toList());
    }

    private List<Suggestion> withTypoCorrections(String input, List<Suggestion> existing, int topK) {
        Set<String> chosenQueries = existing.stream().map(Suggestion::query).collect(Collectors.toSet());
        List<Suggestion> suggestions = new ArrayList<>(existing);

        queryFrequency.forEach((query, frequency) -> {
            if (chosenQueries.contains(query)) {
                return;
            }

            int distance = levenshteinDistance(input, query);
            if (distance <= 2) {
                suggestions.add(new Suggestion(query, frequency.get()));
            }
        });

        suggestions.sort(Comparator.comparingInt(Suggestion::frequency).reversed().thenComparing(Suggestion::query));
        return suggestions.stream().limit(topK).collect(Collectors.toList());
    }

    private TrieNode traverse(String prefix) {
        TrieNode node = root;
        for (char ch : prefix.toCharArray()) {
            node = node.children.get(ch);
            if (node == null) {
                return null;
            }
        }
        return node;
    }

    private int levenshteinDistance(String a, String b) {
        if (a.equals(b)) {
            return 0;
        }

        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[a.length()][b.length()];
    }

    private String normalize(String query) {
        return query == null ? "" : query.trim().toLowerCase();
    }

    private static final class TrieNode {
        private final Map<Character, TrieNode> children = new HashMap<>();
        private final Set<String> queries = new HashSet<>();
    }

    public record Suggestion(String query, int frequency) { }
}
