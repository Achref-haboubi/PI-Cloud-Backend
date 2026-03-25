package tn.esprit.peakwell.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tn.esprit.peakwell.entities.Dietitian;
import tn.esprit.peakwell.services.IDietitianService;

import java.util.Map;

@Controller
@RequestMapping("/dietitian")
public class DietitianController {
    @Autowired
    IDietitianService dietitianService;

    @PutMapping("admin/{id}/status")
    public ResponseEntity<Map<String, Object>> updateDietitianStatus(@PathVariable Long id, @RequestParam boolean active) {

        Dietitian dietitian = dietitianService.setDietitianActiveStatus(id, active);

        Map<String, Object> response = Map.of(
                "status", HttpStatus.OK.value(),
                "message", active
                        ? "Dietitian activated successfully"
                        : "Dietitian deactivated successfully",
                "data", dietitian
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
