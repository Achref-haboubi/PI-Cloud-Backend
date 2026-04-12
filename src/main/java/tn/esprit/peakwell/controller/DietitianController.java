package tn.esprit.peakwell.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import tn.esprit.peakwell.services.IDietitianService;


@Controller
@RequestMapping("/dietitian")
public class DietitianController {
    @Autowired
    IDietitianService dietitianService;

    
}
