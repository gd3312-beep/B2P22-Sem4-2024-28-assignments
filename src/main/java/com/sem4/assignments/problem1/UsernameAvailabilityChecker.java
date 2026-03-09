package com.sem4.assignments.problem1;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UsernameAvailabilityChecker {
    private final ConcurrentMap<String, Long> usernameToUserId = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AtomicInteger> attemptFrequency = new ConcurrentHashMap<>();

    public boolean registerUsername(String username, long userId) {
        String normalized = normalize(username);
        return usernameToUserId.putIfAbsent(normalized, userId) == null;
    }

    public boolean checkAvailability(String username) {
        String normalized = normalize(username);
        attemptFrequency.computeIfAbsent(normalized, key -> new AtomicInteger()).incrementAndGet();
        return !usernameToUserId.containsKey(normalized);
    }

    public List<String> suggestAlternatives(String username, int count) {
        String normalized = normalize(username);
        List<String> suggestions = new ArrayList<>();

        if (count <= 0) {
            return suggestions;
        }

        addIfAvailable(normalized, suggestions, count);
        addIfAvailable(normalized.replace("_", "."), suggestions, count);

        int suffix = 1;
        while (suggestions.size() < count && suffix <= count * 20) {
            addIfAvailable(normalized + suffix, suggestions, count);
            addIfAvailable(normalized + "_" + suffix, suggestions, count);
            addIfAvailable(normalized + "." + suffix, suggestions, count);
            suffix++;
        }

        return suggestions;
    }

    public Optional<AttemptedUsername> getMostAttempted() {
        return attemptFrequency.entrySet()
                .stream()
                .max(Comparator.comparingInt(entry -> entry.getValue().get()))
                .map(entry -> new AttemptedUsername(entry.getKey(), entry.getValue().get()));
    }

    public int getAttemptCount(String username) {
        String normalized = normalize(username);
        return attemptFrequency.getOrDefault(normalized, new AtomicInteger(0)).get();
    }

    public Map<String, Long> getRegisteredUsersSnapshot() {
        return Map.copyOf(usernameToUserId);
    }

    private void addIfAvailable(String candidate, List<String> suggestions, int count) {
        if (candidate.isBlank()) {
            return;
        }
        if (!usernameToUserId.containsKey(candidate) && !suggestions.contains(candidate) && suggestions.size() < count) {
            suggestions.add(candidate);
        }
    }

    private String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }

    public record AttemptedUsername(String username, int attempts) { }
}
