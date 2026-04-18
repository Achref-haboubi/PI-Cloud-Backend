package tn.esprit.peakwell.dto;

import java.util.List;

public class PredictionAllergeneResponse {

    private List<String> predictedAllergens;

    public PredictionAllergeneResponse() {
    }

    public List<String> getPredictedAllergens() {
        return predictedAllergens;
    }

    public void setPredictedAllergens(List<String> predictedAllergens) {
        this.predictedAllergens = predictedAllergens;
    }
}