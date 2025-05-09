package com.nova.CaptchaIdentify;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

import static com.nova.CommonUtils.MyFileUtils.getFileStrAbsolutePath;
import static com.nova.PrintLog.PrintLog.print_debug;

public class CaptchaUtils {

    public static String imageToBase64(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            FileInputStream imageInputStream = new FileInputStream(imageFile);
            byte[] imageBytes = new byte[(int) imageFile.length()];
            imageInputStream.read(imageBytes);
            imageInputStream.close();

            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return null; // 处理异常，返回null或其他错误信息
        }
    }

    public static String LoadImageToFile(String imageUrl, String imagePath) {
        imagePath = getFileStrAbsolutePath(imagePath);

        //从网页加载 验证码 内容，并保存到文件
        HttpRequest request = HttpUtil.createGet(imageUrl);
        request.timeout(5000); //超时，毫秒
        HttpResponse response = request.execute();
        // 获取响应状态码
        int statusCode = response.getStatus();
        byte[] responseBodyBytes = response.bodyBytes();
        imagePath = writeBytesToFile(imagePath, responseBodyBytes);
        print_debug(String.format("imageUrl:%s  statusCode:%s  imagePath:%s ", imageUrl, statusCode, imagePath));
        return imagePath;
    }


    public static String writeBytesToFile(String filePath, byte[] bytes) {
        try  {
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(bytes);
            fos.close();
        } catch (IOException e) {
            System.err.println(String.format("写入文件时出错： %s", e.getMessage()));
            e.printStackTrace();
            return null;
        }
        return filePath;
    }
}
