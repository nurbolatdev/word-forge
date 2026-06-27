package com.wordforge.lists;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/presets")
class PresetController {

    private final PresetService service;

    PresetController(PresetService service) {
        this.service = service;
    }

    @GetMapping
    List<PresetService.PresetDto> listPresets() {
        return service.listPresets();
    }

    @PostMapping("/{presetId}/import")
    WordListDto importPreset(@PathVariable String presetId,
                             @RequestAttribute Long userId,
                             @RequestParam(defaultValue = "RU") String targetLang) {
        return service.importPreset(presetId, userId, targetLang);
    }
}
