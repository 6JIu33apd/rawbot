package ru.kusupovar.rawbot.service;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.kusupovar.rawbot.config.BotConfig;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final Set<Long> subscribers = new HashSet<>();
    private final LinkedList<Double> rates = new LinkedList<>();
    private final OkHttpClient httpClient = new OkHttpClient();
    private String latestValue;
    final BotConfig botConfig;

    public TelegramBot(BotConfig botConfig) {
        this.botConfig = botConfig;
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

            if (messageText.equalsIgnoreCase("/subscribe")) {
                subscribers.add(chatId);
                sendMessage(chatId, "Вы подписались на рассылку!");
            } else if (messageText.equalsIgnoreCase("/unsubscribe")) {
                subscribers.remove(chatId);
                sendMessage(chatId, "Вы отписались от рассылки.");
            } else {
                sendMessage(chatId, "Неизвестная команда. Используйте /subscribe или /unsubscribe.");
            }
        }
    }

    @Scheduled(fixedRate = 60000)
    private void fetchApiData() throws IOException {

        String apiUrl = "https://coinmarketcap.com/currencies/bitcoin/";
        Request request = new Request.Builder().url(apiUrl).build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                Document document = Jsoup.parse(response.body().string());
                Element targetElement = document.selectXpath("//*[@id=\"section-coin-overview\"]/div[2]/span").first();
                if (targetElement != null) {
                    saveRates(targetElement.text());
                    latestValue = targetElement.text();
                    System.out.println("Получено новое значение: " + latestValue);
                } else {
                    System.out.println("Не удалось найти нужный элемент в HTML.");
                }
            }
        }
    }

    private void saveRates(String rate) {

        final int MAX_SIZE = 10;

        if (rates.size() >= MAX_SIZE) {
            rates.remove(0);
        }
        try {
            rates.add(Double.parseDouble(rate.replace("$", "").replace(",", "")));
            System.out.println(rates);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } finally {
            prepareData();
        }
    }

    private void prepareData() {

        double change;
        double threshold = 0.01;

        if (rates.size() >= 2) {
            try {
                if (rates.getLast() >= rates.get(rates.size() - 2)) {
                    change = (rates.getLast() / rates.get(rates.size() - 2) - 1) * 100;
                    System.out.println("RISE " + String.format("%.5f", change));
                } else {
                    change = (rates.getLast() / rates.get(rates.size() - 2) - 1) * 100;
                    System.out.println("FALL " + String.format("%.5f", change));
                }
                if (Math.abs(change) > threshold) {
                    notifySubscribers(change);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void notifySubscribers(double change) {
        for (Long chatId : subscribers) {
            System.out.println("Отправил сообщение в чат " + chatId);
            try {
                if (change > 0) {
                    sendMessage(chatId, "BTC change " + "\uD83D\uDFE2" + " " + String.format("%.5f", change) + "\n" + "Current price " + latestValue);
                } else {
                    sendMessage(chatId, "BTC change " + "\uD83D\uDD34" + " " + String.format("%.5f", change) + "\n" + "Current price " + latestValue);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startCommandReceived(long chatId, String name, File file) {
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
