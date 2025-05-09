package com.fuping.CommonUtils;

import cn.hutool.core.io.FileUtil;
import com.fuping.LoadDict.UserPassPair;

import java.io.File;

import static cn.hutool.core.io.CharsetDetector.detect;
import static com.fuping.LoadConfig.MyConst.*;
import static com.fuping.PrintLog.PrintLog.print_debug;

public class MyFileUtils {
    public static String getFileStrAbsolutePath(String fileStr) {
        //获取文件的物理路径
        return new File(fileStr).getAbsolutePath();
    }

    public static boolean isEmptyFile(String fileStr) {
        //判断文件是否为空
        String absolutePath = getFileStrAbsolutePath(fileStr);
        return FileUtil.isEmpty(new File(absolutePath));
    }

    public static boolean isNotEmptyFile(String fileStr) {
        //判断文件是否不为空
        return !isEmptyFile(fileStr);
    }

    public static String checkFileEncode(String absolutePath, String defaultEncode){
        //检测文件编码
        String encoding = defaultEncode;
        if (isNotEmptyFile(absolutePath)){
            try {
                encoding = detect(new File(absolutePath)).name();
                print_debug(String.format("Detect File Encoding [%s] From [%s]", encoding, absolutePath));
            } catch (Exception e){
                encoding = defaultEncode;
            }
        }
        return encoding;
    }

    public static String genFileNameByUrl(String urlString, String defaultPath, String suffix, boolean useAbsolutePath){
        String filename = defaultPath;

        if (ElementUtils.isNotEmptyObj(urlString)){
            filename = urlString.split("[\\\\\"*?<>|%'#]")[0]; // 按照 # 或 ? 进行切割
            filename = filename.replaceAll("[/:]", "_"); // 对URL的特殊字符进行替换
            filename = String.format("%s%s", filename, suffix);
        }

        return  useAbsolutePath ? getFileStrAbsolutePath(filename): filename;
    }

    public static boolean writeUserPassPairToFile(String historyFile, String separator, UserPassPair userPassPair){
        //写入账号密码对文件到历史记录文件
        try {
            historyFile = getFileStrAbsolutePath(historyFile);
            String content = String.format("%s%s%s",userPassPair.getUsername(), separator, userPassPair.getPassword());
            // 使用 Hutool 写入字符串到文件
            FileUtil.appendString(String.format("%s\n", content), historyFile, "UTF-8");
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static boolean writeTitleToFile(String historyFile, String content){
        //写入账号密码对文件到历史记录文件
        try {
            historyFile = getFileStrAbsolutePath(historyFile);
            if(isEmptyFile(historyFile)){
                FileUtil.appendString(String.format("%s\n", content), historyFile, "UTF-8");
            }
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static boolean writeLineToFile(String historyFile, String content){
        //写入账号密码对文件到历史记录文件
        try {
            historyFile = getFileStrAbsolutePath(historyFile);
            FileUtil.appendString(String.format("%s\n", content), historyFile, "UTF-8");
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static void initBaseOnLoginUrlFile(String login_url) {
        //根据当前登录URL生成 history 文件名称
        globalCrackHistoryFilePath = genFileNameByUrl(login_url, "dict/CrackHistory.log", ".CrackHistory.log", true);
        globalCrackLogRecodeFilePath = genFileNameByUrl(login_url, "dict/CrackLogRecode.csv", ".CrackLogRecode.csv", true);
        globalLoginSuccessFilePath = genFileNameByUrl(login_url, "dict/LoginSuccess.log", ".LoginSuccess.log", true);
        globalLoginFailureFilePath = genFileNameByUrl(login_url, "dict/LoginFailure.log", ".LoginFailure.log", true);
        globalErrorCaptchaFilePath = genFileNameByUrl(login_url, "dict/ErrorCaptcha.log", ".ErrorCaptcha.log", true);
        globalUnknownStatusFilePath = genFileNameByUrl(login_url, "dict/UnknownStatus.log", ".UnknownStatus.log", true);
    }
}
