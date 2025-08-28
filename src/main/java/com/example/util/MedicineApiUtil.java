package com.example.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;

public class MedicineApiUtil {
    private static final String RXNORM_API = "https://rxnav.nlm.nih.gov/REST/Prescribe/Suggest.json?name=";
    private static final String OPEN_FDA_API = "https://api.fda.gov/drug/label.json?limit=10&search=openfda.brand_name:";

    private static final int CONNECT_TIMEOUT_MS = 2000;
    private static final int READ_TIMEOUT_MS = 2000;
    private static final int MAX_CACHE_SIZE = 200;
    private static final long CACHE_TTL_MS = 10 * 60 * 1000;
    private static final int MAX_RESULTS = 10;

    private static List<String> LOCAL_DATASET = loadLocalDataset();

    private static final Map<String, CacheEntry> cache = new HashMap<>();
    private static final Deque<String> lruOrder = new ArrayDeque<>();

    public static List<String> searchMedicineNames(String query) {
        if (query == null) return new ArrayList<>();
        String key = query.trim().toLowerCase();
        if (key.length() < 2) return new ArrayList<>();

        CacheEntry entry = cache.get(key);
        long now = System.currentTimeMillis();
        if (entry != null && (now - entry.createdAtMs) < CACHE_TTL_MS) {
            lruOrder.remove(key);
            lruOrder.addFirst(key);
            return entry.values;
        }

        // Local: prefix-first then contains match
        List<String> merged = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        List<String> prefixMatches = LOCAL_DATASET.stream()
                .filter(n -> n.toLowerCase().startsWith(key))
                .limit(MAX_RESULTS)
                .collect(Collectors.toList());
        for (String n : prefixMatches) { addUnique(merged, seen, n); }
        if (merged.size() < MAX_RESULTS) {
            for (String n : LOCAL_DATASET) {
                if (merged.size() >= MAX_RESULTS) break;
                String lower = n.toLowerCase();
                if (!lower.startsWith(key) && lower.contains(key)) addUnique(merged, seen, n);
            }
        }

        // If still room, try remote providers
        if (merged.size() < MAX_RESULTS) merged = fetchFromRxNorm(key, merged, seen);
        if (merged.size() < MAX_RESULTS) merged = fetchFromOpenFda(key, merged, seen);

        putInCache(key, merged);
        return merged;
    }

    private static List<String> loadLocalDataset() {
        List<String> list = new ArrayList<>();
        try {
            InputStream in = MedicineApiUtil.class.getClassLoader().getResourceAsStream("medicines.txt");
            if (in != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty() && !line.startsWith("#")) list.add(line);
                    }
                }
            }
        } catch (Exception ignored) {}
        return list;
    }

    private static List<String> fetchFromRxNorm(String key, List<String> acc, Set<String> seen) {
        try {
            String encoded = URLEncoder.encode(key, StandardCharsets.UTF_8.name());
            String urlStr = RXNORM_API + encoded;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) content.append(line);
                JSONObject json = new JSONObject(content.toString());
                JSONObject group = json.optJSONObject("suggestionGroup");
                if (group != null) {
                    JSONObject list = group.optJSONObject("suggestionList");
                    if (list != null) {
                        JSONArray arr = list.optJSONArray("suggestion");
                        if (arr != null) {
                            for (int i = 0; i < arr.length(); i++) {
                                if (acc.size() >= MAX_RESULTS) break;
                                String name = arr.optString(i, "");
                                if (!name.isEmpty()) addUnique(acc, seen, name);
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return acc;
    }

    private static List<String> fetchFromOpenFda(String key, List<String> acc, Set<String> seen) {
        try {
            String wildcard = key + "*";
            String encoded = URLEncoder.encode(wildcard, StandardCharsets.UTF_8.name());
            String urlStr = OPEN_FDA_API + encoded;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) content.append(line);
                JSONObject json = new JSONObject(content.toString());
                JSONArray results = json.optJSONArray("results");
                if (results != null) {
                    for (int i = 0; i < results.length(); i++) {
                        if (acc.size() >= MAX_RESULTS) break;
                        JSONObject med = results.optJSONObject(i);
                        if (med == null) continue;
                        JSONObject openfda = med.optJSONObject("openfda");
                        if (openfda == null) continue;
                        JSONArray brandNames = openfda.optJSONArray("brand_name");
                        if (brandNames != null) {
                            for (int j = 0; j < brandNames.length(); j++) {
                                if (acc.size() >= MAX_RESULTS) break;
                                String name = brandNames.optString(j, "");
                                if (!name.isEmpty()) addUnique(acc, seen, name);
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return acc;
    }

    private static void addUnique(List<String> acc, Set<String> seen, String name) {
        String lower = name.toLowerCase();
        if (!seen.contains(lower)) {
            acc.add(name);
            seen.add(lower);
        }
    }

    private static void putInCache(String key, List<String> values) {
        if (cache.containsKey(key)) lruOrder.remove(key);
        cache.put(key, new CacheEntry(values));
        lruOrder.addFirst(key);
        while (lruOrder.size() > MAX_CACHE_SIZE) {
            String least = lruOrder.removeLast();
            cache.remove(least);
        }
    }

    private static class CacheEntry {
        final List<String> values;
        final long createdAtMs;
        CacheEntry(List<String> values) {
            this.values = values;
            this.createdAtMs = System.currentTimeMillis();
        }
    }
}
