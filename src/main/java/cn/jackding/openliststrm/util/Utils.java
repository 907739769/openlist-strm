package cn.jackding.openliststrm.util;

import cn.jackding.openliststrm.tg.TgSendMsg;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @Author Jack
 * @Date 2024/6/22 19:23
 * @Version 1.0.0
 */
@Slf4j
public class Utils {

    public static boolean isVideo(String name) {
        return name.toLowerCase().endsWith(".mp4") || name.toLowerCase().endsWith(".mkv")
                || name.toLowerCase().endsWith(".avi") || name.toLowerCase().endsWith(".mov")
                || name.toLowerCase().endsWith(".rmvb") || name.toLowerCase().endsWith(".flv")
                || name.toLowerCase().endsWith(".webm") || name.toLowerCase().endsWith(".m3u8")
                || name.toLowerCase().endsWith(".wmv") || name.toLowerCase().endsWith(".iso") || name.toLowerCase().endsWith(".ts");

    }

    public static boolean isSrt(String name) {
        return name.toLowerCase().endsWith(".ass") || name.toLowerCase().endsWith(".srt");
    }

    public static void sleep(long l) {
        try {
            TimeUnit.SECONDS.sleep(l);
        } catch (InterruptedException e) {
            log.error("", e);
        }
    }

    public static void sendTgMsg(String msg) {
        new TgSendMsg().sendMsg(msg);
    }

}
