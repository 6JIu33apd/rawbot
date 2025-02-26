package ru.kusupovar.rawbot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.kusupovar.rawbot.config.BotConfig;

@Slf4j
@Service
public class TelegramService extends TelegramLongPollingBot {
    private final BotConfig botConfig;
    private final CommandHandler commandHandler;

    public TelegramService(BotConfig botConfig, CommandHandler commandHandler) {
        this.botConfig = botConfig;
        this.commandHandler = commandHandler;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            commandHandler.handleMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            commandHandler.handleCallback(update.getCallbackQuery());
        }
    }
}