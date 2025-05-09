package com.fuping.LoadConfig;

public class Constant {

//    public static final String LOADING_START = "loading_start";
//    public static final String LOADING_FINISH = "loading_finish";
//    public static final String LOADING_FAILED = "loading_failed";
//    public static final String LOADING_UNKNOWN = "loading_unknown"; //在没有获取到状态时使用


    // 定义 ActionStatus 枚举
    public enum LoadingStatus {
        LOADING_START, LOADING_FINISH, LOADING_FAILED, LOADING_UNKNOWN;

        // 自定义从字符串创建枚举的方法，处理大小写不敏感的情况
        public static LoadingStatus fromString(String text) {
            if (text != null) {
                for (LoadingStatus b : LoadingStatus.values()) {
                    if (text.equalsIgnoreCase(b.name())) {
                        return b;
                    }
                }
            }
            throw new IllegalArgumentException("No matching enum found for text: " + text);
        }
    }

//    public static final String LOGIN_SUCCESS = "login_success";  //登录成功
//    public static final String LOGIN_FAILURE = "login_failure";  //登录失败
//    public static final String ERROR_CAPTCHA = "error_captcha";  //验证码错误常量

    // 定义 ActionStatus 枚举
    public enum LoginStatus {
        LOGIN_SUCCESS, LOGIN_FAILURE, ERROR_CAPTCHA;

        // 自定义从字符串创建枚举的方法，处理大小写不敏感的情况
        public static LoginStatus fromString(String text) {
            if (text != null) {
                for (LoginStatus b : LoginStatus.values()) {
                    if (text.equalsIgnoreCase(b.name())) {
                        return b;
                    }
                }
            }
            throw new IllegalArgumentException("No matching enum found for text: " + text);
        }
    }

    // 定义 DictMode 枚举
    public enum DictMode {
        // "pair_file"; //账号密码对文件
        // "cartesian"; //默认 交叉模式 常用

        PAIR_FILE, PITCHFORK, CARTESIAN;

        // 自定义从字符串创建枚举的方法，处理大小写不敏感的情况
        public static DictMode fromString(String text) {
            if (text != null) {
                for (DictMode b : DictMode.values()) {
                    if (text.equalsIgnoreCase(b.name())) {
                        return b;
                    }
                }
            }
            throw new IllegalArgumentException("No matching enum found for text: " + text);
        }
    }

    // 定义 ActionStatus 枚举
    public enum EleFoundStatus {
        SUCCESS, FAILURE, BREAK, CONTINUE;
        // 自定义从字符串创建枚举的方法，处理大小写不敏感的情况
        public static EleFoundStatus fromString(String text) {
            if (text != null) {
                for (EleFoundStatus b : EleFoundStatus.values()) {
                    if (text.equalsIgnoreCase(b.name())) {
                        return b;
                    }
                }
            }
            throw new IllegalArgumentException("No matching enum found for text: " + text);
        }
    }


    // 定义 HttpMethod 枚举 主要是登录和验证码请求相关的HTTP方法
    public enum HttpMethod {
        GET, POST, PUT, OPTIONS, HEAD;
        // 自定义从字符串创建枚举的方法，处理大小写不敏感的情况
        public static HttpMethod fromString(String text) {
            if (text != null) {
                for (HttpMethod b : HttpMethod.values()) {
                    if (text.equalsIgnoreCase(b.name())) {
                        return b;
                    }
                }
            }
            throw new IllegalArgumentException("No matching enum found for text: " + text);
        }
    }


    // 定义 HttpMethod 枚举 主要是登录和验证码请求相关的HTTP方法
    public enum EleFindType {
         ID, NAME, CLASS, CSS, XPATH;
        // 自定义从字符串创建枚举的方法，处理大小写不敏感的情况
        public static EleFindType fromString(String text) {
            if (text != null) {
                for (EleFindType b : EleFindType.values()) {
                    if (text.equalsIgnoreCase(b.name())) {
                        return b;
                    }
                }
            }
            throw new IllegalArgumentException("No matching enum found for text: " + text);
        }
    }


}
