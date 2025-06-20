package cn.jackding.openliststrm;

import cn.jackding.openliststrm.config.Config;
import cn.jackding.openliststrm.service.StrmService;
import cn.jackding.openliststrm.tg.StrmBot;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
@Slf4j
@EnableAsync
@EnableScheduling
public class OpenlistStrmApplication implements CommandLineRunner {

    @Autowired
    private StrmService strmService;

    @Value("${runAfterStartup:1}")
    private String runAfterStartup;

    @Value("${logLevel:}")
    private String logLevel;

    @Value("${slowMode:0}")
    private String slowMode;

    public static void main(String[] args) {
        SpringApplication.run(OpenlistStrmApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (StringUtils.isNotBlank(logLevel)) {
            Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.valueOf(logLevel));
        }
        if (!"1".equals(slowMode)) {
            System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "49");
        }
        if ("1".equals(runAfterStartup)) {
            strmService.strm();
        } else {
            log.info("启动立即执行任务未启用，等待定时任务处理");
        }
        if (StringUtils.isBlank(Config.tgUserId) || StringUtils.isBlank(Config.tgToken)) {
            return;
        }
        TelegramBotsApi telegramBotsApi;
        try {
            telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        } catch (TelegramApiException e) {
            log.error("", e);
            return;
        }
        DefaultBotOptions botOptions = new DefaultBotOptions();
//        botOptions.setProxyHost(Config.telegramBotProxyHost);
//        botOptions.setProxyPort(Config.telegramBotProxyPort);
//        botOptions.setProxyType(DefaultBotOptions.ProxyType.HTTP);
        //使用AbilityBot创建的事件响应机器人
        try {
            telegramBotsApi.registerBot(new StrmBot(botOptions));
        } catch (TelegramApiException e) {
            log.error("", e);
        }
    }


}
