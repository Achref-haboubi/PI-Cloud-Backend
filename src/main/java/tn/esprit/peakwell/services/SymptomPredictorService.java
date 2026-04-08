package tn.esprit.peakwell.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
@Slf4j
public class SymptomPredictorService {

  private JsonNode modelData;
  private List<String> featureColumns;
  private boolean modelLoaded = false;

  private static final Map<Integer, String> SEVERITY_LABELS = Map.of(
    1, "Mild", 2, "Light", 3, "Moderate", 4, "Severe", 5, "Very Severe");
  private static final Map<String, Integer> TIME_MAP = Map.of(
    "morning", 0, "afternoon", 1, "evening", 2, "night", 3);
  private static final List<String> SYMPTOM_TYPES = List.of(
    "Headache", "Fatigue", "Nausea", "Dizziness", "Insomnia",
    "Bloating", "Joint Pain", "Muscle Pain", "Anxiety", "Brain Fog",
    "Chest Tightness", "Shortness of Breath", "Back Pain", "Stomach Pain",
    "Heartburn", "Cramps", "Numbness", "Skin Rash");
  private static final List<String> TRIGGER_NAMES = List.of(
    "caffeine", "poor_sleep", "stress", "skipped_meal", "exercise",
    "dehydration", "screen_time", "alcohol", "weather", "medication");

  @PostConstruct
  public void loadModel() {
    try {
      ClassPathResource resource = new ClassPathResource("ml/symptom_model.json");
      InputStream is = resource.getInputStream();
      modelData = new ObjectMapper().readTree(is);
      featureColumns = new ArrayList<>();
      modelData.get("features").forEach(f -> featureColumns.add(f.asText()));
      modelLoaded = true;
      log.info("ML Model loaded: {} trees, {} features", modelData.get("n").asInt(), featureColumns.size());
    } catch (Exception e) {
      log.warn("ML model not found: {}", e.getMessage());
      modelLoaded = false;
    }
  }

  public boolean isModelLoaded() { return modelLoaded; }

  // ── Multi-symptom prediction ────────────────────

  public Map<String, Object> predictMultiple(int stress, int mood, int energy, double sleep, int water,
                                             String timeOfDay, List<String> symptoms, List<String> triggers,
                                             int age, double bmi, boolean chronic, double exerciseHrs, int caffeineCups) {
    Map<String, Object> result = new LinkedHashMap<>();
    if (!modelLoaded) { result.put("error", "Model not loaded"); return result; }
    if (symptoms == null || symptoms.isEmpty()) { result.put("error", "No symptoms provided"); return result; }

    // Predict each symptom individually
    List<Map<String, Object>> perSymptom = new ArrayList<>();
    int totalSeverity = 0;
    double totalConfidence = 0;
    int maxSeverity = 0;
    String maxSeveritySymptom = "";

    for (String symptom : symptoms) {
      Map<String, Object> single = predictSingle(stress, mood, energy, sleep, water,
        timeOfDay, symptom, triggers, age, bmi, chronic, exerciseHrs, caffeineCups);
      perSymptom.add(single);

      int sev = (int) single.get("predictedSeverity");
      double conf = (double) single.get("confidence");
      totalSeverity += sev;
      totalConfidence += conf;

      if (sev > maxSeverity) {
        maxSeverity = sev;
        maxSeveritySymptom = symptom;
      }
    }

    int count = symptoms.size();
    double avgSeverity = Math.round((double) totalSeverity / count * 10.0) / 10.0;
    double avgConfidence = Math.round(totalConfidence / count * 10.0) / 10.0;

    // Overall severity: weighted toward the worst symptom
    int overallSeverity = (int) Math.round(avgSeverity * 0.4 + maxSeverity * 0.6);
    overallSeverity = Math.max(1, Math.min(5, overallSeverity));

    // Multi-symptom bonus: having many symptoms at once increases overall severity
    if (count >= 3 && overallSeverity < 5) overallSeverity = Math.min(5, overallSeverity + 1);
    else if (count >= 5 && overallSeverity < 5) overallSeverity = Math.min(5, overallSeverity + 2);

    result.put("overallSeverity", overallSeverity);
    result.put("overallSeverityLabel", SEVERITY_LABELS.get(overallSeverity));
    result.put("averageSeverity", avgSeverity);
    result.put("averageConfidence", avgConfidence);
    result.put("symptomCount", count);
    result.put("worstSymptom", maxSeveritySymptom);
    result.put("worstSeverity", maxSeverity);
    result.put("worstSeverityLabel", SEVERITY_LABELS.get(maxSeverity));
    result.put("predictions", perSymptom);
    result.put("modelType", "Random Forest (150 trees, 12K samples, 29 features)");
    result.put("riskFactors", analyzeRisks(stress, mood, energy, sleep, water, triggers,
      age, bmi, chronic, exerciseHrs, caffeineCups, count));
    result.put("multiSymptomWarning", generateMultiSymptomWarning(symptoms, perSymptom));

    return result;
  }

  // ── Single symptom prediction ───────────────────

  private Map<String, Object> predictSingle(int stress, int mood, int energy, double sleep, int water,
                                            String timeOfDay, String symptom, List<String> triggers,
                                            int age, double bmi, boolean chronic, double exerciseHrs, int caffeineCups) {
    double[] features = buildFeatures(stress, mood, energy, sleep, water, timeOfDay, symptom,
      triggers, age, bmi, chronic, exerciseHrs, caffeineCups);

    int nTrees = modelData.get("n").asInt();
    JsonNode trees = modelData.get("trees");
    int[] votes = new int[6];
    for (int i = 0; i < nTrees; i++) {
      int p = traverse(trees.get(i), features);
      if (p >= 1 && p <= 5) votes[p]++;
    }

    int best = 1; int maxV = 0;
    for (int c = 1; c <= 5; c++) if (votes[c] > maxV) { maxV = votes[c]; best = c; }

    Map<String, Double> probs = new LinkedHashMap<>();
    for (int c = 1; c <= 5; c++)
      probs.put(SEVERITY_LABELS.get(c), Math.round((double) votes[c] / nTrees * 1000.0) / 10.0);

    double conf = Math.round((double) maxV / nTrees * 1000.0) / 10.0;

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("symptom", symptom);
    result.put("predictedSeverity", best);
    result.put("severityLabel", SEVERITY_LABELS.get(best));
    result.put("confidence", conf);
    result.put("probabilities", probs);
    return result;
  }

  // ── Feature building (29 features) ──────────────

  private double[] buildFeatures(int stress, int mood, int energy, double sleep, int water,
                                 String time, String symptom, List<String> triggers,
                                 int age, double bmi, boolean chronic, double exHrs, int caffeine) {
    double[] f = new double[29];
    f[0] = stress; f[1] = mood; f[2] = energy; f[3] = sleep; f[4] = water;
    f[5] = TIME_MAP.getOrDefault(time != null ? time.toLowerCase() : "", 0);
    f[6] = symptom != null ? Math.max(0, SYMPTOM_TYPES.indexOf(symptom)) : 0;

    Set<String> ts = new HashSet<>();
    if (triggers != null) triggers.forEach(t -> ts.add(t.toLowerCase().replace(" ", "_")));
    int tc = 0;
    for (int i = 0; i < TRIGGER_NAMES.size(); i++) {
      f[7 + i] = ts.contains(TRIGGER_NAMES.get(i)) ? 1 : 0;
      if (f[7 + i] == 1) tc++;
    }
    f[17] = tc; f[18] = age; f[19] = bmi; f[20] = chronic ? 1 : 0; f[21] = exHrs; f[22] = caffeine;

    // 6 engineered features
    f[23] = stress * (6 - mood);
    f[24] = Math.round(sleep / (energy + 0.5) * 100.0) / 100.0;
    f[25] = Math.round(tc * stress * 0.1 * 100.0) / 100.0;
    f[26] = Math.round((mood + energy + (sleep / 2)) / 3 * 100.0) / 100.0;
    f[27] = water < 1500 ? Math.round((1500 - water) / 1000.0 * 1000.0) / 1000.0 : 0;
    f[28] = Math.round(((bmi / 25) + (6 - exHrs) / 5 + (chronic ? 0.5 : 0)) * 100.0) / 100.0;

    return f;
  }

  private int traverse(JsonNode node, double[] features) {
    if (node.has("l") && node.get("l").isBoolean() && node.get("l").asBoolean())
      return node.get("c").asInt();
    String fn = node.get("f").asText();
    double th = node.get("t").asDouble();
    int idx = featureColumns.indexOf(fn);
    if (idx < 0 || idx >= features.length) return 3;
    return features[idx] <= th ? traverse(node.get("L"), features) : traverse(node.get("R"), features);
  }

  // ── Multi-symptom warning ───────────────────────

  private String generateMultiSymptomWarning(List<String> symptoms, List<Map<String, Object>> predictions) {
    long severeCount = predictions.stream()
      .filter(p -> (int) p.get("predictedSeverity") >= 4)
      .count();
    int count = symptoms.size();

    if (severeCount >= 2) {
      return "Multiple severe symptoms detected simultaneously. This combination may indicate a systemic issue. Seek medical consultation promptly.";
    } else if (count >= 4) {
      return "You are experiencing " + count + " symptoms at once. Multiple concurrent symptoms may indicate an underlying condition. Consider a medical check-up.";
    } else if (count >= 2 && severeCount >= 1) {
      return "One or more of your symptoms is predicted as severe. Monitor closely and consult your healthcare provider if symptoms persist.";
    } else if (count >= 2) {
      return "Multiple symptoms present. Track their progression — if severity increases, consult your healthcare provider.";
    }
    return null;
  }

  // ── Risk analysis ───────────────────────────────

  private List<String> analyzeRisks(int stress, int mood, int energy, double sleep, int water,
                                    List<String> triggers, int age, double bmi, boolean chronic,
                                    double exHrs, int caff, int symptomCount) {
    List<String> f = new ArrayList<>();
    if (symptomCount >= 3) f.add(symptomCount + " concurrent symptoms — elevated overall risk");
    if (stress >= 4) f.add("High stress (" + stress + "/5) — top severity predictor");
    if (mood <= 2) f.add("Low mood (" + mood + "/5) — strongly linked to severity");
    if (energy <= 2) f.add("Low energy (" + energy + "/5) — amplifies symptoms");
    if (sleep < 5.5) f.add("Sleep deficit (" + sleep + "h) — major feature importance");
    if (water < 1200) f.add("Dehydration risk (" + water + "ml)");
    if (age > 55) f.add("Age (" + age + ") — higher baseline risk");
    if (bmi > 30) f.add("BMI " + bmi + " — obesity increases severity");
    if (chronic) f.add("Chronic condition — raises baseline");
    if (exHrs < 1) f.add("Sedentary (" + exHrs + "h/week)");
    if (caff > 3) f.add("High caffeine (" + caff + " cups)");
    double wellness = (mood + energy + (sleep / 2)) / 3;
    if (wellness < 2) f.add("Very low wellness index (" + Math.round(wellness * 100.0) / 100.0 + ")");
    if (triggers != null && triggers.size() >= 3) f.add(triggers.size() + " triggers — cumulative effect");
    if (f.isEmpty()) f.add("No major risk factors detected");
    return f;
  }

  public List<String> getSymptomTypes() { return SYMPTOM_TYPES; }
  public List<String> getTriggerNames() { return TRIGGER_NAMES; }
  public Map<Integer, String> getSeverityLabels() { return SEVERITY_LABELS; }
}
