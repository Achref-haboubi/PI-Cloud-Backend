package tn.esprit.peakwell.controller;

import tn.esprit.peakwell.entities.DailyMenu;
import tn.esprit.peakwell.dto.DailyMenuDTO;
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

    // ✅ CREATE MANUAL
    @PostMapping
    public DailyMenu createMenu(@RequestBody DailyMenu menu) {
        return menuService.createMenu(menu);
    }

    // 🔥 NEW → AUTO GENERATE MENU
    @PostMapping("/generate")
    public DailyMenu generateMenu() {
        return menuService.generateMenu();
    }

    // ✅ GET TODAY → DTO
    @GetMapping("/today")
    public DailyMenuDTO getTodayMenu() {
        return menuService.getTodayMenu();
    }

    // ✅ GET ALL → DTO
    @GetMapping
    public List<DailyMenuDTO> getAllMenus() {
        return menuService.getAllMenus();
    }

    // ✅ GET WEEK → DTO
    @GetMapping("/week")
    public List<DailyMenuDTO> getWeeklyMenus() {
        return menuService.getWeeklyMenus();
    }

    // ✅ GET BY DATE → DTO
    @GetMapping("/{date}")
    public DailyMenuDTO getMenuByDate(@PathVariable String date) {
        return menuService.getMenuByDate(LocalDate.parse(date));
    }
}