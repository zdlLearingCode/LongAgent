package com.dragon.agentCore.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * 网页抓取工具
 * 用于获取指定URL的网页内容
 *
 * @author dlzhang13
 * @create 2026/4/29
 */
@Slf4j
@Component
public class WebFetchTool {

    @Tool(
        name = "fetchWebPage",
        description = "获取指定URL的网页内容。当需要访问网页、查看网页内容或从互联网获取信息时使用此工具。"
    )
    public String fetchWebPage(
        @ToolParam(description = "要访问的网页URL地址，必须是完整的HTTP或HTTPS地址")
        String url
    ) {
        log.info("开始抓取网页: {}", url);

        HttpURLConnection connection = null;
        try {
            // 创建URL连接
            URI uri = new URI(url);
            connection = (HttpURLConnection) uri.toURL().openConnection();

            // 设置请求方法和属性
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000); // 10秒连接超时
            connection.setReadTimeout(60000);    // 60秒读取超时
            connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            // 检查响应码
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                String errorMsg = String.format("请求失败，HTTP状态码: %d", responseCode);
                log.error(errorMsg);
                return errorMsg;
            }

            // 读取响应内容
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String content = reader.lines().collect(Collectors.joining("\n"));
                log.info("成功抓取网页，内容长度: {} 字符", content.length());
                return content;
            }

        } catch (Exception e) {
            String errorMsg = String.format("抓取网页失败: %s", e.getMessage());
            log.error(errorMsg, e);
            return errorMsg;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
