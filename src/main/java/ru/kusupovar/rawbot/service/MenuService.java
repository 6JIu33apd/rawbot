package ru.kusupovar.rawbot.service;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Component
public class MenuService {
    private final MessageService messageService;

    public MenuService(MessageService messageService) {
        this.messageService = messageService;
    }

    public void sendMenu(long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(List.of(
                List.of(InlineKeyboardButton.builder()
                        .text("Курсы")
                        .callbackData("menu")
                        .build())
        ));
        messageService.sendMarkup(chatId, "Выберите раздел:", markup);
    }

    public void sendSubMenu(long chatId) {
        List<String> currencies = List.of("BTC", "ETH", "XRP");

        List<List<InlineKeyboardButton>> buttons = currencies.stream()
                .map(currency -> List.of(InlineKeyboardButton.builder()
                        .text(currency)
                        .callbackData(currency)
                        .build()))
                .toList();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(buttons);
        messageService.sendMarkup(chatId, "Выберите валюту:", markup);
    }
}