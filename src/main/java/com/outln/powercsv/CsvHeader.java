package com.outln.powercsv;

import java.util.HashMap;
import java.util.Map;

public class CsvHeader {
    private Map<String, Integer> indices = new HashMap<>();
    private String[] headers;

    CsvHeader(String[] headers) {
        this.headers = headers;
        for (int i = 0; i < headers.length; i++) {
            indices.put(headers[i], i);
        }
    }

    public Map<String, Integer> getIndices() {
        return indices;
    }

    public String[] getHeaders() {
        return headers;
    }

    public String getHeader(int index) {
        if (index >= headers.length) {
            return null;
        }
        return headers[index];
    }

    public int getIndex(String header) {
        return indices.getOrDefault(header, -1);
    }
}
