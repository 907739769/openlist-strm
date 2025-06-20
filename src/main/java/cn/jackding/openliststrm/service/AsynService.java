package cn.jackding.openliststrm.service;

import cn.jackding.openliststrm.openlist.OpenlistService;
import cn.jackding.openliststrm.util.Utils;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 异步线程服务
 *
 * @Author Jack
 * @Date 2024/6/23 12:42
 * @Version 1.0.0
 */
@Service
public class AsynService {

    @Autowired
    private OpenlistService openlistService;

    @Autowired
    private StrmService strmService;

    private final AtomicBoolean isRun = new AtomicBoolean(false);

    /**
     * 判断openlist的复制任务是否完成 完成就执行strm任务
     *
     * @return
     * @Async
     */
    @Async
    public void isCopyDone(String dstDir, String strmDir, Set<String> taskIdList) {
        if (isRun.get() && StringUtils.isBlank(strmDir)) {
            return;
        }
        isRun.set(true);
        Utils.sleep(30);
        while (true) {
            boolean allTasksCompleted = true;
            for (String taskId : taskIdList) {
                JSONObject jsonResponse = openlistService.copyInfo(taskId);
                if (jsonResponse == null) {
                    continue;
                }

                // 检查任务状态
                Integer code = jsonResponse.getInteger("code");
                Integer state = -1;
                if (jsonResponse.getJSONObject("data") != null) {
                    state = jsonResponse.getJSONObject("data").getInteger("state");
                }

                //不是上传成功状态
                if (200 == code && state != 2) {
                    //失败状态了  就重试 状态1是运行中  状态8是等待重试
                    if (state == 7) {
                        openlistService.copyRetry(taskId);
                    }
                    allTasksCompleted = false;
                } else if (404 == code || state == 2) {
                    taskIdList.remove(taskId);
                }
            }
            if (allTasksCompleted) {
                isRun.set(false);
                strmService.strmDir(dstDir + strmDir);// 生成 STRM 文件
                break;// 任务完成，退出循环
            } else {
                Utils.sleep(30);//继续检查
            }
        }
    }

    @Async
    public void isCopyDoneOneFile(String path, String taskId) {
        Utils.sleep(30);
        while (true) {
            JSONObject jsonResponse = openlistService.copyInfo(taskId);
            if (jsonResponse == null) {
                break;
            }
            // 检查任务状态
            Integer code = jsonResponse.getInteger("code");
            Integer state = -1;
            if (jsonResponse.getJSONObject("data") != null) {
                state = jsonResponse.getJSONObject("data").getInteger("state");
            }
            //判定任务是否完成了 完成了就生成strm文件
            if (404 == code || state == 2) {
                strmService.strmOneFile(path);// 生成 STRM 文件
                break;// 任务完成，退出循环
            } else if (state == 7) {
                //失败就重试
                openlistService.copyRetry(taskId);
            }
            Utils.sleep(30);//继续检查
        }
    }

}
