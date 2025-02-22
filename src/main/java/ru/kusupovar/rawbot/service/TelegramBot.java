package ru.kusupovar.rawbot.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.kusupovar.rawbot.config.BotConfig;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final SubscriptionService subscriptionService;
    private final CryptoService cryptoService;
    private final BotConfig botConfig;
    private Double lastPrice = null;

    public TelegramBot(BotConfig botConfig, SubscriptionService subscriptionService, CryptoService cryptoService) {
        this.botConfig = botConfig;
        this.subscriptionService = subscriptionService;
        this.cryptoService = cryptoService;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText.toLowerCase()) {
                case "/subscribe" -> {
                    subscriptionService.subscribe(chatId);
                    sendMessage(chatId, "Вы подписались на рассылку!");
                }
                case "/unsubscribe" -> {
                    subscriptionService.unsubscribe(chatId);
                    sendMessage(chatId, "Вы отписались от рассылки.");
                }
                case "/price" -> sendMessage(chatId, "Текущий курс BTC: " + cryptoService.getLatestPrice());
                default -> sendMessage(chatId, "Неизвестная команда. Используйте /subscribe, /unsubscribe или /price.");
            }
        }
    }

    @Scheduled(fixedRate = 60000)
    private void fetchAndNotify() {
        try {
            double newPrice = cryptoService.getLatestPrice();
            System.out.println("1 " + newPrice);
            if (lastPrice != null) {
                double change = ((newPrice / lastPrice) - 1) * 100;
                System.out.println("2 " + change);
                if (Math.abs(change) > 0.001) {
                    notifySubscribers(change, newPrice);
                }
            }
            lastPrice = newPrice;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notifySubscribers(double change, double price) {
        for (Long chatId : subscriptionService.getSubscribers()) {
            String emoji = change > 0 ? "\uD83D\uDFE2" : "\uD83D\uDD34";
            sendMessage(chatId, "BTC change " + emoji + " " + String.format("%.2f", change) + "\n" + "Current price " + price);
        }
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}