package tn.esprit.peakwell.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.peakwell.entities.Notification;
import tn.esprit.peakwell.services.NotificationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin("*")
public class NotificationController {

  private final NotificationService notifService;

  // Get all active notifications for a patient
  @GetMapping
  public List<Notification> getAll(@RequestParam(required = false) Long profileId) {
    return notifService.getAll(profileId);
  }

  // Get unread count (for badge)
  @GetMapping("/unread-count")
  public Map<String, Long> getUnreadCount(@RequestParam(required = false) Long profileId) {
    return Map.of("count", notifService.getUnreadCount(profileId));
  }

  // Mark one as read
  @PatchMapping("/{id}/read")
  public Notification markAsRead(@PathVariable Long id) {
    return notifService.markAsRead(id);
  }

  // Mark all as read
  @PatchMapping("/read-all")
  public Map<String, String> markAllAsRead(@RequestParam(required = false) Long profileId) {
    notifService.markAllAsRead(profileId);
    return Map.of("status", "ok");
  }

  // Dismiss one
  @DeleteMapping("/{id}")
  public Map<String, String> dismiss(@PathVariable Long id) {
    notifService.dismiss(id);
    return Map.of("status", "dismissed");
  }

  // Dismiss all
  @DeleteMapping("/dismiss-all")
  public Map<String, String> dismissAll(@RequestParam(required = false) Long profileId) {
    notifService.dismissAll(profileId);
    return Map.of("status", "all dismissed");
  }

  // Manually trigger health check (also auto-runs every 6h)
  @PostMapping("/check")
  public List<Notification> triggerCheck(@RequestParam(required = false) Long profileId) {
    return notifService.checkAndNotify(profileId);
  }
}
