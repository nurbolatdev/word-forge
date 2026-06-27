package com.wordforge.enrichment;

import java.util.List;

record EnrichmentDto(
        Long wordId,
        String targetLang,
        String cefrLevel,
        String mnemonic,
        List<ExampleDto> examples
) {
    record ExampleDto(Long id, String text, String translation) {}

    static EnrichmentDto from(WordEnrichment enrichment, List<WordExample> examples) {
        return new EnrichmentDto(
                enrichment.getWordId(),
                enrichment.getTargetLang(),
                enrichment.getCefrLevel(),
                enrichment.getMnemonic(),
                examples.stream()
                        .map(e -> new ExampleDto(e.getId(), e.getText(), e.getTranslation()))
                        .toList()
        );
    }
}
