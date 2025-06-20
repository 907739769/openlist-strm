package cn.jackding.openliststrm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author Jack
 * @Date 2022/8/2 21:04
 * @Version 1.0.0
 */
@Component
public class Config {

    public static String tgToken;

    public static String tgUserId;

    @Value("${tgToken:}")
    public void setTgToken(String tgToken) {
        Config.tgToken = tgToken;
    }

    @Value("${tgUserId:}")
    public void setTgUserId(String tgUserId) {
        Config.tgUserId = tgUserId;
    }

}
