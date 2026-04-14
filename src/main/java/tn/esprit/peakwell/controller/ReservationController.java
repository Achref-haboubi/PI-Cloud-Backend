package tn.esprit.peakwell.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.peakwell.services.ReservationService;

@RestController
@RequestMapping("/reservations")
@CrossOrigin("*")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // toggle
    @PutMapping("/{menuId}")
    public void toggle(@PathVariable Long menuId) {
        reservationService.toggleReservation(menuId);
    }

    // count
    @GetMapping("/count/{menuId}")
    public int count(@PathVariable Long menuId) {
        return reservationService.getReservationCount(menuId);
    }

    // check user
    @GetMapping("/check/{menuId}")
    public boolean check(@PathVariable Long menuId) {
        return reservationService.isReserved(menuId);
    }
}
