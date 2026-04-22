package tn.esprit.peakwell.services;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.net.InetAddress;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import tn.esprit.peakwell.entities.*;
import tn.esprit.peakwell.repositories.UserActivityRepository;

@Service
@RequiredArgsConstructor
public class UserActivityService implements IUserActivityService {

    private final UserActivityRepository userActivityRepository;

    @Override
    public void log(User user, ActivityType action, String description,
                    String status, HttpServletRequest request) {

        UserActivity activity = new UserActivity();
        activity.setUser(user);
        activity.setAction(action);
        activity.setDescription(description);
        activity.setStatus(status);

        // Use passed request, or fallback to current HTTP context
        HttpServletRequest req = request != null ? request : getCurrentRequest();

        if (req != null) {
            activity.setIpAddress(getClientIp(req));
            activity.setUserAgent(getUserAgent(req));
        } else {
            activity.setIpAddress("UNKNOWN");
            activity.setUserAgent("UNKNOWN");
        }

        userActivityRepository.save(activity);
    }

    // IP HANDLING
    private String getClientIp(HttpServletRequest request) {

        String ip = request.getHeader("X-Forwarded-For");

        // If behind proxy → take first IP
        if (ip != null && !ip.isBlank()) {
            ip = ip.split(",")[0].trim();
        } else {
            ip = request.getRemoteAddr();
        }

        // Normalize IPv6 localhost → IPv4
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            ip = "127.0.0.1";
        }

        return normalizeIp(ip);
    }

    // USER AGENT
    private String getUserAgent(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");

        if (ua == null || ua.isBlank()) {
            return "UNKNOWN";
        }

        return ua;
    }

    // CURRENT REQUEST 
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        return (attrs != null) ? attrs.getRequest() : null;
    }

    // NORMALIZE IP 
    private String normalizeIp(String ip) {
        if (ip == null || ip.isBlank()) {
            return "UNKNOWN";
        }

        try {
            InetAddress inet = InetAddress.getByName(ip);
            return inet.getHostAddress(); // returns normalized IPv4/IPv6
        } catch (Exception e) {
            return ip; // fallback if parsing fails
        }
    }
}