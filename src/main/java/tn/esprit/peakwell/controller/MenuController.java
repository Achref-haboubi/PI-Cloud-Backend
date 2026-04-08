package tn.esprit.peakwell.controller;

import tn.esprit.peakwell.entities.DailyMenu;
import tn.esprit.peakwell.dto.DailyMenuDTO;
import tn.esprit.peakwell.dto.DailyMenuRequest;
import tn.esprit.peakwell.services.MenuService;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/menu")
@CrossOrigin("*")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    // CREATE MANUAL
    @PostMapping
    public DailyMenuDTO createMenu(@RequestBody DailyMenuRequest request) {
        return menuService.createMenu(request);
    }

    // AUTO GENERATE MENU
    @PostMapping("/generate")
    public DailyMenuDTO generateMenu() {
        return menuService.generateMenu();
    }

    // GET TODAY
    @GetMapping("/today")
    public DailyMenuDTO getTodayMenu() {
        return menuService.getTodayMenu();
    }

    // GET ALL
    @GetMapping
    public List<DailyMenuDTO> getAllMenus() {
        return menuService.getAllMenus();
    }

    // GET WEEK
    @GetMapping("/week")
    public List<DailyMenuDTO> getWeeklyMenus() {
        return menuService.getWeeklyMenus();
    }

    // GET BY DATE
    @GetMapping("/{date}")
    public DailyMenuDTO getMenuByDate(@PathVariable String date) {
        return menuService.getMenuByDate(LocalDate.parse(date));
    }

    @DeleteMapping("/{id}")
    public void deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
    }
}