package tn.esprit.peakwell.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;

import tn.esprit.peakwell.services.MenuService;
import tn.esprit.peakwell.repositories.DailyMenuRepository;

@Component
public class MenuScheduler {

    private final MenuService menuService;
    private final DailyMenuRepository menuRepository;

    public MenuScheduler(MenuService menuService, DailyMenuRepository menuRepository) {
        this.menuService = menuService;
        this.menuRepository = menuRepository;
    }

    // Génération automatique chaque jour à minuit
    @Scheduled(cron = "0 0 0 * * ?")
    public void generateDailyMenu() {

        LocalDate today = LocalDate.now();

        if (menuRepository.findByDate(today).isEmpty()) {
            menuService.generateMenu();
        }
    }

    @PostConstruct
    public void generateMenuAtStartup() {

        LocalDate today = LocalDate.now();

        if (menuRepository.findByDate(today).isEmpty()) {
            //menuService.generateMenu();
        }
    }
}