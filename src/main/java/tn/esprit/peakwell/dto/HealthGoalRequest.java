package tn.esprit.peakwell.dto;

import lombok.Data;

@Data
public class HealthGoalRequest {
  private String metric;
  private String direction;
  private Double startValue;
  private Double targetValue;
  private String unit;
  private String deadline;        // "2026-06-16"
}
