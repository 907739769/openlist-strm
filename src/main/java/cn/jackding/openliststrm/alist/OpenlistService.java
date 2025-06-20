package cn.jackding.openliststrm.openlist;

import cn.jackding.openliststrm.util.Utils;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author Jack
 * @Date 2024/6/22 18:36
 * @Version 1.0.0
 */
@Service
@Slf4j
public class OpenlistService {

    @Value("${openlistServerToken}")
    private String token;

    @Value("${openlistServerUrl}")
    private String url;

    @Value("${maxIdleConnections:5}")
    private int maxIdleConnections;

    @Value("${refresh:1}")
    private String refresh;

    private OkHttpClient client;

    // 在 Bean 初始化时预创建 OkHttpClient
    @PostConstruct
    public void init() {
        client = new OkHttpClient.Builder()
                .connectTimeout(90, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(maxIdleConnections, 5, TimeUnit.SECONDS))
                .build();
        log.info("OkHttpClient initialized successfully.");
    }

    public JSONObject getOpenlist(String path) {
        JSONObject jsonResponse = null;

        // 设置请求头
        Headers headers = new Headers.Builder()
                .add("Content-Type", "application/json")
                .add("Accept", "application/json")
                .add("Authorization", token)
                .build();

        // 构建请求体数据
        JSONObject requestBodyJson = new JSONObject();
        requestBodyJson.put("path", path);
        requestBodyJson.put("password", "");
        requestBodyJson.put("page", 1);
        requestBodyJson.put("per_page", 0);
        if ("1".equals(refresh)) {
            requestBodyJson.put("refresh", true);
        } else {
            requestBodyJson.put("refresh", false);
        }
        String requestBodyString = requestBodyJson.toJSONString();

        // 构建请求
        Request request = new Request.Builder()
                .url(url + "/api/fs/list")
                .headers(headers)
                .post(RequestBody.create(MediaType.parse("application/json"), requestBodyString))
                .build();

        log.debug("开始获取openlist目录{}", path);
        for (int i = 0; i < 3; i++) {
            // 发送请求并处理响应
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    // 获取响应体
                    String responseBody = response.body().string();

                    // 解析 JSON 响应
                    jsonResponse = JSONObject.parseObject(responseBody);

                    // 处理响应数据
                    if (200 == jsonResponse.getInteger("code")) {
                        log.debug("获取openlist目录成功{}", path);
                        return jsonResponse;
                    } else {
                        log.info("Response Body: " + jsonResponse.toJSONString());
                        log.warn("获取openlist目录{}第{}次失败", path, i + 1);
                        Utils.sleep(1);
                    }

                } else {
                    log.warn("Request failed with code: {}", response.code());
                    log.error("Request failed with response :{}", response);
                    return jsonResponse;
                }
            } catch (Exception e) {
                log.error("获取openlist目录失败{}", path);
                log.error("", e);
            }
        }
        return jsonResponse;
    }

    public JSONObject getFile(String path) {
        JSONObject jsonResponse;

        // 设置请求头
        Headers headers = new Headers.Builder()
                .add("Content-Type", "application/json")
                .add("Accept", "application/json")
                .add("Authorization", token)
                .build();

        // 构建请求体数据
        JSONObject requestBodyJson = new JSONObject();
        requestBodyJson.put("path", path);
        requestBodyJson.put("password", "");
        requestBodyJson.put("page", 1);
        requestBodyJson.put("per_page", 0);
        if ("1".equals(refresh)) {
            requestBodyJson.put("refresh", true);
        } else {
            requestBodyJson.put("refresh", false);
        }
        String requestBodyString = requestBodyJson.toJSONString();

        // 构建请求
        Request request = new Request.Builder()
                .url(url + "/api/fs/get")
                .headers(headers)
                .post(RequestBody.create(MediaType.parse("application/json"), requestBodyString))
                .build();

        log.debug("开始获取openlist文件{}", path);

        // 发送请求并处理响应
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                // 获取响应体
                String responseBody = response.body().string();

                // 解析 JSON 响应
                jsonResponse = JSONObject.parseObject(responseBody);


                log.debug("获取openlist文件成功{}", path);
                return jsonResponse;


            } else {
                log.warn("Request failed with code: {}", response.code());
                log.error("Request failed with response :{}", response);
                return null;
            }
        } catch (Exception e) {
            log.error("获取openlist文件失败{}", path);
            log.error("", e);
        }

        return null;
    }


    public JSONObject copyOpenlist(String srcDir, String dstDir, List<String> names) {
        JSONObject jsonResponse = null;

        // 设置请求头
        Headers headers = new Headers.Builder()
                .add("Content-Type", "application/json")
                .add("Accept", "application/json")
                .add("Authorization", token)
                .build();

        // 构建请求体数据
        JSONObject requestBodyJson = new JSONObject();
        requestBodyJson.put("src_dir", srcDir);
        requestBodyJson.put("dst_dir", dstDir);
        requestBodyJson.put("names", names);
        String requestBodyString = requestBodyJson.toJSONString();

        // 构建请求
        Request request = new Request.Builder()
                .url(url + "/api/fs/copy")
                .headers(headers)
                .post(RequestBody.create(MediaType.parse("application/json"), requestBodyString))
                .build();

        log.debug("开始复制[{}]=>[{}]", srcDir + File.separator + names.get(0), dstDir);
        for (int i = 0; i < 3; i++) {
            // 发送请求并处理响应
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    // 获取响应体
                    String responseBody = response.body().string();

                    // 解析 JSON 响应
                    jsonResponse = JSONObject.parseObject(responseBody);

                    // 处理响应数据
                    if (200 == jsonResponse.getInteger("code")) {
                        log.debug("复制[{}]=>[{}]成功", srcDir + File.separator + names.get(0), dstDir);
                        return jsonResponse;
                    } else {
                        log.warn("Response Body: " + jsonResponse.toJSONString());
                        log.error("复制[{}]=>[{}]第{}次失败", srcDir + File.separator + names.get(0), dstDir, i + 1);
                        Utils.sleep(1);
                    }

                } else {
                    log.warn("Request failed with code: {}", response.code());
                    log.error("Request failed with response :{}", response);
                    return jsonResponse;
                }
            } catch (Exception e) {
                log.error("复制[{}]=>[{}]失败", srcDir + File.separator + names.get(0), dstDir);
                log.error("", e);
            }
        }
        return jsonResponse;
    }


    public JSONObject mkdir(String path) {
        JSONObject jsonResponse;

        // 设置请求头
        Headers headers = new Headers.Builder()
                .add("Content-Type", "application/json")
                .add("Accept", "application/json")
                .add("Authorization", token)
                .build();

        // 构建请求体数据
        JSONObject requestBodyJson = new JSONObject();
        requestBodyJson.put("path", path);
        String requestBodyString = requestBodyJson.toJSONString();

        // 构建请求
        Request request = new Request.Builder()
                .url(url + "/api/fs/mkdir")
                .headers(headers)
                .post(RequestBody.create(MediaType.parse("application/json"), requestBodyString))
                .build();

        log.debug("开始创建openlist目录{}", path);

        // 发送请求并处理响应
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                // 获取响应体
                String responseBody = response.body().string();

                // 解析 JSON 响应
                jsonResponse = JSONObject.parseObject(responseBody);


                log.debug("创建openlist目录完成{}", path);
                return jsonResponse;


            } else {
                log.warn("Request failed with code: {}", response.code());
                log.error("Request failed with response :{}", response);
                return null;
            }
        } catch (Exception e) {
            log.warn("创建openlist目录失败{}", path);
            log.error("", e);
        }

        return null;
    }

    /**
     * 获取未完成的复制任务
     *
     * @return
     */
    public JSONObject copyUndone() {
        JSONObject jsonResponse;

        // 设置请求头
        Headers headers = new Headers.Builder()
                .add("Content-Type", "application/json")
                .add("Accept", "application/json")
                .add("Authorization", token)
                .build();

        // 构建请求
        Request request = new Request.Builder()
                .url(url + "/api/task/copy/undone")
                .headers(headers)
                .get()
                .build();

        // 发送请求并处理响应
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                // 获取响应体
                String responseBody = response.body().string();

                // 解析 JSON 响应
                jsonResponse = JSONObject.parseObject(responseBody);
                return jsonResponse;
            } else {
                log.warn("Request failed with code: {}", response.code());
                log.error("Request failed with response :{}", response);
                return null;
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    /**
     * 获取复制任务的信息
     *
     * @return
     */
    public JSONObject copyInfo(String tid) {
        JSONObject jsonResponse;

        // 设置请求头
        Headers headers = new Headers.Builder()
                .add("Accept", "application/json")
                .add("Authorization", token)
                .build();

        // 创建请求体，传递参数
        RequestBody formBody = new FormBody.Builder()
                .add("tid", tid)
                .build();

        // 构建请求
        Request request = new Request.Builder()
                .url(url + "/api/task/copy/info" + "?tid=" + tid)
                .headers(headers)
                .post(formBody)
                .build();

        // 发送请求并处理响应
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                // 获取响应体
                String responseBody = response.body().string();

                // 解析 JSON 响应
                jsonResponse = JSONObject.parseObject(responseBody);
                return jsonResponse;
            } else {
                log.warn("Request failed with code: {}", response.code());
                log.error("Request failed with response :{}", response);
                return null;
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    /**
     * 获取复制任务的信息
     *
     * @return
     */
    public JSONObject copyRetry(String tid) {
        JSONObject jsonResponse;

        // 设置请求头
        Headers headers = new Headers.Builder()
                .add("Accept", "application/json")
                .add("Authorization", token)
                .build();

        // 创建请求体，传递参数
        RequestBody formBody = new FormBody.Builder()
                .add("tid", tid)
                .build();

        // 构建请求
        Request request = new Request.Builder()
                .url(url + "/api/task/copy/retry" + "?tid=" + tid)
                .headers(headers)
                .post(formBody)
                .build();

        // 发送请求并处理响应
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                // 获取响应体
                String responseBody = response.body().string();

                // 解析 JSON 响应
                jsonResponse = JSONObject.parseObject(responseBody);
                return jsonResponse;
            } else {
                log.warn("Request failed with code: {}", response.code());
                log.error("Request failed with response :{}", response);
                return null;
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

}
