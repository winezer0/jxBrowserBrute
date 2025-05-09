package com.fuping.LoadDict;

import cn.hutool.core.io.FileUtil;
import com.fuping.CommonUtils.MyFileUtils;
import com.fuping.LoadConfig.Constant;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static cn.hutool.core.util.StrUtil.isEmptyIfStr;
import static com.fuping.LoadConfig.Constant.DictMode.*;
import static com.fuping.PrintLog.PrintLog.print_error;
import static com.fuping.PrintLog.PrintLog.print_debug;

public class LoadDictUtils {
    public static List<String> readDictFile(String filePath) {
        // 读取文件内容到列表
        String absolutePath = MyFileUtils.getFileStrAbsolutePath(filePath);

        //判断文件是否存在
        if (MyFileUtils.isEmptyFile(absolutePath)){
            print_error(String.format("File Not Found Or Read Empty From [%s]", absolutePath));
            System.exit(0);
            return null;
        }

        //检查文件编码
        String checkEncode = MyFileUtils.checkFileEncode(absolutePath, "UTF-8");
        // 读取文件内容到列表
        List<String> baseLines = FileUtil.readLines(absolutePath, checkEncode);
        List<String> newLines = new ArrayList<>();
        for (String line : baseLines) {
            //去除空行和首尾空格
            String trimmedLine = line.trim();
            if (!trimmedLine.isEmpty()) {
                // 添加非空行到 processedLines
                newLines.add(trimmedLine);
            }
        }

        print_debug(String.format("Read Lines [%s] From File [%s]", newLines.size(), absolutePath));
        return newLines;
    }

    public static HashSet<UserPassPair> createCartesianUserPassPairs(List<String> usernames, List<String> passwords) {
        //创建 笛卡尔积 模式的用户密码对
        HashSet<UserPassPair> userPassPairs = new HashSet<>();

        for (String username : usernames) {
            for (String password : passwords) {
                userPassPairs.add(new UserPassPair(username.trim(), password.trim()));
            }
        }
        print_debug(String.format("Create Cartesian (pairs=max[m*n]) User Pass Pairs [%s]", userPassPairs.size()));
        return userPassPairs;
    }

    public static HashSet<UserPassPair> splitAndCreatUserPassPairs(List<String> pairStringList, String pair_separator) {
        //拆分账号密钥对文件 到用户名密码字典
        HashSet<UserPassPair> userPassPairs = new HashSet<>();
        for (String str : pairStringList) {
            // 使用 split 方法按冒号分割字符串
            String[] parts = str.split(pair_separator, 2);
            if (parts.length == 2) {
                String username = parts[0];
                String password = parts[1];
                userPassPairs.add(new UserPassPair(username.trim(), password.trim()));
            }
        }
        print_debug(String.format("Split And Creat User Pass Pairs [%s]", userPassPairs.size()));
        return userPassPairs;
    }

    public static HashSet<UserPassPair> loadUserPassFile(String userNameFile, String passWordFile,String userPassFile, String pair_separator, Constant.DictMode dictCompoMode
    ){
        //判断是加载账号密码对字典还是加载账号字典
        HashSet<UserPassPair> userPassPairs = new HashSet<>();

        //根据不同的模式生成账号密码对文件
        if(PAIR_FILE.equals(dictCompoMode)){
            if (userPassFile != null){
                //处理账号密码对文件
                List<String> userPassPairList =  readDictFile(userPassFile);
                userPassPairs = splitAndCreatUserPassPairs(userPassPairList, pair_separator);
            }
        }else {
            if (userNameFile != null && passWordFile != null){
                //处理账号密码文件
                List<String> userNameList =  readDictFile(userNameFile);
                List<String> passWordList =  readDictFile(passWordFile);
                userPassPairs = createCartesianUserPassPairs(userNameList, passWordList);
            }
        }
        return userPassPairs;
    }

    public static HashSet<UserPassPair> excludeHistoryPairs(HashSet<UserPassPair> rawUserPassPairs, String historyFile, String separator) {
        HashSet<UserPassPair> userPassPairs = rawUserPassPairs;
        //处理历史账号密码对文件
        if (MyFileUtils.isNotEmptyFile(historyFile)){
            List<String> hisUserPassPairList =  readDictFile(historyFile);
            HashSet<UserPassPair> hisUserPassPairs = splitAndCreatUserPassPairs(hisUserPassPairList, separator);
            userPassPairs = subtractHashSet(rawUserPassPairs, hisUserPassPairs);
        }
        return userPassPairs;
    }

    private static HashSet<UserPassPair> subtractHashSet(HashSet<UserPassPair> inputUserPassPairs, HashSet<UserPassPair> hisUserPassPairs) {
        //将两个Hashset相减
        HashSet<UserPassPair> userPassPairs = new HashSet<>(inputUserPassPairs);
        for (UserPassPair pairToRemove : hisUserPassPairs) {
            userPassPairs.remove(pairToRemove); // 从结果中移除与 set2 中相同的元素
        }
        return userPassPairs;
    }

    public static void showUserPassPairs(HashSet<UserPassPair> userPassPairs){
        //循环输出
        //for (UserPassPair pair : userPassPairs) { System.out.println(pair.getUsername() + " <--> " + pair.getPassword());}
        // 使用 ArrayList 转换为数组后输出
        UserPassPair[] userPassArray = userPassPairs.toArray(new UserPassPair[0]);
        // 使用 Arrays.toString() 输出数组 //需要先重写userPass的toString方法哦
        System.out.println(Arrays.toString(userPassArray));
    }

    public static HashSet<UserPassPair> replaceUserMarkInPass(HashSet<UserPassPair> userPassPairs, String userMark) {
        //仅处理存在的情况
        if(isEmptyIfStr(userMark) || userPassPairs.size()< 1){
            return userPassPairs;
        }

        //替换密码中的用户名标记符号为用户名
        for (UserPassPair pair : userPassPairs) {
            String newPassword = pair.getPassword().replace(userMark, pair.getUsername());
            pair.setPassword(newPassword);
        }
        return userPassPairs;
    }


    public static void main(String[] args) {
        HashSet<UserPassPair> inputUserPassPairs;

        String usernamePath = "dict" + File.separator + "username.txt";
        String passwordPath = "dict" + File.separator + "password.txt";
        //笛卡尔积读取的账号密码字典
        loadUserPassFile(usernamePath, passwordPath, null,null , CARTESIAN);
        //并列读取的账号密码字典
        loadUserPassFile(usernamePath, passwordPath, null,null , PITCHFORK);

        //直接读取账号密码对文件
        String userPassPath = "dict" + File.separator + "user_pass.txt";
        inputUserPassPairs = loadUserPassFile(null, null, userPassPath, ":", PAIR_FILE);

        //排除历史记录中的文件
        String hisUserPassPath = "dict" + File.separator + "history.txt";
        HashSet<UserPassPair> userPassPairs = excludeHistoryPairs(inputUserPassPairs, hisUserPassPath, ":");
        showUserPassPairs(userPassPairs);

        //遍历
        for (UserPassPair userPassPair: userPassPairs){
            print_debug(String.format("write %s", userPassPair.toString(":")));
        }
    }

}
