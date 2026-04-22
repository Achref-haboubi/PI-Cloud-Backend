package tn.esprit.peakwell.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tn.esprit.peakwell.entities.UserActivity;
 
import java.time.LocalDateTime;
import java.util.List;

public interface UserActivityRepository extends JpaRepository<UserActivity, Long>{
 

     // ── 1. Total count by status ──────────────────────────────────────────────
    long countByStatus(String status);
 
    // ── 2. Count by action type ───────────────────────────────────────────────
    @Query("SELECT a.action, COUNT(a) FROM UserActivity a GROUP BY a.action ORDER BY COUNT(a) DESC")
    List<Object[]> countByActionType();
 
    // ── 3. Recent activity feed (with user info) ──────────────────────────────
    @Query("SELECT a FROM UserActivity a JOIN FETCH a.user ORDER BY a.createdAt DESC")
    List<UserActivity> findRecentActivity(Pageable pageable);
 
    // ── 4. Failed attempts per day (last 30 days) ─────────────────────────────
    @Query("""
        SELECT CAST(a.createdAt AS date), COUNT(a)
        FROM UserActivity a
        WHERE a.status = 'FAILED'
        AND a.createdAt >= :since
        GROUP BY CAST(a.createdAt AS date)
        ORDER BY CAST(a.createdAt AS date)
    """)
    List<Object[]> failedAttemptsPerDay(LocalDateTime since);
 
    // ── 5. Top IPs ────────────────────────────────────────────────────────────
    @Query("SELECT a.ipAddress, COUNT(a) FROM UserActivity a WHERE a.ipAddress IS NOT NULL GROUP BY a.ipAddress ORDER BY COUNT(a) DESC")
    List<Object[]> topIpAddresses(Pageable pageable);
 
    // ── 6. User agent breakdown ───────────────────────────────────────────────
    @Query("SELECT a.userAgent, COUNT(a) FROM UserActivity a WHERE a.userAgent IS NOT NULL GROUP BY a.userAgent ORDER BY COUNT(a) DESC")
    List<Object[]> topUserAgents(Pageable pageable);
 
    // ── 7. Activity by hour of day (heatmap) ──────────────────────────────────
    @Query("SELECT FUNCTION('HOUR', a.createdAt), COUNT(a) FROM UserActivity a GROUP BY FUNCTION('HOUR', a.createdAt) ORDER BY FUNCTION('HOUR', a.createdAt)")
    List<Object[]> activityByHour();
 
    // ── 8. Activity per day (last 30 days, all actions) ───────────────────────
    @Query("""
        SELECT CAST(a.createdAt AS date), COUNT(a)
        FROM UserActivity a
        WHERE a.createdAt >= :since
        GROUP BY CAST(a.createdAt AS date)
        ORDER BY CAST(a.createdAt AS date)
    """)
    List<Object[]> activityPerDay(LocalDateTime since);
 
    // ── 9. Per-user activity log ──────────────────────────────────────────────
    @Query("SELECT a FROM UserActivity a WHERE a.user.id = :userId ORDER BY a.createdAt DESC")
    List<UserActivity> findByUserId(Long userId, Pageable pageable);
 
    // ── 10. Success vs Failed per action type ─────────────────────────────────
    @Query("SELECT a.action, a.status, COUNT(a) FROM UserActivity a GROUP BY a.action, a.status ORDER BY a.action")
    List<Object[]> actionStatusBreakdown();
 
    // ── 11. Most active users ─────────────────────────────────────────────────
    @Query("""
        SELECT u.email, u.firstName, u.lastName, COUNT(a)
        FROM UserActivity a
        JOIN a.user u
        GROUP BY u.id, u.email, u.firstName, u.lastName
        ORDER BY COUNT(a) DESC
    """)
    List<Object[]> mostActiveUsers(Pageable pageable);

}
