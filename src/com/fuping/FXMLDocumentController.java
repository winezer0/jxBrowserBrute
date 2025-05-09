package com.fuping;

import com.fuping.BrowserUtils.MyDialogHandler;
import com.fuping.CommonUtils.ElementUtils;
import com.fuping.CommonUtils.MyFileUtils;
import com.fuping.CommonUtils.SystemUtilization;
import com.fuping.LoadDict.UserPassPair;
import com.teamdev.jxbrowser.chromium.Callback;
import com.teamdev.jxbrowser.chromium.*;
import com.teamdev.jxbrowser.chromium.dom.By;
import com.teamdev.jxbrowser.chromium.dom.DOMDocument;
import com.teamdev.jxbrowser.chromium.dom.internal.Element;
import com.teamdev.jxbrowser.chromium.events.*;
import com.teamdev.jxbrowser.chromium.javafx.BrowserView;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeoutException;

import static cn.hutool.core.util.StrUtil.isEmptyIfStr;
import static com.fuping.BrowserUtils.BrowserUtils.*;
import static com.fuping.CaptchaIdentify.CaptchaUtils.LoadImageToFile;
import static com.fuping.CaptchaIdentify.RemoteApiIdent.remoteIndentCaptcha;
import static com.fuping.CommonUtils.UiUtils.*;
import static com.fuping.CommonUtils.Utils.*;
import static com.fuping.LoadConfig.Constant.*;
import static com.fuping.LoadConfig.Constant.EleFoundStatus.*;
import static com.fuping.LoadConfig.Constant.LoadingStatus.*;
import static com.fuping.LoadConfig.Constant.LoginStatus.*;
import static com.fuping.LoadConfig.MyConst.*;
import static com.fuping.LoadDict.LoadDictUtils.*;
import static com.fuping.PrintLog.PrintLog.print_debug;

public class FXMLDocumentController implements Initializable {
    @FXML  //识别结果判断条件
    public TextField bro_id_ident_format_length_text;
    @FXML  //识别结果格式筛选
    public TextField bro_id_ident_format_regex_text;
    @FXML  //识别超时配置
    public ComboBox<Integer> bro_id_ident_time_out_combo;
    @FXML //远程识别接口地址
    public TextField bro_id_remote_ident_url_text;
    @FXML  //识别结果判断条件
    public TextField bro_id_remote_resp_is_ok_status_text;
    @FXML  //识别结果判断条件
    public TextField bro_id_remote_resp_is_ok_keywords_text;
    @FXML  //识别结果内容提取
    public TextField bro_id_remote_ident_result_extract_regex_text;

    //登录相关元素
    @FXML
    private TextField id_login_access_url_text;
    @FXML
    private TextField bro_id_login_actual_url_text;

    @FXML
    private TextField bro_id_name_box_ele_text;
    @FXML
    private TextField bro_id_pass_box_ele_text;
    @FXML
    private TextField bro_id_submit_btn_ele_text;
    @FXML
    private ComboBox<String> bro_id_name_box_ele_type_combo;
    @FXML
    private ComboBox<String> bro_id_pass_box_ele_type_combo;
    @FXML
    private ComboBox<String> bro_id_submit_btn_ele_type_combo;
    @FXML
    private TextField bro_id_success_regex_text;
    @FXML
    private TextField bro_id_failure_regex_text;

    @FXML
    private ComboBox<String> bro_id_pre_click_ele_type_combo;
    @FXML
    private TextField bro_id_pre_click_ele_text;

    //浏览器设置相关
    @FXML
    private ComboBox<Integer> bro_id_login_page_wait_time_combo;
    @FXML
    private ComboBox<Integer> bro_id_submit_fixed_wait_time_combo;
    @FXML
    public CheckBox bro_id_submit_auto_wait_check;

    @FXML
    public CheckBox bro_id_js_mode_check;
    @FXML
    public CheckBox bro_id_match_login_url_check;

    @FXML //设置字典组合模式
    private ComboBox<String> bro_id_dict_compo_mode_combo;
    @FXML
    private CheckBox bro_id_show_browser_check;
    @FXML
    private CheckBox bro_id_exclude_history_check;

    //验证码元素相关
    @FXML
    public VBox bro_id_captcha_set_vbox;
    @FXML
    private CheckBox bro_id_ident_captcha_switch_check;
    @FXML
    private CheckBox bro_id_store_unknown_status_check;
    @FXML
    private CheckBox bro_id_use_browser_proxy;
    @FXML
    private TextField bro_id_captcha_actual_url_text;
    @FXML
    private ComboBox<String> bro_id_captcha_box_ele_type_combo;
    @FXML
    private TextField bro_id_captcha_box_ele_text;
    @FXML
    private TextField bro_id_captcha_regex_text;

    @FXML
    private ComboBox<String> bro_id_login_actual_method_combo; //限定登录包的请求方法
    @FXML
    private ComboBox<String> bro_id_captcha_actual_method_combo; //限定验证码包的请求方法

    //输出相关
    @FXML
    private TextArea bro_id_output_text_area; // 注意：这里不应该使用static，除非有特殊需求

    private Stage primaryStage;

    public static byte[] captchaPictureData; //存储验证码数据
    public static String CURR_LOADING_STATUS; //记录当前页面加载状态
    public static String CURR_LOGIN_STATUS; //记录当前登录爆破加载状态
    public static boolean Trigger_Redirection=false; //爆破过程中触发重定向

    private List<String> login_about_urls = null;  //存储当前URL相关的多个URl
    private String login_access_url = null;  //设置当前登录url的全局变量用于后续调用
    private String login_actual_url = null;  //设置当前登录HTTP报文的URL地址用于后续调用 登录的实际URL
    private HttpMethod login_actual_method = null;  //设置当前登录HTTP报文的请求方法

    private String captcha_actual_url = null;  //设置当前登录验证码url用于后续调用
    private HttpMethod captcha_actual_method = null;  //设置当前登录验证码的请求方法

    private boolean captcha_ident_was_error = false; //设置当前验证码识别错误状态

    private Browser browser = null;

    private String login_url_http_proto = null; //记录当前登录URL的协议类型,用于后续http/https的纠正使用

    //元素查找方法
    private boolean executeJavaScriptMode = false;

    //识别验证码的函数, 便于合并
    private String identCaptcha(String imagePath, byte[] imageBytes){
        //获取验证码筛选条件
        String ident_format_length = this.bro_id_ident_format_length_text.getText(); //期望长度
        String ident_format_regex = this.bro_id_ident_format_regex_text.getText(); //期望格式
        Integer ident_time_out = this.bro_id_ident_time_out_combo.getValue();

        //记录验证码识别结果
        String captcha_indent_text = null;
        //获取是被接口信息接口URL
        String remote_ident_url_text = this.bro_id_remote_ident_url_text.getText().trim();
        if (isEmptyIfStr(remote_ident_url_text)){
            this.bro_id_remote_ident_url_text.requestFocus();
            return null;
        }
        //获取响应判断条件
        String remote_resp_is_ok_status = this.bro_id_remote_resp_is_ok_status_text.getText(); //期望的 远程OCR接口  正常响应状态码 如 200
        String remote_resp_is_ok_keywords = this.bro_id_remote_resp_is_ok_keywords_text.getText(); //期望的 远程OCR接口 正常响应关键字 默认空
        //响应结果正则提取
        String ident_result_extract_regex = this.bro_id_remote_ident_result_extract_regex_text.getText(); //从响应中提取验证码结果的正则 默认获取全部

        try {
            //识别图片地址
            if (ElementUtils.isNotEmptyObj(imagePath)){
                captcha_indent_text = remoteIndentCaptcha(
                        imagePath,
                        remote_ident_url_text,
                        remote_resp_is_ok_status,
                        remote_resp_is_ok_keywords,
                        ident_result_extract_regex,
                        ident_format_regex,
                        ident_format_length,
                        ident_time_out
                );
            } else {
                captcha_indent_text = remoteIndentCaptcha(
                        imageBytes,
                        remote_ident_url_text,
                        remote_resp_is_ok_status,
                        remote_resp_is_ok_keywords,
                        ident_result_extract_regex,
                        ident_format_regex,
                        ident_format_length,
                        ident_time_out
                );
            }
            printlnInfoOnUIAndConsole(String.format("验证码 远程识别结果: [%s]", captcha_indent_text));
        }catch (Exception e){
            printlnErrorOnUIAndConsole(String.format("验证码 远程识别错误: [%s]", e.getMessage()));
            e.printStackTrace();
        }
        return captcha_indent_text;
    }

    //浏览器动作配置
    private Browser initJxBrowserInstance() {
        //创建窗口对象 JavaFX的Stage类是JavaFX应用程序创建窗口的基础
        this.primaryStage = new Stage();

        //在创建任何浏览器实例之前，只能修改一次用户代理字符串。
        // 可以使用BrowserPreferences.setUserAgent（String userAgent）方法
        // 或通过 jxbrowser.chromium.user-agent Java System属性提供用户代理字符串：
        BrowserPreferences.setUserAgent(GLOBAL_BROWSER_USERAGENT);

        //创建浏览器对象 轻量级对象 BrowserType.LIGHTWEIGHT 轻量级渲染模式， BrowserType.HEAVYWEIGHT重量级渲染模式。
        //轻量级渲染模式是通过CPU来加速渲染的，速度更快，占用更少的内存。在轻量级渲染模式下，Chromium引擎会在后台使用CPU渲染网页，然后将网页的图像保存在共享内存中。
        //重量级渲染模式则使用GPU加速渲染，相对于轻量级模式来说，它需要占用更多的内存，但在某些场景下可能会有更好的性能和更高的渲染质量。
        Browser browser = new Browser(BrowserType.LIGHTWEIGHT);

        ////浏览器首选项设置
        //BrowserPreferences preferences = browser.getPreferences();
        //preferences.setImagesEnabled(false);
        //preferences.setJavaScriptEnabled(false);
        //browser.setPreferences(preferences);

        //着浏览器视图将占据布局容器的中心区域，并自动适应大小。
        BrowserView view = new BrowserView(browser); //将 JxBrowser 的浏览器引擎 browser 嵌入到 JavaFX 中
        BorderPane borderPane = new BorderPane(view); //着浏览器视图将占据布局容器的中心区域，并自动适应大小。

        //创建了一个进度指示器，该指示器可以用于显示任务的进度状态，并通过设置首选高度和宽度来调整其在界面中的大小
        ProgressIndicator progressIndicator = new ProgressIndicator(1.0D);
        progressIndicator.setPrefHeight(30.0D);
        progressIndicator.setPrefWidth(30.0D);

        //创建一个文本输入框 用于输入URL的输入框，用户可以在其中键入网址。
        TextField url_input = new TextField();
        //进度指示器  和 文本输入框 。这两个子节点之间的水平间隔为 2.0D。
        HBox Hbox = new HBox(2.0D, new Node[]{progressIndicator, url_input});
        //文本输入框将始终尝试占据额外的水平空间，以充分利用可用的宽度
        HBox.setHgrow(url_input, Priority.ALWAYS);
        //子节点的水平对齐方式被设置为 Pos.CENTER_LEFT，这表示子节点在容器内水平居左对齐。
        Hbox.setAlignment(Pos.CENTER_LEFT);
        //将 进度指示器和URL输入框 放在 用户界面的顶部部分
        borderPane.setTop(Hbox);

        //创建了一个场景（Scene）对象 宽度为 800.0 像素，高度为 700.0 像素
        Scene scene = new Scene(borderPane, 800.0D, 700.0D);
        //primaryStage 是 JavaFX 应用程序的顶层窗口，指定要在主窗口中显示的用户界面内容。
        this.primaryStage.setScene(scene);
        //设置了主舞台的标题，标题将显示在窗口的标题栏上。
        this.primaryStage.setTitle("请勿操作浏览器页面");

        //是否显示浏览器框//应该放在后面处理
        if (this.bro_id_show_browser_check.isSelected()) { this.primaryStage.show(); }

        //在浏览器中处理弹出窗口的情况，将弹出窗口的内容加载到父级容器中。这对于控制浏览器行为或执行自动化测试时可能会很有用。
        PopupHandler popupHandler = new PopupHandler() {
            public PopupContainer handlePopup(PopupParams paramPopupParams) {
                paramPopupParams.getParent().loadURL(paramPopupParams.getURL());
                return null;
            }
        };
        browser.setPopupHandler(popupHandler);

        //确保在关闭应用程序时释放浏览器资源，并设置了自定义的对话框处理程序，以控制网页中对话框的行为。
        this.primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent event) {
                browser.dispose();
            }
        });
        browser.setDialogHandler(new MyDialogHandler(view));

        //添加页面加载监听事件
        browser.addLoadListener(new LoadAdapter() {
            @Override
            //预加载触发事件||表示加载即将开始
            public void onProvisionalLoadingFrame(ProvisionalLoadingEvent event) {
                //页面首次加载|| 页面中的iframe或其他嵌入式框架开始加载新内容||用户点击链接、提交表单等操作导致页面或其部分重新加载。
                if (event.isMainFrame())
                    Platform.runLater(new Runnable() {
                        public void run() {
                            url_input.setText(event.getURL());
                        }
                    });
            }

            @Override
            //加载中触发事件||在实际接收到数据时触发
            public void onStartLoadingFrame(StartLoadingEvent event) {
                //在浏览器开始接收到框架的数据时被调用，意味着网络连接已经建立，并且服务器已经开始发送响应数据。
                if (event.isMainFrame()){
                    Platform.runLater(new Runnable() {
                        public void run() {
                            //输出加载中记录输出两次 UI重复,不是错误,是浏览器实际进行了主页和登录请求两次
                            String validatedURL = event.getValidatedURL();
                            CURR_LOADING_STATUS = String.format("%s<->%s", LOADING_START, validatedURL);
                            printlnDebugOnUIAndConsole(String.format("Loading Status: %s %s", LOADING_START, validatedURL));
                            progressIndicator.setProgress(-1.0D);
                        }
                    });
                }
                super.onStartLoadingFrame(event);
            }

            @Override
            //加载失败触发事件
            public void onFailLoadingFrame(FailLoadingEvent event) {
                //尝试加载某个框架的内容但未能成功时
                if (event.isMainFrame())
                    Platform.runLater(new Runnable() {
                        public void run() {
                            String validatedURL = event.getValidatedURL();
                            CURR_LOADING_STATUS = String.format("%s<->%s", LOADING_FAILED, validatedURL);
                            printlnErrorOnUIAndConsole(String.format("Loading Status: %s %s", LOADING_FAILED, validatedURL));
                            progressIndicator.setProgress(1.0D);
                        }
                    });
                super.onFailLoadingFrame(event);
            }

            @Override
            //加载完成触发事件
            public void onFinishLoadingFrame(FinishLoadingEvent event) {
                //当浏览器成功加载完某个框架的所有内容时，包括HTML、CSS、JavaScript等资源时触发
                if (event.isMainFrame()) {
                    Platform.runLater(new Runnable() {
                        public void run() {
                            String validatedURL = event.getValidatedURL();
                            CURR_LOADING_STATUS = String.format("%s<->%s", LOADING_FINISH, validatedURL);
                            printlnDebugOnUIAndConsole(String.format("Loading Status: %s %s", LOADING_FINISH, validatedURL));
                            progressIndicator.setProgress(1.0D);
                        }
                    });
                }
                super.onFinishLoadingFrame(event);
            }

            @Override
            //主框架和子框架加载完成调用事件
            public void onDocumentLoadedInFrame(FrameLoadEvent event) {
                //在页面中任何框架（包括主框架和嵌套的子框架如 iframe）的 DOM 文档加载完成后被调用。
                //printlnInfoOnUIAndConsole("Frame document is loaded.");
            }

            @Override
            //主框架加载完成调用事件
            public void onDocumentLoadedInMainFrame(LoadEvent event) {
                //仅在主框架（即整个页面）的DOM文档加载完成后被调用，而不考虑其他嵌套的子框架。
                //printlnInfoOnUIAndConsole("Main frame document is loaded.");
            }

        });

        //addStatusListener状态监听器 允许开发者监听浏览器的各种状态变化、可以捕获到诸如导航开始、导航完成、加载进度更新等事件，
        //这对于实现自定义的用户界面反馈（如显示加载进度条）或执行特定逻辑（如在页面加载完成后执行某些操作）非常有用。
        //addStatusListener状态监听器 // 没有获取到任何数据 //放弃使用//

        return browser;
    }

    //定义是否停止所有爆破动作
    private boolean stopCrackStatus = false;
    @FXML //停止爆破动作
    public void stopCrack(ActionEvent actionEvent) {
        stopCrackStatus=true;
        printlnInfoOnUIAndConsole("已点击停止按钮,请等待停止信号传递...");
    }

    //修改浏览器代理配置
    @FXML
    public void change_browser_proxy_action(ActionEvent actionEvent) {
        printlnInfoOnUIAndConsole("已点击修改浏览器代理配置,请等待修改信号传递...");
        setBrowserProxyMode(browser, this.bro_id_use_browser_proxy.isSelected(), GLOBAL_BROWSER_PROXY_STR, login_url_http_proto);
    }

    @FXML
    public void change_js_mode_action(ActionEvent actionEvent) {
        printlnInfoOnUIAndConsole("已点击修改元素查找模式,请等待修改信号传递...");
        executeJavaScriptMode = this.bro_id_js_mode_check.isSelected();
    }

    @FXML
    public void change_match_login_url_action(ActionEvent actionEvent) {
        printlnInfoOnUIAndConsole("已点击修改响应匹配模式,请等待修改信号传递...");
        //设置JxBrowser中网络委托的对象，以实现对浏览器的网络请求和响应的控制和处理。 //更详细的请求和响应处理,含保存验证码图片
        browser.getContext().getNetworkService().setNetworkDelegate(
                new MyNetworkDelegate(
                        this.captcha_actual_url,
                        this.captcha_actual_method,
                        this.login_actual_url,
                        this.login_actual_method,
                        this.bro_id_match_login_url_check.isSelected(),
                        this.bro_id_captcha_regex_text.getText(),
                        this.bro_id_failure_regex_text.getText(),
                        this.bro_id_success_regex_text.getText()
                ));
    }

    @FXML
    public void change_submit_auto_wait_action(ActionEvent actionEvent) {
        //点击了自动等待就关闭手动设置等待的功能
        this.bro_id_submit_fixed_wait_time_combo.setDisable(bro_id_submit_auto_wait_check.isSelected());
    }


    // 获取控制器实例的方法
    private static FXMLDocumentController fxmlInstance;
    public static synchronized FXMLDocumentController getInstance() {
        if (fxmlInstance == null)
            fxmlInstance = new FXMLDocumentController();
        return fxmlInstance;
    }

    // 私有化构造函数
    private FXMLDocumentController() {
    }

    @Override //UI加载时的初始化操作
    public void initialize(URL url, ResourceBundle rb) {
        // 确保只初始化一次
        if (fxmlInstance == null) {
            fxmlInstance = this;
        }

        //通过代码修改复选框设置参考，已修改到FXML文件里面配置
        //ObservableList threads = FXCollections.observableArrayList(new Integer[]{Integer.valueOf(1), Integer.valueOf(3), Integer.valueOf(5), Integer.valueOf(10), Integer.valueOf(20), Integer.valueOf(30), Integer.valueOf(50), Integer.valueOf(60), Integer.valueOf(70), Integer.valueOf(80), Integer.valueOf(90), Integer.valueOf(100)});
        //this.nor_id_threads.setItems(threads);

        //初始化窗口1的内容设置
        //设置登录URL
        setWithCheck(this.id_login_access_url_text, default_login_access_url);

        //设置登录包URL框
        setWithCheck(this.bro_id_login_actual_url_text, default_login_actual_url);
        this.bro_id_login_actual_url_text.setTooltip(new Tooltip("登录包的实际请求URL"));

        setWithCheck(this.bro_id_login_actual_method_combo, default_login_actual_method.name());
        this.bro_id_login_actual_method_combo.setTooltip(new Tooltip("登录包的实际请求方法"));

        //设置登录框
        setWithCheck(this.bro_id_pre_click_ele_text, default_pre_click_ele_value);
        this.bro_id_pre_click_ele_text.setTooltip(new Tooltip("预点击元素定位方式和对应值"));
        setWithCheck(this.bro_id_pre_click_ele_type_combo, default_pre_click_ele_type.name());
        this.bro_id_pre_click_ele_type_combo.setTooltip(new Tooltip("预点击元素定位方式和对应值"));

        //设置登录框
        setWithCheck(this.bro_id_name_box_ele_text, default_name_box_ele_value);
        this.bro_id_name_box_ele_text.setTooltip(new Tooltip("账号框元素定位方式和对应值"));
        setWithCheck(this.bro_id_name_box_ele_type_combo, default_name_box_ele_type.name());
        this.bro_id_name_box_ele_type_combo.setTooltip(new Tooltip("账号框元素定位方式和对应值"));

        //设置密码框
        setWithCheck(this.bro_id_pass_box_ele_text, default_pass_box_ele_value);
        this.bro_id_pass_box_ele_text.setTooltip(new Tooltip("密码框元素定位方式和对应值"));
        setWithCheck(this.bro_id_pass_box_ele_type_combo, default_pass_box_ele_type.name());
        this.bro_id_pass_box_ele_type_combo.setTooltip(new Tooltip("密码框元素定位方式和对应值"));

        //设置提交按钮
        setWithCheck(this.bro_id_submit_btn_ele_text, default_submit_btn_ele_value);
        this.bro_id_submit_btn_ele_text.setTooltip(new Tooltip("提交按钮元素定位方式和对应值"));
        setWithCheck(this.bro_id_submit_btn_ele_type_combo, default_submit_btn_ele_type.name());
        this.bro_id_submit_btn_ele_type_combo.setTooltip(new Tooltip("提交按钮元素定位方式和对应值"));

        //设置浏览器选项
        setWithCheck(this.bro_id_show_browser_check, default_show_browser_switch);
        this.bro_id_show_browser_check.setTooltip(new Tooltip("显示浏览器到窗口"));

        setWithCheck(this.bro_id_exclude_history_check, GLOBAL_EXCLUDE_HISTORY_SWITCH);
        this.bro_id_exclude_history_check.setTooltip(new Tooltip("排除已测试的历史账号密码"));

        //设置是否保存默认状态
        setWithCheck(this.bro_id_store_unknown_status_check, default_store_unknown_load_status);
        this.bro_id_store_unknown_status_check.setTooltip(new Tooltip("保存响应状态未知（程序无法确定登录结果）的结果"));

        //设置是否使用浏览器代理
        setWithCheck(this.bro_id_use_browser_proxy, true);
        this.bro_id_use_browser_proxy.setTooltip(new Tooltip("启用浏览器代理用于访问调试"));

        setWithCheck(this.bro_id_login_page_wait_time_combo, default_login_page_wait_time);
        this.bro_id_login_page_wait_time_combo.setTooltip(new Tooltip("请求间隔时间（毫秒）"));

        setWithCheck(this.bro_id_submit_fixed_wait_time_combo, default_submit_fixed_wait_time);
        this.bro_id_submit_fixed_wait_time_combo.setTooltip(new Tooltip("提交等待时间（毫秒）"));

        setWithCheck(this.bro_id_submit_auto_wait_check, default_submit_auto_wait_switch);
        this.bro_id_submit_auto_wait_check.setTooltip(new Tooltip("自动等待页面加载完成"));
        this.change_submit_auto_wait_action(null);

        setWithCheck(this.bro_id_js_mode_check, default_js_mode_switch);
        this.bro_id_js_mode_check.setTooltip(new Tooltip("使用JS执行模式进行元素查找和输入 仅实现XPATH和CSS元素选择器"));

        setWithCheck(this.bro_id_match_login_url_check, default_match_login_url_switch);
        this.bro_id_match_login_url_check.setTooltip(new Tooltip("是否仅对请求包的URL进行响应数据提取 需要输入请求包对应的URL"));

        setWithCheck(this.bro_id_dict_compo_mode_combo, default_dict_compo_mode);
        this.bro_id_dict_compo_mode_combo.setTooltip(new Tooltip("字典组合方式"));

        //设置关键字匹配
        setWithCheck(this.bro_id_success_regex_text, default_resp_key_success_regex);
        setWithCheck(this.bro_id_failure_regex_text, default_resp_key_failure_regex);
        setWithCheck(this.bro_id_captcha_regex_text, default_resp_key_captcha_regex);
        //设置验证码识别开关
        setWithCheck(this.bro_id_ident_captcha_switch_check, default_ident_captcha_switch);
        this.bro_id_ident_captcha_switch_check.setTooltip(new Tooltip("开启验证码识别功能"));
        //设置验证码属性
        setWithCheck(this.bro_id_captcha_actual_url_text, default_captcha_actual_url);
        this.bro_id_captcha_actual_url_text.setTooltip(new Tooltip("验证码请求包的实际请求URL"));
        setWithCheck(this.bro_id_captcha_actual_method_combo, default_captcha_actual_method.name());
        this.bro_id_captcha_actual_method_combo.setTooltip(new Tooltip("验证码请求包的实际请求方法"));


        setWithCheck(this.bro_id_captcha_box_ele_text, default_captcha_box_ele_value);
        setWithCheck(this.bro_id_captcha_box_ele_type_combo, default_captcha_box_ele_type.name());
        this.bro_id_captcha_box_ele_text.setTooltip(new Tooltip("验证码输入框元素定位方式和对应值"));
        this.bro_id_captcha_box_ele_type_combo.setTooltip(new Tooltip("验证码输入框元素定位方式和对应值"));

        //设置验证码配置细节
        setWithCheck(this.bro_id_ident_time_out_combo, default_ident_time_out);
        this.bro_id_ident_time_out_combo.setTooltip(new Tooltip("验证码识别超时（毫秒）"));

        setWithCheck(this.bro_id_ident_format_regex_text, default_ident_format_regex);
        setWithCheck(this.bro_id_ident_format_length_text, default_ident_format_length);
        setWithCheck(this.bro_id_remote_ident_url_text, default_remote_ident_url);
        setWithCheck(this.bro_id_remote_ident_result_extract_regex_text, default_remote_extract_regex);
        setWithCheck(this.bro_id_remote_resp_is_ok_status_text, default_remote_expected_status);
        setWithCheck(this.bro_id_remote_resp_is_ok_keywords_text, default_remote_expected_keywords);

        //模拟禁用动作
        this.captcha_identify_action(null);

    }

    @FXML  //点击 验证码识别开关需要 禁用|开启 的按钮
    private void captcha_identify_action(ActionEvent event) {
        this.bro_id_captcha_set_vbox.setDisable(!this.bro_id_ident_captcha_switch_check.isSelected());
    }


    @FXML //测试验证码识别功能是否正常
    public void bro_id_remote_ident_test_run(ActionEvent event) {
        //获取验证码输入URL的内容
        String bro_captcha_url_text = this.bro_id_captcha_actual_url_text.getText().trim();
        if (isEmptyIfStr(bro_captcha_url_text)) {
            this.bro_id_captcha_actual_url_text.requestFocus();
            return;
        }

        //另外开一个线程处理,以免UI卡顿
        new Thread(new Runnable() {
            public void run() {
                String imagePath = LoadImageToFile(bro_captcha_url_text, "TestRemote.jpg");
                printlnDebugOnUIAndConsole(String.format("Stored Image [%s] To [%s]", bro_captcha_url_text, imagePath));
                identCaptcha(imagePath, null);
            }
        }).start();
    }

    @FXML  //浏览器窗口显示设置,不需要管理
    private void show_browser_action(ActionEvent event) {
        if (this.primaryStage == null) {
            return;
        }
        if (this.bro_id_show_browser_check.isSelected())
            this.primaryStage.show();
        else
            this.primaryStage.hide();
    }

    public void appendTextToTextArea(String appendText) {
        if (bro_id_output_text_area != null && appendText != null) {
            Platform.runLater(() -> bro_id_output_text_area.appendText(String.format("%s", appendText)));
        }
    }

    //主要爆破函数的修改
    @FXML
    private void startCrack(ActionEvent actionEvent) {
        //初始化停止爆破状态为 false,以免上次的状态影响本次的操作
        stopCrackStatus = false;

        Platform.runLater(() -> {
            if(stopCrackStatus){
                return;
            }

            //读取登录 URL
            String login_url_text = this.id_login_access_url_text.getText().trim();

            //登陆 URL 检查
            if (isEmptyIfStr(login_url_text) || !login_url_text.startsWith("http")) {
                new Alert(Alert.AlertType.NONE, "请输入完整的登录页面URL", new ButtonType[]{ButtonType.CLOSE}).show();
                return;
            } else {
                //支持在登录URL处输入多个URL,第一个URL用于登录访问,其他URL用于判断
                if (login_url_text.contains("|")){
                    login_about_urls = splitAndFilter(login_url_text, "\\|") ;
                } else{
                    login_about_urls =  new ArrayList<>(Collections.singletonList(login_url_text));
                }
                //把登录相关URl的第一个URL作为访问URL
                login_access_url = login_about_urls.get(0);

                //获取指定登录包相关的URL、不指定也能用
                if (!isEmptyIfStr(this.bro_id_login_actual_url_text.getText().trim())){
                    this.login_actual_url = this.bro_id_login_actual_url_text.getText().trim();
                    this.login_actual_method = HttpMethod.fromString(this.bro_id_login_actual_method_combo.getValue());
                    this.login_about_urls.add(this.login_actual_url); //把登录包URL也加入相关URL中, 可能不需要
                }

                printlnDebugOnUIAndConsole(String.format("指定登录访问URL:[%s] 登录包URL为:[%s:%s] 登录相关URL为:[%s]",
                        this.login_access_url, this.login_actual_method, this.login_actual_url, login_url_text));
            }

            //基于登录URL初始化|URL更新|日志文件配置
            MyFileUtils.initBaseOnLoginUrlFile(login_access_url);

            //检查是否存在关键按钮信息修改,(都需要更新到全局变量做记录),并且重新更新加载字典
            boolean isModifiedAuthFile = MyFileUtils.isModifiedAuthFile(); //字典文件是否修改
            //print_info(String.format("isModifiedAuthFile %s", isModifiedAuthFile));

            boolean isModifiedLoginUrl = MyFileUtils.isModifiedLoginUrl(login_access_url); //登录URL是否修改
            //print_info(String.format("isModifiedLoginUrl %s", isModifiedLoginUrl));

            boolean isModifiedDictMode = MyFileUtils.isModifiedDictMode(this.bro_id_dict_compo_mode_combo.getValue()); //字典模式是否修改
            //print_info(String.format("isModifiedDictMode %s", isModifiedDictMode));

            boolean isModifiedExcludeHistory = MyFileUtils.isModifiedExcludeHistory(this.bro_id_exclude_history_check.isSelected());//排除历史状态是否修改
            //print_info(String.format("isModifiedExcludeHistory %s", isModifiedExcludeHistory));

            if(GLOBAL_EXCLUDE_HISTORY_SWITCH ||isModifiedAuthFile||isModifiedLoginUrl||isModifiedDictMode||isModifiedExcludeHistory){
                //当登录URL或账号密码文件修改后,就需要重新更新
                printlnDebugOnUIAndConsole("加载账号密码文件开始...");
                //点击登录后加载字典文件
                HashSet<UserPassPair> UserPassPairsHashSet = loadUserPassFile(GLOBAL_USERNAME_FILE, GLOBAL_PASSWORD_FILE, GLOBAL_USER_PASS_FILE, GLOBAL_PAIR_SEPARATOR, DictMode.fromString(default_dict_compo_mode));

                //替换密码中的用户名变量
                UserPassPairsHashSet = replaceUserMarkInPass(UserPassPairsHashSet, GLOBAL_USER_MARK_IN_PASS);
                print_debug(String.format("Pairs Count After Replace Mark Str [%s]", UserPassPairsHashSet.size()));

                //读取 history 文件,排除历史扫描记录 ，
                if (GLOBAL_EXCLUDE_HISTORY_SWITCH) {
                    UserPassPairsHashSet = excludeHistoryPairs(UserPassPairsHashSet, globalCrackHistoryFilePath, GLOBAL_PAIR_SEPARATOR);
                    print_debug(String.format("Pairs Count After Exclude History [%s] From [%s]", UserPassPairsHashSet.size(), globalCrackHistoryFilePath));
                }

                //将账号密码字典格式从 HashSet 转为 数组,便于索引统计
                globalUserPassPairsArray = UserPassPairsHashSet.toArray(new UserPassPair[0]);
            }

            //判断字典列表数量是否大于0
            if(globalUserPassPairsArray.length > 0){
                printlnDebugOnUIAndConsole(String.format("加载账号密码文件完成 当前账号:密码数量[%s], 开始爆破操作...", globalUserPassPairsArray.length));
            } else {
                printlnErrorOnUIAndConsole(String.format("加载账号密码文件完成 当前账号:密码数量[%s], 跳过爆破操作...", globalUserPassPairsArray.length));
                return;
            }


            //获取用户名框框的内容
            String bro_name_box_ele_text = this.bro_id_name_box_ele_text.getText().trim();
            EleFindType bro_name_box_ele_type = EleFindType.fromString(this.bro_id_name_box_ele_type_combo.getValue());
            if (isEmptyIfStr(bro_name_box_ele_text)) { this.bro_id_name_box_ele_text.requestFocus(); return;}

            //获取密码框元素的内容
            String bro_pass_box_ele_text = this.bro_id_pass_box_ele_text.getText().trim();
            EleFindType bro_pass_box_ele_type = EleFindType.fromString(this.bro_id_pass_box_ele_type_combo.getValue());
            if (isEmptyIfStr(bro_pass_box_ele_text)) { this.bro_id_pass_box_ele_text.requestFocus(); return;}

            //登录按钮内容
            String bro_submit_btn_ele_text = this.bro_id_submit_btn_ele_text.getText().trim();
            EleFindType bro_id_submit_btn_ele_type = EleFindType.fromString(this.bro_id_submit_btn_ele_type_combo.getValue());
            if (isEmptyIfStr(bro_submit_btn_ele_text)) { this.bro_id_submit_btn_ele_text.requestFocus(); return;}

            //预点击按钮元素 可以为空
            String bro_pre_click_ele_text = this.bro_id_pre_click_ele_text.getText().trim();
            EleFindType bro_pre_click_ele_type = EleFindType.fromString(this.bro_id_pre_click_ele_type_combo.getValue());

            //检查验证码输入URL的内容
            if (this.bro_id_ident_captcha_switch_check.isSelected()) {
                //获取验证码URL
                this.captcha_actual_url = this.bro_id_captcha_actual_url_text.getText().trim();
                this.captcha_actual_method = HttpMethod.fromString(this.bro_id_captcha_actual_method_combo.getValue());
                if (isEmptyIfStr(this.captcha_actual_url)){
                    this.bro_id_captcha_actual_url_text.requestFocus();
                    return;
                }
            }

            //初始化浏览器 //尝试将 Browser 设置为全局时,将导致无法停止
            browser = initJxBrowserInstance();

            //浏览器代理设置
            login_url_http_proto = login_access_url.toLowerCase().startsWith("http://") ? "http" : "https";
            setBrowserProxyMode(browser, this.bro_id_use_browser_proxy.isSelected(), GLOBAL_BROWSER_PROXY_STR, login_url_http_proto);

            //设置JxBrowser中网络委托的对象，以实现对浏览器的网络请求和响应的控制和处理。 //更详细的请求和响应处理,含保存验证码图片
            browser.getContext().getNetworkService().setNetworkDelegate(
                    new MyNetworkDelegate(
                            this.captcha_actual_url,
                            this.captcha_actual_method,
                            this.login_actual_url,
                            this.login_actual_method,
                            this.bro_id_match_login_url_check.isSelected(),
                            this.bro_id_captcha_regex_text.getText(),
                            this.bro_id_failure_regex_text.getText(),
                            this.bro_id_success_regex_text.getText()
                    ));

            //初始化获取当前的元素选择模式
            executeJavaScriptMode = this.bro_id_js_mode_check.isSelected();

            //开启一个新的线程进行爆破操作
            new Thread(new Runnable() {
                public void run() {
                    try {
                        //记录是否发生超时错误,是的话后续就需要重新访问页面了 用于在设置了每次不自动访问登录页的场景下
                        boolean occurAccessUrlError = false;
                        //记录本次循环的错误次数
                        Integer localEleErrorCounts = 0;
                        //提交等待时间 由于需要动态变化 因此需要移动到循环外面
                        Integer bro_submit_fixed_wait_time = fxmlInstance.bro_id_submit_fixed_wait_time_combo.getValue();
                        //遍历账号密码字典
                        for (int index = 0; index < globalUserPassPairsArray.length;) {
                            // 记录程序开始时间
                            long startTime = System.currentTimeMillis();

                            UserPassPair userPassPair = globalUserPassPairsArray[index];
                            String cur_user = userPassPair.getUsername();
                            String cur_pass = userPassPair.getPassword();

                            //判断停止按钮是否点击,是的话就跳出循环
                            if(stopCrackStatus) {
                                printlnErrorOnUIAndConsole("发现已点击停止按钮, 停止爆破动作");
                                break;
                            }

                            //重复次数过多时应该停止、重新打开浏览器
                            if (localEleErrorCounts > 10) {
                                throw new TimeoutException("Too many errors: " + localEleErrorCounts);
                            }

                            //假设本次爆破发生错误
                            localEleErrorCounts++;

                            //输出当前即将测试的数据
                            printlnInfoOnUIAndConsole(String.format("当前进度 [%s/%s] <--> [%s] [%s]", index+1, globalUserPassPairsArray.length, userPassPair, login_access_url));

                            //请求间隔设置
                            Integer bro_login_page_wait_time = fxmlInstance.bro_id_login_page_wait_time_combo.getValue();

                            //设置初始化Cookies字符串 用于满足Cookie不存在时不能直接访问登录页面的情况
                            if (index == 0 && GLOBAL_BROWSER_INIT_COOKIES != null && !GLOBAL_BROWSER_INIT_COOKIES.trim().isEmpty()){
                                setBrowserCookies(browser, login_access_url, GLOBAL_BROWSER_INIT_COOKIES);
                            }

                            //清理所有Cookie //可能存在问题,比如验证码, 没有Cookie会怎么样呢?
                            if (index>0 && GLOBAL_CLEAR_COOKIES_SWITCH){clearCookieStorage(browser); }

                            //清空上一次记录的的验证码数据 //在这里清空的话会导致没有加载页面的时候验证码图片没有值
                            //instance.captcha_data = null;

                            //加载登录URL
                            //判断当前页面是不是登录页面 当前页面不是登录页[或登录相关]时重新加载登录页面
                            //当用户指定了 global_login_page_reload_per_time 时，重新加载页面
                            //当验证码是错误的时候也需要重新加载页面，不然总是重新识别不出来,死循环
                            if(GLOBAL_LOGIN_PAGE_RELOAD_PER_TIME
                                    || occurAccessUrlError
                                    || !ElementUtils.isEqualsOneKey(browser.getURL(),login_about_urls)
                                    || captcha_ident_was_error)
                            {
                                try {
                                    occurAccessUrlError = false; // 已经重新访问URL就重置
                                    printlnDebugOnUIAndConsole("等待加载登录页面 By global_login_page_reload_per_time || !base_login_url.equals(browser.getURL()) || captcha_ident_was_error");
                                    Browser.invokeAndWaitFinishLoadingMainFrame(browser, new Callback<Browser>() {
                                        public void invoke(Browser browser) {
                                            browser.loadURL(login_access_url);
                                        }
                                    }, GLOBAL_LOGIN_PAGE_LOAD_TIME);
                                } catch (IllegalStateException illegalStateException) {
                                    occurAccessUrlError = true; //发生异常就设置要求重新访问页面
                                    String illegalStateExceptionMessage = illegalStateException.getMessage();
                                    //状态异常是否重新开始
                                    if (illegalStateExceptionMessage.contains("Channel is already closed")) {
                                        printlnErrorOnUIAndConsole("停止测试, 请点击按钮重新开始");
                                        break;
                                    } else {
                                        printlnErrorOnUIAndConsole(String.format("重新测试, %s", illegalStateExceptionMessage));
                                        continue;
                                    }

                                } catch (Exception e) {
                                    occurAccessUrlError = true; //发生异常就设置要求重新访问页面
                                    e.printStackTrace();
                                    //登录页面加载超时是否重头再来
                                    if(GLOBAL_LOGIN_PAGE_LOAD_TIMEOUT_REWORK) {
                                        printlnErrorOnUIAndConsole(String.format("访问超时-重新测试, 请检查网络设置状态:%s", e.getMessage()));
                                        continue;
                                    } else {
                                        printlnErrorOnUIAndConsole(String.format("访问超时-停止测试, 请检查网络设置状态:%s", e.getMessage()));
                                        break;
                                    }
                                }

                                //进行线程延迟 //等待页面加载完毕//原则上是可以不需要的
                                Thread.sleep(bro_login_page_wait_time);
                            }else {
                                //进行线程延迟
                                Thread.sleep(bro_login_page_wait_time + 1000);
                            }

                            //加载URl文档
                            DOMDocument document = browser.getDocument();

                            //进行预点击操作 //不进行错误处理
                            if (bro_pre_click_ele_text.length()>0){
                                String[] pre_click_ele_text_list = bro_pre_click_ele_text.split("&&");
                                //支持多个预点击操作
                                for (String ele_text : pre_click_ele_text_list){
                                    try {
                                        Element submitElement = findElementByOption(document, ele_text, bro_pre_click_ele_type);
                                        submitElement.click();
                                        Thread.sleep(500);
                                    } catch (Exception exception) {
                                        exception.printStackTrace();
                                        printlnErrorOnUIAndConsole(String.format("Error For Location:[%s] <--> Action:[%s] <--> Error:[%s]", ele_text, bro_pre_click_ele_type, exception.getMessage()));
                                    }
                                }
                            }

                            //输入用户名
                            EleFoundStatus eleFoundStatusAction;
                            if (executeJavaScriptMode){
                                eleFoundStatusAction = setInputValueByJS(browser, bro_name_box_ele_text, bro_name_box_ele_type,  cur_user);
                            } else {
                                eleFoundStatusAction = findElementAndInputWithRetries(document, bro_name_box_ele_text, bro_name_box_ele_type, cur_user, GLOBAL_FIND_ELE_RETRY_TIMES, GLOBAL_FIND_ELE_DELAY_TIME);
                            }

                            //处理资源寻找状态
                            if(!SUCCESS.equals(eleFoundStatusAction)){
                                printlnErrorOnUIAndConsole(String.format("Error For Location [USERNAME] [%s] <--> Action: [%s]", bro_name_box_ele_text, eleFoundStatusAction));
                                //查找元素错误时的处理 继续还是中断
                                if(BREAK.equals(eleFoundStatusAction)) {break;} else if(CONTINUE.equals(eleFoundStatusAction)){continue;} else {continue;}
                            }else{
                                print_debug("find [USERNAME] Element And Input Success ...");
                            }

                            //查找密码输入框
                            if (executeJavaScriptMode){
                                eleFoundStatusAction = setInputValueByJS(browser, bro_pass_box_ele_text, bro_pass_box_ele_type,  cur_pass);
                            } else {
                                eleFoundStatusAction = findElementAndInputWithRetries(document, bro_pass_box_ele_text, bro_pass_box_ele_type, cur_pass, GLOBAL_FIND_ELE_RETRY_TIMES, GLOBAL_FIND_ELE_DELAY_TIME);
                            }
                            //处理资源寻找状态
                            if(!SUCCESS.equals(eleFoundStatusAction)){
                                printlnErrorOnUIAndConsole(String.format("Error For Location [PASSWORD] [%s] <--> Action: [%s]", bro_pass_box_ele_text, eleFoundStatusAction));
                                //查找元素错误时的处理 继续还是中断
                                if(BREAK.equals(eleFoundStatusAction)) {break;} else if(CONTINUE.equals(eleFoundStatusAction)){continue;} else {continue;}
                            }else{
                                print_debug("find [PASSWORD] Element And Input Success ...");
                            }

                            //获取验证码并进行识别
                            if (fxmlInstance.bro_id_ident_captcha_switch_check.isSelected()) {
                                //captcha_data不存在
                                if (FXMLDocumentController.captchaPictureData == null) {
                                    printlnErrorOnUIAndConsole("获取验证码失败 (数据为空) 重新测试...");
                                    captcha_ident_was_error = true;
                                    continue;
                                }

                                //获取输入的验证码元素定位信息
                                String bro_captcha_box_ele_text = fxmlInstance.bro_id_captcha_box_ele_text.getText().trim();
                                EleFindType bro_captcha_box_ele_type = EleFindType.fromString(fxmlInstance.bro_id_captcha_box_ele_type_combo.getValue());
                                if (isEmptyIfStr(bro_captcha_box_ele_text)) {
                                    printlnErrorOnUIAndConsole("验证码定位元素表单内容为空 请输入...");
                                    fxmlInstance.bro_id_captcha_box_ele_text.requestFocus();
                                    return;
                                }

                                //开始验证码识别
                                String captchaText = identCaptcha(null, fxmlInstance.captchaPictureData);
                                //判断验证码 是否是否正确
                                if(isEmptyIfStr(captchaText)){
                                    printlnErrorOnUIAndConsole(String.format("识别验证码失败 (结果为空) 重新测试...", captchaText));
                                    captcha_ident_was_error = true;
                                    localEleErrorCounts--; //验证码错误时重置
                                    continue;
                                }

                                //输入验证码元素 并检查输入状态
                                if (executeJavaScriptMode){
                                    eleFoundStatusAction = setInputValueByJS(browser, bro_captcha_box_ele_text, bro_captcha_box_ele_type,  captchaText);
                                } else {
                                    eleFoundStatusAction = findElementAndInputWithRetries(document,bro_captcha_box_ele_text, bro_captcha_box_ele_type, captchaText, GLOBAL_FIND_ELE_RETRY_TIMES, GLOBAL_FIND_ELE_DELAY_TIME);
                                }
                                //处理资源寻找状态
                                if(!SUCCESS.equals(eleFoundStatusAction)){
                                    printlnErrorOnUIAndConsole(String.format("Error For Location [CAPTCHA] [%s] <--> Action: [%s]", bro_captcha_box_ele_text, eleFoundStatusAction));
                                    //查找元素错误时的处理 继续还是中断
                                    if(BREAK.equals(eleFoundStatusAction)) {break;} else if(CONTINUE.equals(eleFoundStatusAction)){continue;} else {continue;}
                                }else{
                                    print_debug("find [CAPTCHA] Element And Input Success ...");
                                }
                                captcha_ident_was_error = false;
                            }

                            //定位提交按钮, 并填写按钮
                            EleFoundStatus EleFoundStatusForSubmitBtn = SUCCESS;
                            try {
                                Element submitElement = findElementByOption(document, bro_submit_btn_ele_text, bro_id_submit_btn_ele_type);
                                submitElement.click();
                            } catch (Exception exception) {
                                exception.printStackTrace();
                                printlnErrorOnUIAndConsole(String.format("Error For Location:[%s] <--> Action:[%s] <--> Error:[%s]", bro_submit_btn_ele_text, bro_id_submit_btn_ele_type, exception.getMessage()));
                                try {
                                    document.findElement(By.cssSelector("[type=submit]")).click();
                                } catch (IllegalStateException illegalStateException) {
                                    illegalStateException.printStackTrace();
                                    printlnErrorOnUIAndConsole("Error For document.findElement(By.cssSelector(\"[type=submit]\")).click()");
                                    EleFoundStatusForSubmitBtn = GLOBAL_FIND_ELE_NULL_ACTION;
                                }
                            } finally {
                                //处理按钮点击状态
                                if(!SUCCESS.equals(EleFoundStatusForSubmitBtn)){
                                    printlnErrorOnUIAndConsole(String.format("Error For Location [SUBMIT] [%s] <--> Action: [%s]", bro_submit_btn_ele_text, EleFoundStatusForSubmitBtn));
                                    //查找元素错误时的处理 继续还是中断
                                    if(BREAK.equals(eleFoundStatusAction)) {break;} else if(CONTINUE.equals(eleFoundStatusAction)){continue;} else {continue;}
                                }else{
                                    print_debug("find [SUBMIT] Element And Input Success ...");
                                }
                            }

                            //完成监测时重置查找错误计数
                            localEleErrorCounts = 0;

                            //在当前编辑区域（可能是文本框或富文本编辑器等）的光标位置插入一个新的空行，类似于按下回车键创建一个新行。
                            //browser.executeCommand(EditorCommand.INSERT_NEW_LINE);

                            //点击按钮前先重置页面加载状态
                            CURR_LOADING_STATUS ="";
                            CURR_LOGIN_STATUS = "";

                            //需要等待页面加载完毕
                            if (bro_id_submit_auto_wait_check.isSelected()){
                                Thread.sleep(GLOBAL_SUBMIT_AUTO_WAIT_INTERVAL);
                                long wait_start_time = System.currentTimeMillis();
                                while (isEmptyIfStr(CURR_LOADING_STATUS) || CURR_LOADING_STATUS.contains(LOADING_START.name())) {
                                    //输出检查状态
                                    printlnDebugOnUIAndConsole(String.format("checking status: [%s]", CURR_LOADING_STATUS));
                                    // 检查是否超时
                                    if (System.currentTimeMillis() - wait_start_time > GLOBAL_SUBMIT_AUTO_WAIT_LIMIT) {
                                        occurAccessUrlError = true;
                                        printlnDebugOnUIAndConsole("等待超时，退出循环");
                                        break;
                                    }
                                    //继续等待
                                    Thread.sleep(GLOBAL_SUBMIT_AUTO_WAIT_INTERVAL);
                                }
                            } else  {
                                Thread.sleep( bro_submit_fixed_wait_time>0 ? bro_submit_fixed_wait_time : 2000);
                            }


                            //设置 loading_status 为 const_loading_unknown
                            if(isEmptyIfStr(CURR_LOADING_STATUS)|| CURR_LOADING_STATUS.equals(LOADING_UNKNOWN.name())) {
                                printlnErrorOnUIAndConsole(String.format("最终页面状态异常: [%s] 保留: [%s]", CURR_LOADING_STATUS, bro_id_store_unknown_status_check.isSelected()));
                                CURR_LOADING_STATUS = LOADING_UNKNOWN.name();
                                occurAccessUrlError = true; //页面状态异常，就当是出错了吧,重新访问登录页
                            }

                            //输出加载状态
                            String cur_url = browser.getURL();
                            String cur_title = browser.getTitle();
                            int cur_length = browser.getHTML().length();

                            //判断是否跳转
                            boolean isPageForward = Trigger_Redirection || !urlRemoveQuery(login_access_url).equalsIgnoreCase(urlRemoveQuery(cur_url));
                            Trigger_Redirection = false; //重置全局重定向标记状态
                            //对于所有状态都进行日志记录
                            String title = "是否跳转,登录URL,测试账号,测试密码,跳转URL,网页标题,内容长度,爆破状态,加载状态";
                            MyFileUtils.writeTitleToFile(globalCrackLogRecodeFilePath, title);
                            String content = String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                                    escapeString(isPageForward),
                                    escapeString(login_access_url),
                                    escapeString(cur_user),
                                    escapeString(cur_pass),
                                    escapeString(cur_url),
                                    escapeString(cur_title),
                                    escapeString(cur_length),
                                    escapeString(CURR_LOGIN_STATUS),
                                    escapeString(CURR_LOADING_STATUS)
                            );
                            MyFileUtils.writeLineToFile(globalCrackLogRecodeFilePath, content);

                            if (true){
                                print_debug(String.format("本次 Crack Login 状态: %s", CURR_LOGIN_STATUS));

                                //判断登录状态是否时验证码码错误,是的话,就不能记录到爆破历史中
                                if(CURR_LOGIN_STATUS.contains(ERROR_CAPTCHA.name())){
                                    captcha_ident_was_error = true;
                                    MyFileUtils.writeUserPassPairToFile(globalErrorCaptchaFilePath, GLOBAL_PAIR_SEPARATOR, userPassPair);
                                    printlnErrorOnUIAndConsole(String.format("验证码识别错误 :-> 账号:密码【%s:%s】\n", cur_user, cur_pass));
                                    print_debug(String.format("验证码识别错误 :->\n" +
                                                    "账号:密码【%s:%s】\n" +
                                                    "跳转情况:%s -> %s->%s\n" +
                                                    "网页标题:%s -> 长度:%s\n" +
                                                    "加载状态:[%s]\n" +
                                                    "登录状态:[%s]\n",
                                            cur_user, cur_pass,
                                            isPageForward, login_access_url, cur_url,
                                            cur_title, cur_length,
                                            CURR_LOADING_STATUS, CURR_LOGIN_STATUS
                                    ));

                                    //不对统计计数进行增加, 需要重新测试本目标
                                } else if(CURR_LOGIN_STATUS.contains(LOGIN_SUCCESS.name())){
                                    //进行爆破历史记录、不对成功状态加载的数据进行重复爆破
                                    MyFileUtils.writeUserPassPairToFile(globalCrackHistoryFilePath, GLOBAL_PAIR_SEPARATOR, userPassPair);
                                    MyFileUtils.writeUserPassPairToFile(globalLoginSuccessFilePath, GLOBAL_PAIR_SEPARATOR, userPassPair);
                                    printlnInfoOnUIAndConsole(String.format("登录认证成功 :-> 账号:密码【%s:%s】\n", cur_user, cur_pass));
                                    print_debug(String.format("登录认证成功 :->\n" +
                                                    "账号:密码【%s:%s】\n" +
                                                    "跳转情况:%s -> %s->%s\n" +
                                                    "网页标题:%s -> 长度:%s\n" +
                                                    "加载状态:[%s]\n" +
                                                    "登录状态:[%s]\n",
                                            cur_user, cur_pass,
                                            isPageForward, login_access_url, cur_url,
                                            cur_title, cur_length,
                                            CURR_LOADING_STATUS, CURR_LOGIN_STATUS
                                    ));

                                    //对统计计数进行增加
                                    index ++;
                                } else if(CURR_LOGIN_STATUS.contains(LOGIN_FAILURE.name())){
                                    //进行爆破历史记录、不对错误状态加载的数据进行重复爆破
                                    MyFileUtils.writeUserPassPairToFile(globalCrackHistoryFilePath, GLOBAL_PAIR_SEPARATOR, userPassPair);
                                    MyFileUtils.writeUserPassPairToFile(globalLoginFailureFilePath, GLOBAL_PAIR_SEPARATOR, userPassPair);
                                    printlnErrorOnUIAndConsole(String.format("登录认证失败 :-> 账号:密码【%s:%s】\n", cur_user, cur_pass));
                                    print_debug(String.format("登录认证失败 :->\n" +
                                                    "账号:密码【%s:%s】\n" +
                                                    "跳转情况:%s -> %s->%s\n" +
                                                    "网页标题:%s -> 长度:%s\n" +
                                                    "加载状态:[%s]\n" +
                                                    "登录状态:[%s]\n",
                                            cur_user, cur_pass,
                                            isPageForward, login_access_url, cur_url,
                                            cur_title, cur_length,
                                            CURR_LOADING_STATUS, CURR_LOGIN_STATUS
                                    ));
                                    //对统计计数进行增加
                                    index ++;
                                } else {
                                    //响应状态已更新为未知状态，表名已经匹配过，但是没有正常匹配
                                    if (CURR_LOADING_STATUS.contains(LOADING_UNKNOWN.name())||Trigger_Redirection){
                                        if (bro_id_store_unknown_status_check.isSelected()||Trigger_Redirection){
                                            //进行爆破历史记录、不对未知状态加载的数据进行重复爆破
                                            MyFileUtils.writeUserPassPairToFile(globalCrackHistoryFilePath, GLOBAL_PAIR_SEPARATOR, userPassPair);
                                            //对于触发了重定向的数据要进行保存,大概率是登陆成功了
                                            if (Trigger_Redirection)
                                                printlnInfoOnUIAndConsole("[!] 触发未知状态及重定向, 可能登录成功...");
                                        }
                                        MyFileUtils.writeUserPassPairToFile(globalUnknownStatusFilePath, GLOBAL_PAIR_SEPARATOR, userPassPair);
                                        printlnErrorOnUIAndConsole(String.format("保存未知状态(%s) :->\n" +
                                                        "账号:密码【%s:%s】\n" +
                                                        "跳转情况:%s -> %s->%s\n" +
                                                        "网页标题:%s -> 长度:%s\n" +
                                                        "加载状态:[%s]\n" +
                                                        "登录状态:[%s]\n",
                                                bro_id_store_unknown_status_check.isSelected(),
                                                cur_user, cur_pass,
                                                isPageForward, login_access_url, cur_url,
                                                cur_title, cur_length,
                                                CURR_LOADING_STATUS, CURR_LOGIN_STATUS
                                        ));
                                        //对统计计数进行增加
                                    }  else {
                                        //对于其他的未知状态,进行下一个账号密码测试
                                        MyFileUtils.writeUserPassPairToFile(globalUnknownStatusFilePath, GLOBAL_PAIR_SEPARATOR, userPassPair);
                                        printlnErrorOnUIAndConsole(String.format("未知错误状态(%s) :->\n" +
                                                        "账号:密码【%s:%s】\n" +
                                                        "跳转情况:%s -> %s->%s\n" +
                                                        "网页标题:%s -> 长度:%s\n" +
                                                        "加载状态:[%s]\n" +
                                                        "登录状态:[%s]\n",
                                                bro_id_store_unknown_status_check.isSelected(),
                                                cur_user, cur_pass,
                                                isPageForward, login_access_url, cur_url,
                                                cur_title, cur_length,
                                                CURR_LOADING_STATUS, CURR_LOGIN_STATUS
                                        ));

                                        //判断当前是不是固定加载模式,是的话就自动添加一点加载时间
                                        if(!bro_id_submit_auto_wait_check.isSelected() && bro_submit_fixed_wait_time < GLOBAL_SUBMIT_AUTO_WAIT_LIMIT) {
                                            bro_submit_fixed_wait_time += 1000;
                                            printlnInfoOnUIAndConsole(String.format("等待超时|||自动更新等待时间至[%s]", bro_submit_fixed_wait_time));
                                        }

                                        //对于未知加载状态的数据进行跳过，但是不保存，防止死循环的发生
                                    }
                                    index ++;
                                }
                            }

                            // 输出预计剩余运行时间
                            long elapsedTime = (System.currentTimeMillis() - startTime) / 1000; //单位 秒
                            long residueTime = (globalUserPassPairsArray.length - index - 1) * elapsedTime / 60; //单位 分
                            print_debug(String.format("本次运行时间:[%s]秒, 预计剩余时间:[%s]分钟 ==> [%s]小时...\n", elapsedTime, residueTime, residueTime/60));
                        }

                        //停止所有请求,防止影响到下一次的使用 //6.15版本没有办法处理关闭浏览器
                        printlnInfoOnUIAndConsole("所有爆破任务正常结束...");
                        stopCrackStatus=true;
                    } catch (Exception e) {
                        // 处理特定的 IllegalStateException
                        if (e.getMessage().contains("stream was closed")) {
                            stopCrackStatus=false;
                            printlnDebugOnUIAndConsole("发生已知异常[Channel stream was closed !!!] | 即将重试...");
                        }else if (e.getMessage().contains("Failed to send message")) {
                            stopCrackStatus=false;
                            printlnDebugOnUIAndConsole("发生已知异常[Failed to send message !!!] | 即将重试...");
                        }else if (e.getMessage().contains("Failed to send message")) {
                            stopCrackStatus=false;
                            printlnDebugOnUIAndConsole("发生已知异常[Failed to send message !!!] | 即将重试...");
                        } else {
                            // 其他类型的 IllegalStateException
                            stopCrackStatus=!GLOBAL_UNKNOWN_ERROR_NOT_STOP;
                            e.printStackTrace();
                            printlnErrorOnUIAndConsole(String.format("发生未知异常:[%s]|StopCrack:[%s]", e.getMessage(), stopCrackStatus));
                        }
                    } finally {
                        //开始进行重新测试
                        if (!stopCrackStatus && !SystemUtilization.isSystemOverloaded()){
                            startCrack(actionEvent);
                        } else{
                            //关闭当前浏览器,后续需要优化为连Chrome进程一起关闭
                            browser.dispose();
                        }
                    }
                }
            }).start();
        });

    }


}
