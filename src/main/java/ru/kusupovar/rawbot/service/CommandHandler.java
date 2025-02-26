package ru.kusupovar.rawbot.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.Map;

@Service
public class CommandHandler {
    private final MenuService menuService;
    private final CryptoService cryptoService;
    private final MessageService messageService;
    private final SubscriptionService subscriptionService;

    public CommandHandler(MenuService menuService, CryptoService cryptoService,
                          MessageService messageService, SubscriptionService subscriptionService) {
        this.menuService = menuService;
        this.cryptoService = cryptoService;
        this.messageService = messageService;
        this.subscriptionService = subscriptionService;
    }

    public void handleMessage(Message message) {
        long chatId = message.getChatId();
        String messageText = message.getText().toLowerCase();

        Map<String, Runnable> commands = Map.of(
                "/subscribe", () -> {
                    subscriptionService.subscribe(chatId);
                    messageService.sendMessage(chatId, "Вы подписались на рассылку!");
                },
                "/unsubscribe", () -> {
                    subscriptionService.unsubscribe(chatId);
                    messageService.sendMessage(chatId, "Вы отписались от рассылки.");
                },
                "/menu", () -> menuService.sendMenu(chatId)
        );

        commands.getOrDefault(messageText,
                () -> messageService.sendMessage(chatId, "Неизвестная команда. Используйте кнопку Меню")
        ).run();
    }

    public void handleCallback(CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getMessage().getChatId();
        String callbackText = callbackQuery.getData();

        switch (callbackText) {
            case "menu" -> menuService.sendSubMenu(chatId);
            case "BTC", "ETH", "XRP" -> messageService.sendMessage(chatId, "Текущая цена " + callbackText + ": " +
                    String.format("%.2f", cryptoService.getLatestPrice(callbackText)));
            default -> messageService.sendMessage(chatId, "Неизвестная команда. Используйте кнопку Меню");
        }
    }
}