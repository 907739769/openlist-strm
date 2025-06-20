package cn.jackding.openliststrm.controller;

import cn.jackding.openliststrm.service.CopyOpenlistFileService;
import cn.jackding.openliststrm.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Jack
 * @Date 2024/6/23 20:34
 * @Version 1.0.0
 */
@RestController
@RequestMapping("api/v1")
@Slf4j
public class NotifyController {

    @Value("${replaceDir:}")
    private String replaceDir;

    @Autowired
    private CopyOpenlistFileService copyOpenlistFileService;

    @PostMapping("/notify")
    public void notifySync() {
        copyOpenlistFileService.syncFiles("", ConcurrentHashMap.newKeySet());
    }

    @PostMapping("/notifyByDir")
    public void notifyByDir(@RequestBody Map<String, Object> map) {
        log.info("map: " + map);
        String relativePath = "";
        if (StringUtils.hasText(replaceDir) && StringUtils.hasText((CharSequence) map.get("dir"))) {
            relativePath = map.get("dir").toString().replaceFirst(replaceDir, "");
            if (Utils.isVideo(relativePath)) {
                copyOpenlistFileService.syncOneFile(relativePath);
            } else {
                copyOpenlistFileService.syncFiles(relativePath, ConcurrentHashMap.newKeySet());
            }
        } else {
            copyOpenlistFileService.syncFiles(relativePath, ConcurrentHashMap.newKeySet());
        }
    }

}
