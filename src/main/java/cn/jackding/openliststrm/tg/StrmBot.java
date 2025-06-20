package cn.jackding.openliststrm.tg;

import cn.jackding.openliststrm.config.Config;
import cn.jackding.openliststrm.service.CopyOpenlistFileService;
import cn.jackding.openliststrm.service.StrmService;
import cn.jackding.openliststrm.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Flag;
import org.telegram.telegrambots.bots.DefaultBotOptions;

import java.util.concurrent.ConcurrentHashMap;

import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.CREATOR;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;

/**
 * @Author Jack
 * @Date 2024/6/4 21:33
 * @Version 1.0.0
 */
@Slf4j
public class StrmBot extends AbilityBot {

    private final ResponseHandler responseHandler = new ResponseHandler(sender, db);

    public StrmBot() {
        super(Config.tgToken, "");
    }

    public StrmBot(DefaultBotOptions options) {
        super(Config.tgToken, "bot", options);
    }

    @Override
    public long creatorId() {
        return Long.parseLong(Config.tgUserId);
    }

    public Ability strm() {
        return Ability.builder()
                .name("strm")
                .info("生成strm")
                .privacy(CREATOR)
                .locality(USER)
                .input(0)
                .action(ctx -> {
                    silent.send("==开始执行strm任务==", ctx.chatId());
                    StrmService strmService = (StrmService) SpringContextUtil.getBean("strmService");
                    strmService.strm();
                    silent.send("==执行strm任务完成==", ctx.chatId());
                })
                .build();
    }

    public Ability strmDir() {
        return Ability.builder()
                .name("strmdir")
                .info("生成指定路径strm")
                .privacy(CREATOR)
                .locality(USER)
                .input(0)
                .action(ctx -> {
                    String parameter;
                    try {
                        parameter = ctx.firstArg();
                    } catch (Exception e) {
                        silent.forceReply("请输入路径", ctx.chatId());
                        return;
                    }
                    if (StringUtils.isBlank(parameter)) {
                        silent.forceReply("请输入路径", ctx.chatId());
                        return;
                    }
                    silent.send("==开始执行指定路径strm任务==", ctx.chatId());
                    StrmService strmService = (StrmService) SpringContextUtil.getBean("strmService");
                    strmService.strmDir(parameter);
                    silent.send("==执行指定路径strm任务完成==", ctx.chatId());
                })
                .reply((bot, upd) -> responseHandler.replyToStrmDir(getChatId(upd), upd.getMessage().getText(), upd.getMessage().getMessageId()), Flag.REPLY,//回复
                        upd -> upd.getMessage().getReplyToMessage().hasText(), upd -> upd.getMessage().getReplyToMessage().getText().equals("请输入路径")//回复的是上面的问题
                )
                .build();
    }

    public Ability sync() {
        return Ability.builder()
                .name("sync")
                .info("同步openlist")
                .privacy(CREATOR)
                .locality(USER)
                .input(0)
                .action(ctx -> {
                    silent.send("==开始执行同步openlist任务==", ctx.chatId());
                    CopyOpenlistFileService copyOpenlistFileService = (CopyOpenlistFileService) SpringContextUtil.getBean("copyOpenlistFileService");
                    copyOpenlistFileService.syncFiles("", ConcurrentHashMap.newKeySet());
                    silent.send("==执行同步openlist任务完成==", ctx.chatId());
                })
                .build();
    }

    public Ability syncDir() {
        return Ability.builder()
                .name("syncdir")
                .info("同步openlist指定目录")
                .privacy(CREATOR)
                .locality(USER)
                .input(0)
                .action(ctx -> {
                    String parameter;
                    try {
                        parameter = ctx.firstArg();
                    } catch (Exception e) {
                        silent.forceReply("请输入路径(格式：源路径#目标路径)", ctx.chatId());
                        return;
                    }
                    if (StringUtils.isBlank(parameter)) {
                        silent.forceReply("请输入路径(格式：源路径#目标路径)", ctx.chatId());
                        return;
                    }
                    if (!parameter.contains("#")) {
                        silent.send("请输入正确的参数，例如：/阿里云盘/电影#/115网盘/电影", ctx.chatId());
                    }
                    String[] strings = parameter.split("#");
                    if (strings.length != 2) {
                        silent.send("请输入正确的参数，例如：/阿里云盘/电影#/115网盘/电影", ctx.chatId());
                    }
                    silent.send("==开始执行同步openlist指定目录任务==", ctx.chatId());
                    CopyOpenlistFileService copyOpenlistFileService = (CopyOpenlistFileService) SpringContextUtil.getBean("copyOpenlistFileService");
                    copyOpenlistFileService.syncFiles(strings[0], strings[1], "", ConcurrentHashMap.newKeySet());
                    silent.send("==执行同步openlist指定目录任务完成==", ctx.chatId());
                })
                .reply((bot, upd) -> responseHandler.replyToSyncDir(getChatId(upd), upd.getMessage().getText(), upd.getMessage().getMessageId()), Flag.REPLY,//回复
                        upd -> upd.getMessage().getReplyToMessage().hasText(), upd -> upd.getMessage().getReplyToMessage().getText().equals("请输入路径(格式：源路径#目标路径)")//回复的是上面的问题
                )
                .build();
    }

}
