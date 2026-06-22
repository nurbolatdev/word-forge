package com.wordforge.enrichment;

import java.util.List;

public record EnrichmentResult(String status, List<String> examples) {
}
