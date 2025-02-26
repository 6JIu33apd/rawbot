package ru.kusupovar.rawbot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class MessageService {

    private final TelegramService telegramService;

    public MessageService(@Lazy TelegramService telegramService) {
        this.telegramService = telegramService;
    }

    public void sendMessage(long chatId, String text) {
        SendMessage message = SendMessage.builder().chatId(chatId).text(text).build();

        try {
            telegramService.execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки: {}", e.getMessage(), e);
        }
    }

    public void sendMarkup(long chatId, String text, InlineKeyboardMarkup markup) {
        SendMessage message = SendMessage.builder().chatId(chatId).text(text).replyMarkup(markup).build();

        try {
            telegramService.execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки: {}", e.getMessage(), e);
        }
    }
}