package tn.esprit.peakwell.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.peakwell.services.HealthHeatmapService;

import java.util.Map;

@RestController
@RequestMapping("/api/heatmap")
@RequiredArgsConstructor
@CrossOrigin("*")
public class HealthHeatmapController {

  private final HealthHeatmapService heatmapService;

  @GetMapping
  public Map<String, Object> getHeatmapData() {
    return heatmapService.getHeatmapData();
  }
}
