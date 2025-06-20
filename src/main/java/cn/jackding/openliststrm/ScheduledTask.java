package cn.jackding.openliststrm;

import cn.jackding.openliststrm.openlist.OpenlistService;
import cn.jackding.openliststrm.service.CopyOpenlistFileService;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Jack
 * @Date 2024/5/24 13:32
 * @Version 1.0.0
 */
@Log4j2
@Service
public class ScheduledTask {

    @Autowired
    private CopyOpenlistFileService copyOpenlistFileService;

    @Autowired
    private OpenlistService openlistService;

    /**
     * 每天执行两次
     */
    @Scheduled(cron = "${scheduledCron:0 0 6,18 * * ?}")
    public void syncDaily() {
        JSONObject jsonObject = openlistService.copyUndone();
        if (jsonObject == null || !(200 == jsonObject.getInteger("code"))) {
            log.warn("定时任务未执行，因为openlist的task/copy/undone服务不可用");
            return;
        }
        if (CollectionUtils.isEmpty(jsonObject.getJSONArray("data"))) {
            copyOpenlistFileService.syncFiles("", ConcurrentHashMap.newKeySet());
        } else {
            log.warn("定时任务未执行，因为还有正在上传的文件");
        }
    }


}
