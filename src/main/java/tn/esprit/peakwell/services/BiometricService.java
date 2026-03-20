package tn.esprit.peakwell.services;


import tn.esprit.peakwell.dto.BiometricRequest;
import tn.esprit.peakwell.dto.BiometricResponse;
import tn.esprit.peakwell.dto.HealthAlertDto;
import tn.esprit.peakwell.entities.BiometricEntry;
import tn.esprit.peakwell.entities.MedicalProfile;
import tn.esprit.peakwell.repositories.BiometricEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.peakwell.repositories.MedicalProfileRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BiometricService {

    private final BiometricEntryRepository repository;
    private final MedicalProfileRepository profileRepository;

  public List<BiometricResponse> getAll() {
        return repository.findAllByOrderByRecordedAtAsc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public BiometricResponse addEntry(BiometricRequest request) {
        double bmi = Math.round((request.getWeight() / Math.pow(request.getHeight() / 100.0, 2)) * 10.0) / 10.0;
      MedicalProfile profile = profileRepository.findById(1L)
        .orElseThrow(() -> new RuntimeException("Medical profile not found. Please create your profile first."));

        BiometricEntry entry = BiometricEntry.builder()
                .weight(request.getWeight())
                .height(request.getHeight())
                .bmi(bmi)
                .bodyFat(request.getBodyFat())
                .muscleMass(request.getMuscleMass())
                .systolic(request.getSystolic())
                .diastolic(request.getDiastolic())
                .glucose(request.getGlucose())
                .notes(request.getNotes())
                .profile(profile)
                .build();

        return toResponse(repository.save(entry));
    }

    public BiometricResponse getLatest() {
        return repository.findTopByOrderByRecordedAtDesc()
                .map(this::toResponse)
                .orElse(null);
    }

    public void deleteEntry(Long id) {
        repository.deleteById(id);
    }

    public List<HealthAlertDto> getAlerts() {
        List<BiometricEntry> entries = repository.findAllByOrderByRecordedAtAsc();
        List<HealthAlertDto> alerts = new ArrayList<>();
        if (entries.isEmpty()) return alerts;

        BiometricEntry latest = entries.get(entries.size() - 1);
        BiometricEntry prev   = entries.size() >= 2 ? entries.get(entries.size() - 2) : null;

        if (prev != null) {
            double diff = Math.round((latest.getWeight() - prev.getWeight()) * 10.0) / 10.0;
            if (diff > 2)
                alerts.add(new HealthAlertDto("danger",  "Weight", "Rapid weight gain of +" + diff + "kg detected",   latest.getWeight() + " kg"));
            else if (diff < -3)
                alerts.add(new HealthAlertDto("warning", "Weight", "Rapid weight loss of "  + diff + "kg detected",   latest.getWeight() + " kg"));
        }

        if (latest.getSystolic() != null) {
            if (latest.getSystolic() > 140)
                alerts.add(new HealthAlertDto("danger",  "Blood Pressure", "Systolic BP above 140 mmHg — hypertension range",    latest.getSystolic() + "/" + latest.getDiastolic() + " mmHg"));
            else if (latest.getSystolic() > 130)
                alerts.add(new HealthAlertDto("warning", "Blood Pressure", "Elevated blood pressure — monitor closely",          latest.getSystolic() + "/" + latest.getDiastolic() + " mmHg"));
        }

        if (latest.getGlucose() != null) {
            if (latest.getGlucose() > 126)
                alerts.add(new HealthAlertDto("danger",  "Glucose", "Fasting glucose above 126 mg/dL — diabetic range", latest.getGlucose() + " mg/dL"));
            else if (latest.getGlucose() > 100)
                alerts.add(new HealthAlertDto("warning", "Glucose", "Pre-diabetic glucose level detected",              latest.getGlucose() + " mg/dL"));
        }

        if (alerts.isEmpty())
            alerts.add(new HealthAlertDto("info", "General", "All recorded values are within healthy ranges", "✓ Normal"));

        return alerts;
    }

    private BiometricResponse toResponse(BiometricEntry e) {
        BiometricResponse res = new BiometricResponse();
        res.setId(e.getId());
        res.setWeight(e.getWeight());
        res.setHeight(e.getHeight());
        res.setBmi(e.getBmi());
        res.setBodyFat(e.getBodyFat());
        res.setMuscleMass(e.getMuscleMass());
        res.setSystolic(e.getSystolic());
        res.setDiastolic(e.getDiastolic());
        res.setGlucose(e.getGlucose());
        res.setNotes(e.getNotes());
        res.setRecordedAt(e.getRecordedAt());
        return res;
    }
}
