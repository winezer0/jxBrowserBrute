package com.nova.LoadConfig;

import com.nova.LoadDict.UserPassPair;

import static com.nova.LoadConfig.Constant.DictMode.CARTESIAN;
import static com.nova.LoadConfig.Constant.EleFindType.XPATH;
import static com.nova.LoadConfig.Constant.EleFoundStatus.BREAK;
import static com.nova.LoadConfig.Constant.EleFoundStatus.CONTINUE;
import static com.nova.LoadConfig.Constant.HttpMethod.GET;
import static com.nova.LoadConfig.Constant.HttpMethod.POST;
import static com.nova.PrintLog.PrintLog.print_debug;

public class MyConst {
    //设置配置参数名称
    public static String globalProgramVersion = "NOVASEC 4.0.1" ;

    // 私有静态成员变量，用于保存单例实例
    private static MyConst MyConstInstance;

    //本次全局变量命名规范,设置在UI上做初始值的加init或default,完整的全局变量加global
    public static UserPassPair[] globalUserPassPairsArray = null;

    //存储默认历史记录文件
    public static String globalCrackHistoryFilePath = null;   //记录成功爆破的历史
    public static String globalCrackLogRecodeFilePath = null;   //记录爆破记录

    public static String globalLoginSuccessFilePath = null;   //记录密码正确的记录
    public static String globalLoginFailureFilePath = null;   //记录密码错误的记录
    public static String globalErrorCaptchaFilePath = null;   //记录验证错误的记录
    public static String globalUnknownStatusFilePath = null;   //记录未知状态的记录

    //JxBrowser相关配置参数
    public static String GLOBAL_BROWSER_PROXY_STR = null;
    public static String GLOBAL_BROWSER_USERAGENT = null;
    public static boolean GLOBAL_CLEAR_COOKIES_SWITCH = false;
    public static String GLOBAL_BROWSER_INIT_COOKIES = null;

    //字典文件相关参数
    public static String GLOBAL_USERNAME_FILE = null;
    public static String GLOBAL_PASSWORD_FILE = null;

    public static String GLOBAL_USER_PASS_FILE = null;
    public static String GLOBAL_PAIR_SEPARATOR = null;

    public static String default_dict_compo_mode = null;      //指定字典组合方式 cartesian  pair_file

    public static String GLOBAL_USER_MARK_IN_PASS = "%USER%";  //密码内的用户名变量
    public static boolean GLOBAL_EXCLUDE_HISTORY_SWITCH = false;    //是否排除历史爆破记录

    //记录账号密码文件是否修改过,如果没有修改的话,就考虑不重新读取
    public static long globalUserFileLastModified = 0;
    public static long globalPassFileLastModified = 0;
    public static long globalPairFileLastModified = 0;

    //登录配置参数
    public static String default_login_access_url = null;
    public static String default_login_actual_url = null;  //登录包的实际请求URL
    public static Constant.HttpMethod default_login_actual_method = null;  //登录包的实际请求方法

    public static String default_pre_click_ele_value = null;
    public static Constant.EleFindType default_pre_click_ele_type = null;

    public static String default_name_box_ele_value = null;
    public static Constant.EleFindType default_name_box_ele_type = null;

    public static String default_pass_box_ele_value = null;
    public static Constant.EleFindType default_pass_box_ele_type = null;

    public static String default_submit_btn_ele_value = null;
    public static Constant.EleFindType default_submit_btn_ele_type = null;

    public static boolean default_show_browser_switch = false;

    public static boolean GLOBAL_LOGIN_PAGE_RELOAD_PER_TIME = true; //是否每次都重新加载登录页面
    public static int GLOBAL_LOGIN_PAGE_LOAD_TIME = 30;  //登录页面加载超时时间
    public static boolean GLOBAL_LOGIN_PAGE_LOAD_TIMEOUT_REWORK = true; //登录页面加载超时是否重头再来

    public static int default_login_page_wait_time = 1000;  //登录页面加载后的等待时间

    //查找定位元素的配置
    public static Integer GLOBAL_FIND_ELE_RETRY_TIMES = null;
    public static Integer GLOBAL_FIND_ELE_DELAY_TIME = null;

    //登录按钮点击后的等待时间
    public static boolean default_submit_auto_wait_switch = true; //是否自动等待模式常量
    public static int default_submit_fixed_wait_time = 2000; //常规模式下点击提交按钮后的 固定的等待时间
    public static long GLOBAL_SUBMIT_AUTO_WAIT_LIMIT = 5000; //自动等待模式下的 超时等待时间设置
    public static long GLOBAL_SUBMIT_AUTO_WAIT_INTERVAL = 500; //自动等待模式下的 每次等待时间

    //是否保存无法识别当前链接状态的情况
    public static boolean default_store_unknown_load_status = false;

    //响应判断的关键字正则
    public static String default_resp_key_success_regex = null;
    public static String default_resp_key_failure_regex = null;
    public static String default_resp_key_captcha_regex = null;

    public static String default_captcha_actual_url = null; //验证码请求包的实际请求URL
    public static Constant.HttpMethod default_captcha_actual_method = null;  //验证码请求包的实际请求方法

    public static String default_captcha_box_ele_value = null;
    public static Constant.EleFindType default_captcha_box_ele_type = null;

    public static boolean default_ident_captcha_switch = false;

    //验证码识别相关配置变量
    public static int default_ident_time_out=1000;  //验证码识别超时毫秒

    public static String default_ident_format_regex=null;  //验证码格式正则校验
    public static String default_ident_format_length=null; //验证码格式长度校验

    public static String default_remote_ident_url =null;  //远程模式 默认的API地址
    public static String default_remote_extract_regex =null; //远程模式 从响应中提取验证码的正则
    public static String default_remote_expected_status =null; //访问成功状态正则匹配
    public static String default_remote_expected_keywords =null;  //访问成功响应正则匹配

    //定义查找元素失败后的操作
    public static Constant.EleFoundStatus GLOBAL_BROWSER_CLOSE_ACTION;     //浏览器关闭后的动作  break
    public static Constant.EleFoundStatus GLOBAL_FIND_ELE_ILLEGAL_ACTION;  //查找到不合法的动作时 continue
    public static Constant.EleFoundStatus GLOBAL_FIND_ELE_NULL_ACTION;     //没有找到元素对应的操作 continue
    public static Constant.EleFoundStatus GLOBAL_FIND_ELE_EXCEPTION_ACTION; //发生其他异常时的动作 continue

    //定义查找元素的方案
    public static boolean default_js_mode_switch; //是否使用JS模式进行元素查找 仅支持CSS和XPATH

    public static String GLOBAL_MATCH_BLOCK_SUFFIX; //不需要进行响应URL内容匹配的后缀类型

    public static boolean default_match_login_url_switch; //是否仅对登录包进行响应提取
    public static boolean GLOBAL_UNKNOWN_ERROR_NOT_STOP; //遇到未知异常时自动继续重启爆破

    public MyConst(){
        ConfigReader configReader = ConfigReader.getInstance();
        //读取代理配置参数
        GLOBAL_BROWSER_PROXY_STR = configReader.getString("browser_proxy", null);
        //浏览器UA设置
        GLOBAL_BROWSER_USERAGENT = configReader.getString("browser_ua", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36");
        //是否清理Cookie
        GLOBAL_CLEAR_COOKIES_SWITCH = configReader.isTrue("clear_cookies", false);
        //浏览器初次打开时配置的Cookies数据
        GLOBAL_BROWSER_INIT_COOKIES = configReader.getString("init_cookies", null);
        //加载账号密码、文件路径
        GLOBAL_USERNAME_FILE = configReader.getString("user_name_file", null);
        GLOBAL_PASSWORD_FILE = configReader.getString("pass_word_file", null);
        //加载账号密码对文件路径
        GLOBAL_USER_PASS_FILE = configReader.getString("user_pass_file", null);
        GLOBAL_PAIR_SEPARATOR = configReader.getString("pair_separator", ":");
        //字典模式选择
        default_dict_compo_mode = configReader.getString("dict_compo_mode", CARTESIAN.name());

        GLOBAL_USER_MARK_IN_PASS = configReader.getString("user_mark_in_pass", "%USER%");
        GLOBAL_EXCLUDE_HISTORY_SWITCH = configReader.isTrue("exclude_history", false);

        //加载默认的登录框配置
        default_login_access_url = configReader.getString("login_access_url", "http://127.0.0.1/demo/index.php/Home/Login/login.html");
        default_login_actual_url = configReader.getString("login_actual_url", null);
        default_login_actual_method = Constant.HttpMethod.fromString(configReader.getString("login_actual_method", POST.name()));

        default_pre_click_ele_value = configReader.getString("pre_click_ele_value", "");
        default_pre_click_ele_type = Constant.EleFindType.fromString(configReader.getString("pre_click_ele_type", XPATH.name()));

        default_name_box_ele_value = configReader.getString("name_box_ele_value", "username");
        default_name_box_ele_type = Constant.EleFindType.fromString(configReader.getString("name_box_ele_type", XPATH.name()));

        default_pass_box_ele_value = configReader.getString("pass_box_ele_value", "password");
        default_pass_box_ele_type = Constant.EleFindType.fromString(configReader.getString("pass_box_ele_type", XPATH.name()));

        default_submit_btn_ele_value = configReader.getString("submit_btn_ele_value", "login");
        default_submit_btn_ele_type = Constant.EleFindType.fromString(configReader.getString("submit_btn_ele_type", XPATH.name()));

        default_show_browser_switch = configReader.isTrue("browser_show", false);

        GLOBAL_LOGIN_PAGE_RELOAD_PER_TIME = configReader.isTrue("login_page_reload_per_time", true);
        GLOBAL_LOGIN_PAGE_LOAD_TIME = Integer.parseInt(configReader.getString("login_page_load_time", "30"));
        GLOBAL_LOGIN_PAGE_LOAD_TIMEOUT_REWORK = configReader.isTrue("login_page_load_timeout_rework", true);

        default_login_page_wait_time = Integer.parseInt(configReader.getString("login_page_wait_time", "1000"));
        default_submit_auto_wait_switch = configReader.isTrue("submit_auto_wait", true);
        default_submit_fixed_wait_time = Integer.parseInt(configReader.getString("submit_fixed_wait_limit", "2000"));
        GLOBAL_SUBMIT_AUTO_WAIT_LIMIT = Integer.parseInt(configReader.getString("submit_auto_wait_limit", "5000"));
        GLOBAL_SUBMIT_AUTO_WAIT_INTERVAL = Integer.parseInt(configReader.getString("submit_auto_wait_interval", "500"));

        default_store_unknown_load_status = configReader.isTrue("store_unknown_load_status", false);

        default_resp_key_success_regex = configReader.getString("success_regex", "welcome");
        default_resp_key_failure_regex = configReader.getString("failure_regex", "登录失败");
        default_resp_key_captcha_regex = configReader.getString("captcha_regex", "验证码错误");

        default_ident_captcha_switch = configReader.isTrue("ident_captcha_switch", false);

        default_captcha_actual_url = configReader.getString("captcha_actual_url", null);
        default_captcha_actual_method = Constant.HttpMethod.fromString(configReader.getString("captcha_actual_method", GET.name()));

        default_captcha_box_ele_value = configReader.getString("captcha_box_ele_value", null);
        default_captcha_box_ele_type = Constant.EleFindType.fromString(configReader.getString("captcha_box_ele_type", XPATH.name()));

        default_ident_time_out = Integer.parseInt(configReader.getString("ident_time_out", "1000"));
        default_ident_format_regex = configReader.getString("ident_format_regex", null);
        default_ident_format_length = configReader.getString("ident_format_length", null);
        default_remote_ident_url = configReader.getString("remote_ident_url", null);
        default_remote_extract_regex = configReader.getString("remote_extract_regex", null);
        default_remote_expected_status = configReader.getString("remote_expected_status", null);
        default_remote_expected_keywords = configReader.getString("remote_expected_keywords", null);

        //指定对应的操作
        GLOBAL_BROWSER_CLOSE_ACTION = Constant.EleFoundStatus.fromString(configReader.getString("browser_close_action", BREAK.name()));
        GLOBAL_FIND_ELE_ILLEGAL_ACTION = Constant.EleFoundStatus.fromString(configReader.getString("find_ele_illegal_action", CONTINUE.name()));
        GLOBAL_FIND_ELE_NULL_ACTION = Constant.EleFoundStatus.fromString(configReader.getString("find_ele_null_action", CONTINUE.name()));
        GLOBAL_FIND_ELE_EXCEPTION_ACTION = Constant.EleFoundStatus.fromString(configReader.getString("find_ele_exception_action", CONTINUE.name()));

        GLOBAL_FIND_ELE_DELAY_TIME = Integer.parseInt(configReader.getString("find_ele_delay_time", "500"));
        GLOBAL_FIND_ELE_RETRY_TIMES = Integer.parseInt(configReader.getString("find_ele_retry_times", "6"));

        default_js_mode_switch = configReader.isTrue("js_mode_switch", false);

        GLOBAL_MATCH_BLOCK_SUFFIX = configReader.getString("match_block_suffix", "js|css|woff|woff2|png|jpg|bpm|mp3|mp44|tff");

        default_match_login_url_switch = configReader.isTrue("match_login_url", false);

        GLOBAL_UNKNOWN_ERROR_NOT_STOP = configReader.isTrue("unknown_error_not_stop", false);

        print_debug("Loaded Config Finish...");
    }

    public static MyConst initialize() {
        // 如果实例为null，则创建一个新的实例
        if (MyConstInstance == null) MyConstInstance = new MyConst();
        return MyConstInstance;
    }

    public static void main(String[] args) {
       MyConst.initialize();
    }

}
