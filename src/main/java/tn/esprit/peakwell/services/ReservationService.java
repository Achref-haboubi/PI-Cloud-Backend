package tn.esprit.peakwell.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import tn.esprit.peakwell.entities.DailyMenu;
import tn.esprit.peakwell.entities.Reservation;
import tn.esprit.peakwell.repositories.DailyMenuRepository;
import tn.esprit.peakwell.repositories.ReservationRepository;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final DailyMenuRepository menuRepository;

    // toggle reservation
    public void toggleReservation(Long menuId) {

        String userId = getCurrentUserId();

        DailyMenu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new RuntimeException("Menu not found"));

        Optional<Reservation> existing =
                reservationRepository.findByUserIdAndDailyMenuId(userId, menuId);

        if (existing.isPresent()) {
            reservationRepository.delete(existing.get());
        } else {
            Reservation r = new Reservation();
            r.setUserId(userId);
            r.setDailyMenu(menu);
            reservationRepository.save(r);
        }
    }

    // count reservations
    public int getReservationCount(Long menuId) {
        return reservationRepository.countByDailyMenuId(menuId);
    }

    // check if user reserved
    public boolean isReserved(Long menuId) {
        String userId = getCurrentUserId();

        return reservationRepository
                .findByUserIdAndDailyMenuId(userId, menuId)
                .isPresent();
    }

    private String getCurrentUserId() {
        JwtAuthenticationToken token =
            (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        return token.getToken().getSubject();
    }
}