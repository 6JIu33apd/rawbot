package ru.kusupovar.rawbot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationService {
    private final SubscriptionService subscriptionService;
    private final CryptoService cryptoService;
    private final MessageService messageService;
    private Double lastPrice = null;

    public NotificationService(SubscriptionService subscriptionService, CryptoService cryptoService, MessageService messageService) {
        this.subscriptionService = subscriptionService;
        this.cryptoService = cryptoService;
        this.messageService = messageService;
    }

    @Scheduled(fixedRate = 60000)
    public void fetchAndNotify() {
        try {
            double newPrice = cryptoService.getLatestPrice("BTC");
            if (lastPrice != null) {
                double change = ((newPrice / lastPrice) - 1) * 100;
                if (Math.abs(change) > 0.001) {
                    notifySubscribers(change, newPrice);
                }
            }
            lastPrice = newPrice;
        } catch (Exception e) {
            log.error("Ошибка отправки сообщения: {}", e.getMessage(), e);
        }
    }

    private void notifySubscribers(double change, double price) {
        for (Long chatId : subscriptionService.getSubscribers()) {
            System.out.println(chatId);
            String emoji = change > 0 ? "\uD83D\uDFE2" : "\uD83D\uDD34";
            messageService.sendMessage(chatId, "BTC change " + emoji + " " + String.format("%.5f", change) + "\n" + "Current price " + String.format("%.2f", price));
        }
    }
}