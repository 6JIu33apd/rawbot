package ru.kusupovar.rawbot.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.kusupovar.rawbot.config.BotConfig;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig botConfig;
    File kozel = new File("src/img/kozel.jpg");

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

            log.info("Message from chatId " + update.getMessage().getChatId() + ":\n" + "\t" + update.getMessage().getText());
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start" -> startCommandReceived(chatId, update.getMessage().getChat().getFirstName(), kozel);
                case "/fortune" -> sendMessage(chatId, "Удача повышена на 3%");
                case "/hangout" -> sendMessage(chatId, "Уже в пути");
                case "/smash" -> sendMessage(chatId, "Каждый в мире получил по ебалу");
                case "/weather" -> {
                    try {
                        sendMessage(chatId, weatherInfo());
                    } catch (IOException e) {
                        log.error("Error occurred: " + e.getMessage());
                    }
                }
                default -> sendMessage(chatId, "Не знаю такой команды");
            }
        }

    }

    private void startCommandReceived(long chatId, String name, File file) {

        String answer = "Привет, " + name + "!";
        sendMessage(chatId, answer);
        sendPhoto(chatId, file);

    }

    private void sendMessage(long chatId, String text) {

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.getReplyMarkup();

        try {

            execute(message);
            log.info("Response for chatId " + chatId + ":\n" + "\t" + message.getText());
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void sendPhoto(long chatId, File file) {

        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(new InputFile(file));

        try {

            execute(photo);
            log.info("Response for chatId " + chatId + ":\n" + "\t" + photo.getFile().getAttachName());
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private String weatherInfo() throws IOException {

        String webPage = "https://yandex.ru/pogoda/saratov";
        Document doc = Jsoup.parse(Jsoup.connect(webPage).get().html());
        String temperature = doc.getElementsByClass("temp__value temp__value_with-unit").first().text();
        String time = doc.getElementsByClass("time fact__time").first().text();
        String fact = doc.getElementsByClass("link__feelings fact__feelings").first().text();
        String title = doc.getElementsByClass("title title_level_1 header-title__title").first().text();


        return title + "\n" + time + "\nТемпература " + temperature + "\n" + fact;

    }
//span[@class="wind-speed"]
}
