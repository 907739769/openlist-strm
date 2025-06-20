package cn.jackding.openliststrm.service;

import cn.jackding.openliststrm.openlist.OpenlistService;
import cn.jackding.openliststrm.util.Utils;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * @Author Jack
 * @Date 2024/6/10 20:13
 * @Version 1.0.0
 */
@Service
@Slf4j
public class StrmService {


    @Value("${openlistScanPath}")
    private String path;

    @Value("${openlistServerUrl}")
    private String url;

    @Value("${slowMode:0}")
    private String slowMode;

    @Value("${output.dir}")
    private String outputDir;

    @Value("${encode:1}")
    private String encode;

    @Value("${isDownSub:0}")
    private String isDownSub;

    @Autowired
    private OpenlistService openlistService;

    private final Set<String> cache = ConcurrentHashMap.newKeySet();

    public void strm() {
        strmDir(path);
    }

    public void strmDir(String path) {
        log.info("开始执行指定路径strm任务{}", LocalDateTime.now());
        Utils.sendTgMsg("开始执行strm任务");
        try {
            getData(path, outputDir + File.separator + path.replace("/", File.separator));
        } catch (Exception e) {
            Utils.sendTgMsg("strm任务执行出错");
            log.error("", e);
        } finally {
            log.info("strm任务执行完成{}", LocalDateTime.now());
            Utils.sendTgMsg("strm任务执行完成");
        }
    }

    public void strmOneFile(String path) {
        //判断是否处理过
        if (cache.contains(path)) {
            log.info("文件已处理过，跳过处理{}", path);
            return;
        }
        String fileName = path.substring(path.lastIndexOf("/"), path.lastIndexOf(".")).replaceAll("[\\\\/:*?\"<>|]", "");
        File file = new File(outputDir + File.separator + path.substring(0, path.lastIndexOf("/")).replace("/", File.separator));
        if (!file.exists()) {
            file.mkdirs();
        }
        try (FileWriter writer = new FileWriter(outputDir + File.separator + path.substring(0, path.lastIndexOf("/")).replace("/", File.separator) + File.separator + (fileName.length() > 255 ? fileName.substring(0, 250) : fileName) + ".strm")) {
            String encodePath = path;
            if ("1".equals(encode)) {
                encodePath = URLEncoder.encode(path, "UTF-8").replace("+", "%20").replace("%2F", "/");
            }
            writer.write(url + "/d" + encodePath);
            cache.add(path);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    public void getData(String path, String localPath) {

        File outputDirFile = new File(localPath);
        outputDirFile.mkdirs();

        JSONObject jsonObject = openlistService.getOpenlist(path);
        if (jsonObject != null && 200 == jsonObject.getInteger("code")) {
            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("content");
            if (jsonArray == null) {
                return;
            }
            //判断是不是用多线程流
            Stream<Object> stream;
            if ("1".equals(slowMode)) {
                stream = jsonArray.stream().sequential();
            } else {
                stream = jsonArray.stream().parallel();
            }

            stream.forEach(obj -> {
                JSONObject object = (JSONObject) obj;
                String name = object.getString("name");
                if (object.getBoolean("is_dir")) {
                    String newLocalPath = localPath + File.separator + (name.length() > 255 ? name.substring(0, 250) : name);
                    File file = new File(newLocalPath);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    getData(path + "/" + name, newLocalPath);
                } else {
                    //判断是否处理过
                    if (cache.contains(path + "/" + name)) {
                        log.info("文件已处理过，跳过处理" + path + "/" + name);
                        return;
                    }
                    //视频文件
                    if (Utils.isVideo(name)) {
                        String fileName = name.substring(0, name.lastIndexOf(".")).replaceAll("[\\\\/:*?\"<>|]", "");
                        try (FileWriter writer = new FileWriter(localPath + File.separator + (fileName.length() > 255 ? fileName.substring(0, 250) : fileName) + ".strm")) {
                            String encodePath = path + "/" + name;
                            if ("1".equals(encode)) {
                                encodePath = URLEncoder.encode(path + "/" + name, "UTF-8").replace("+", "%20").replace("%2F", "/");
                            }
                            writer.write(url + "/d" + encodePath);
                            cache.add(path + "/" + name);
                        } catch (Exception e) {
                            log.error("", e);
                        }
                    }

                    //字幕文件
                    if ("1".equals(isDownSub) && Utils.isSrt(name)) {
                        String url = openlistService.getFile(path + "/" + name).getJSONObject("data").getString("raw_url");
                        String fileName = name.replaceAll("[\\\\/:*?\"<>|]", "");
                        downloadFile(url, localPath + File.separator + (fileName.length() > 255 ? fileName.substring(0, 250) : fileName) + name.substring(name.lastIndexOf(".")));
                        cache.add(path + "/" + name);
                    }
                }
            });

        }

    }

    public static void downloadFile(String fileURL, String saveDir) {
        if (StringUtils.isBlank(fileURL)) {
            return;
        }
        // 创建URL对象
        URL url = null;
        try {
            url = new URL(fileURL);
        } catch (Exception e) {
            log.error("文件{}下载失败1", fileURL);
            throw new RuntimeException(e);
        }
        // 打开连接
        URLConnection connection = null;
        try {
            connection = url.openConnection();
        } catch (IOException e) {
            log.error("文件{}下载失败2", fileURL);
            throw new RuntimeException(e);
        }
        try (InputStream inputStream = new BufferedInputStream(connection.getInputStream()); FileOutputStream outputStream = new FileOutputStream(saveDir)) {

            byte[] buffer = new byte[1024];
            int bytesRead = -1;

            // 读取文件并写入到本地
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

        } catch (IOException ex) {
            log.error("文件{}下载失败3", fileURL);
            log.error("", ex);
        }
    }

}
