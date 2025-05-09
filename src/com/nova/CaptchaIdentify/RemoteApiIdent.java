package com.nova.CaptchaIdentify;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.nova.CommonUtils.MyFileUtils;
import com.nova.CommonUtils.ElementUtils;

import static cn.hutool.core.util.StrUtil.isEmptyIfStr;
import static com.nova.CaptchaIdentify.CaptchaUtils.imageToBase64;
import static com.nova.CaptchaIdentify.CaptchaUtils.writeBytesToFile;
import static com.nova.CommonUtils.Utils.*;
import static com.nova.PrintLog.PrintLog.print_error;
import static com.nova.PrintLog.PrintLog.print_debug;

public class RemoteApiIdent {

    public static String remoteIdentCommon(String Url, String base64Image, String ExpectedStatus, String ExpectedKeywords, Integer ident_time_out){
        // 创建 HTTP 请求对象
        HttpRequest request = HttpUtil.createPost(Url);
        // 设置请求体，这里将 Base64 图片数据作为请求体
        request.body(base64Image);
        //设置超市时间等参数
        request.timeout(ident_time_out);//超时，毫秒

        try{
            // 发送 POST 请求并获取响应
            HttpResponse response = request.execute();
            // 获取响应状态码
            int statusCode = response.getStatus();
            String responseBody = response.body();

            //当前 ExpectedStatus 不为空时, 判断响应状态码是否包含关键字正则
            if (ElementUtils.isNotEmptyObj(ExpectedStatus) && !ElementUtils.isContainOneKeyByRegex(String.valueOf(statusCode), ExpectedStatus)) {
                print_error(String.format("异常状态: [%s] <--> [%s]", ExpectedStatus, statusCode));
                return null;
            }
            //当前 ExpectedKeywords 不为空时, 判断响应体是否包含关键字正则
            if (ElementUtils.isNotEmptyObj(ExpectedKeywords) && !ElementUtils.isContainOneKeyByRegex(responseBody, ExpectedKeywords)) {
                print_error(String.format("异常内容: [%s] <--> [%s]", ExpectedKeywords, responseBody));
                return null;
            }
            print_debug(String.format("Remote OCR API Access Status: [%s]<-->[%s]", statusCode, responseBody));
            return responseBody;
        } catch (Exception exception){
            print_error("Remote OCR API Access Error, Please Check API Is Normal?");
            exception.printStackTrace();
            return null;
        }
    }

    public static String remoteIndentCaptcha(byte[] captcha_data, String remoteApi,
                                             String expectedStatus, String expectedKeywords, String extractRegex,
                                             String expectedRegex, String expectedLength,
                                             Integer ident_time_out){

        String imagePath = MyFileUtils.getFileStrAbsolutePath("captcha.png");
        imagePath = writeBytesToFile(imagePath, captcha_data);
        return remoteIndentCaptcha(imagePath, remoteApi, expectedStatus, expectedKeywords, extractRegex, expectedRegex, expectedLength, ident_time_out);
    }

    /**
     * 远程识别验证码
     * @param imagePath 验证码文件路径
     * @param remoteApi 远程识别API
     * @param expectedStatus 正常识别的响应状态码 一般是200
     * @param expectedKeywords  正常识别的响应含有的关键字 可为空
     * @param extractRegex 从响应中提取验证码的正则表达式 为空时表名都是验证码
     * @param expectedRegex 限定验证码的格式 如 \w+
     * @param expectedLength 限定验证码的长度 如 4
     * @param ident_time_out 远程识别的超时时间
     * @return
     */
    public static String remoteIndentCaptcha(String imagePath, String remoteApi,
                                             String expectedStatus, String expectedKeywords, String extractRegex,
                                             String expectedRegex, String expectedLength,
                                             Integer ident_time_out){

        //从绝地路径提取
        imagePath = MyFileUtils.getFileStrAbsolutePath(imagePath);

        //转base64处理
        String base64Image = imageToBase64(imagePath);
        if (isEmptyIfStr(base64Image)) {
            print_error(String.format("转换失败: 图片[%s] <--> Base64格式失败!!!",  imagePath));
            return null;
        }

        //开始进行识别
        String remoteIdentData = remoteIdentCommon(remoteApi, base64Image, expectedStatus,  expectedKeywords, ident_time_out);
        if (isEmptyIfStr(remoteIdentData)) {
            print_error(String.format("接口错误: 接口[%s] <--> 图片[%s]!!!", imagePath, remoteApi));
            return null;
        }

        //提取响应中的验证码
        String captchaResult = regexExtract(remoteIdentData, extractRegex);
        if (isEmptyIfStr(captchaResult)) {
            print_error(String.format("提取错误: 结果[%s] <--> 正则[%s]", remoteIdentData, extractRegex));
            return null;
        }

        //当前 ExpectedRegex 不为空时, 判断验证码是否符合正则
        if (ElementUtils.isNotEmptyObj(expectedRegex) && !ElementUtils.isContainOneKeyByRegex(captchaResult, expectedRegex)) {
            print_error(String.format("识别错误: 结果[%s] <--> 期望格式:[%s]", captchaResult, expectedRegex));
            return null;
        }

        //当前 captchaResult 不为空时, 判断验证码长度是否正确
        if (isNumber(expectedLength) && Integer.parseInt(expectedLength) !=  captchaResult.length()) {
            print_error(String.format("识别错误: 结果[%s] <--> 期望长度:[%s] <--> 实际长度[%s] ",captchaResult, expectedLength, captchaResult.length()));
            return null;
        }

        //全部排除过后,验证码格式正确
        return captchaResult;
    }

    public static void main(String[] args) {
        //输入图片地址 图片格式转换
        String imagePath = "TestRemote.jpg";
        String remoteApi = "http://127.0.0.1:5000/base64ocr"; // POST 请求的 URL
        String result = remoteIndentCaptcha(imagePath, remoteApi, "200", null, null, "", "4", 5000);
        print_debug(String.format("result:%s", result));
    }
}
