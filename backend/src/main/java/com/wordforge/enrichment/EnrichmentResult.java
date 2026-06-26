package com.wordforge.enrichment;

import java.util.List;

public record EnrichmentResult(String cefrLevel, List<ExampleData> examples) {
    public record ExampleData(String text, String translation) {}
}
