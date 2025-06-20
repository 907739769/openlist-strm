package cn.jackding.openliststrm.tg;

import cn.jackding.openliststrm.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * @Author Jack
 * @Date 2024/6/4 17:17
 * @Version 1.0.0
 */
@Slf4j
public class TgSendMsg extends TelegramLongPollingBot {

    public void sendMsg(String msg) {
        if (StringUtils.isBlank(Config.tgUserId) || StringUtils.isBlank(Config.tgToken)) {
            return;
        }
        SendMessage message = new SendMessage();
        message.setChatId(Config.tgUserId);
        message.setText(msg);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("", e);
        }
    }

    @Override
    public String getBotUsername() {
        return "";
    }

    @Override
    public String getBotToken() {
        return Config.tgToken;
    }

    @Override
    public void onUpdateReceived(Update update) {

    }
}
