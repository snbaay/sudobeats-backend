package org.example.sudobeats.service;


import org.example.sudobeats.entity.User;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class StreakService {
    public void markDailyCompleted(User user, LocalDate today) {
        LocalDate last = user.getLastPlayedDate();
        if (today.equals(last)) return;
        if (last != null && today.minusDays(1).equals(last)) {
            user.setCurrentStreak(user.getCurrentStreak() + 1);
        } else {
            user.setCurrentStreak(1);
        }
        user.setLastPlayedDate(today);

    }
}

