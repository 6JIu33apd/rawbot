package ru.kusupovar.rawbot.service;

import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;

@Service
public class SubscriptionService {
    private final Set<Long> subscribers = new HashSet<>();

    public void subscribe(long chatId) {
        subscribers.add(chatId);
    }

    public void unsubscribe(long chatId) {
        subscribers.remove(chatId);
    }

    public Set<Long> getSubscribers() {
        return subscribers;
    }
}
