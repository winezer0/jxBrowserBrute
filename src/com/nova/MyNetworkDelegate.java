package com.nova;

import com.nova.CommonUtils.ElementUtils;
import com.nova.CommonUtils.HttpUrlInfo;
import com.nova.LoadConfig.Constant;
import com.teamdev.jxbrowser.chromium.*;
import com.teamdev.jxbrowser.chromium.javafx.DefaultNetworkDelegate;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static cn.hutool.core.util.StrUtil.isEmptyIfStr;
import static com.nova.CommonUtils.ElementUtils.*;
import static com.nova.CommonUtils.UiUtils.*;
import static com.nova.CommonUtils.Utils.concatHeaders;
import static com.nova.CommonUtils.Utils.countNotEmptyStrings;
import static com.nova.LoadConfig.Constant.LoadingStatus.LOADING_FINISH;
import static com.nova.LoadConfig.Constant.LoginStatus.*;
import static com.nova.LoadConfig.MyConst.GLOBAL_MATCH_BLOCK_SUFFIX;
import static com.nova.PrintLog.PrintLog.print_debug;
import static com.nova.PrintLog.PrintLog.print_info;

public class MyNetworkDelegate extends DefaultNetworkDelegate {

    private boolean isCompleteAuth;
    private boolean isCancelAuth;

    private String captchaActualUrl;  //验证码请求包URL
    private Constant.HttpMethod captchaActualMethod;  //验证码请求包URL的请求方法

    private String matchRespUrl; //登录包请求URL
    private Constant.HttpMethod matchRespMethod; //登录包请求URL的请求方法

    private boolean onlyMatchRespUrl;  //仅匹配指定URL的响应
    //private long current_id;

    private byte[] tmpCaptchaBytes = new byte[0]; // 使用byte数组代替Appendable

    private String loginFailureKey;  //登录失败匹配关键字
    private String captchaFailKey;   //验证码错误匹配关键字
    private String loginSuccessKey;  //登录成功匹配关键字

    private AtomicBoolean isCapturingCaptcha = new AtomicBoolean(false); // 用于线程安全

    /**
     * @param captchaActualUrl 验证码相关URL 支持正则格式
     * @param matchRespUrl   登录包相关URL 支持正则格式
     * @param onlyMatchRespUrl 是否仅匹配指定URL的响应 否的话在所有请求中都取查找登录匹配结果关键字
     */
    public MyNetworkDelegate(String captchaActualUrl, Constant.HttpMethod captchaActualMethod,
                             String matchRespUrl, Constant.HttpMethod matchRespMethod,
                             boolean onlyMatchRespUrl, String captchaFailKey, String loginFailureKey, String loginSuccessKey)
    {
        this.captchaActualUrl = captchaActualUrl;
        this.captchaActualMethod = captchaActualMethod;

        this.onlyMatchRespUrl = onlyMatchRespUrl;
        this.matchRespUrl = matchRespUrl;
        this.matchRespMethod = matchRespMethod;

        this.captchaFailKey = captchaFailKey;
        this.loginFailureKey = loginFailureKey;
        this.loginSuccessKey = loginSuccessKey;
    }


    @Override
    //在浏览器发出网络请求之前调用。
    public void onBeforeURLRequest(BeforeURLRequestParams params) {
        //BeforeURLRequestParams params：包含即将发出的请求信息。
        //params.getRequestId()：获取请求的唯一标识符。
        //params.getMethod()：获取HTTP请求方法（如GET、POST等）。
        //params.getUrl()：获取请求的目标URL。
        //params.getRequestHeaders()：获取请求头信息。
        //params.getPostData()：获取POST请求的数据（如果有的话）。
        //params.isMainFrame()：判断是否是主框架（即整个页面）的请求。
        //params.hasUserGesture()：判断请求是否由用户操作触发（例如点击链接）。
        //params.getTransitionType()：获取请求的过渡类型（例如链接点击、表单提交
    }

    @Override
    //在浏览器准备发送请求头之前调用
    public void onBeforeSendHeaders(BeforeSendHeadersParams params) {
        //BeforeURLRequestParams params：包含即将发出的请求信息及其头信息。
        //允许您直接响应请求而不实际发送它，或者返回 null 表示继续正常的请求流程。
        //super.onSendHeaders(params);
        String currURL = params.getURL();
        String currMethod = params.getMethod();
        //需要判断那think php的情况，baseurl都是一样的，不能作为验证码图片URL

        if(isSimilarLinkAndMethod(currURL, this.captchaActualUrl, Constant.HttpMethod.fromString(currMethod), this.captchaActualMethod)){
            this.tmpCaptchaBytes = new byte[0];
            params.getHeadersEx().setHeader("Accept-Encoding", "");
            //print_debug(String.format("修改验证码请求头 BeforeSendHeaders: %s", currURL));
        }
    }

    /**
     * 判断当前URL和请求方法是否等于预期的URL和请求方法,用来确认是否是登录包和验证码包
     */
    private boolean isSimilarLinkAndMethod(String curUrl, String exceptUrl, Constant.HttpMethod curMethod, Constant.HttpMethod exceptMethod){
        return curUrl != null && curMethod.equals(exceptMethod) && ElementUtils.isSimilarLink(curUrl, exceptUrl);
    }

    @Override
    //在浏览器实际发送请求头之后立即调用
    public void onSendHeaders(SendHeadersParams params) {
/*
        String currURL = params.getURL();
        Constant.HttpMethod currMethod = Constant.HttpMethod.fromString(params.getMethod());
        if(isSimilarLinkAndMethod(currURL, this.captchaActualUrl, currMethod, this.captchaActualMethod)){
            print_debug(String.format("正在发起验证码请求 onSendHeaders: %s", currURL));
        }

        if(isSimilarLinkAndMethod(currURL, this.loginActualUrl, currMethod, this.loginActualMethod)){
           print_debug(String.format("正在发起登录包请求 onSendHeaders: %s", currURL));
        }
*/
    }


    @Override
    //当接收到部分响应数据时调用
    public void onDataReceived(DataReceivedParams params) {
        String currUrl = params.getURL();
        Constant.HttpMethod currMethod = Constant.HttpMethod.fromString(params.getMethod());
        if(isSimilarLinkAndMethod(currUrl, this.captchaActualUrl, currMethod, this.captchaActualMethod)){
            //存储验证码数据
            storeCaptchaData(currUrl, currMethod, params);
        } else {
            //检查登录关键字匹配状态
            checkLoginStatusOnBody(currUrl, currMethod, params);
        }
    }

    @Override
    //当服务器开始发送响应时调用
    public void onResponseStarted(ResponseStartedParams params) {
        super.onResponseStarted(params);
    }

    @Override
    //当接收到服务器响应头时调用
    public void onHeadersReceived(HeadersReceivedParams params) {
        //super.onHeadersReceived(params);
        String currUrl = params.getURL();
        Constant.HttpMethod currMethod = Constant.HttpMethod.fromString(params.getMethod());
        if (isSimilarLinkAndMethod(currUrl, this.captchaActualUrl, currMethod, this.captchaActualMethod)){
            print_debug(String.format("正在接受验证码数据头部 onHeadersReceived: %s", currUrl));
        }

        //部分情况下需要从响应头匹配 Location 字段来判断是否成功登录
        if(onlyMatchRespUrl && this.matchRespUrl != null){
            //在精准模式进行响应头匹配
            if (isSimilarLinkAndMethod(currUrl, this.matchRespUrl, currMethod, this.matchRespMethod)){
                print_debug(String.format("当前进入精准匹配模式 ON Headers: [%s] [%s]...", currMethod, currUrl));
                String receivedHeaders = concatHeaders(params.getHeadersEx().getHeaders());
                handleLoginStatus(currUrl, receivedHeaders, true);
            }
        }else {
            //非精准模式下对常见的静态后缀进行排除、然后进行检查
            String urlSuffix = new HttpUrlInfo(currUrl).getSuffix();
            if (!isEqualsOneKey(urlSuffix, GLOBAL_MATCH_BLOCK_SUFFIX, false)){
                print_debug(String.format("当前进入粗略匹配模式 ON Headers: [%s->%s] NOT IN [%s]...",currUrl, urlSuffix, GLOBAL_MATCH_BLOCK_SUFFIX));
                String receivedHeaders = concatHeaders(params.getHeadersEx().getHeaders());
                handleLoginStatus(currUrl, receivedHeaders, true);
            }
        }
    }

    @Override
    //当请求完成（无论是成功还是失败）时调用
    public void onCompleted(RequestCompletedParams params) {
        //super.onCompleted(params);
        String currUrl = params.getURL();
        Constant.HttpMethod currMethod = Constant.HttpMethod.fromString(params.getMethod());
        if(isSimilarLinkAndMethod(currUrl, this.captchaActualUrl, currMethod, this.captchaActualMethod)){
            //print_debug(String.format("验证码URL请求完成 onHeadersReceived: %s", currUrl));
        }

        if (isSimilarLinkAndMethod(currUrl, this.matchRespUrl, currMethod, this.matchRespMethod)){
            print_debug(String.format("登陆包URL请求完成 onHeadersReceived: %s", currUrl));
            //此处应该可以更新加载状态为 已完成  FXMLDocumentController.CURR_LOADING_STATUS = LOADING_FINISH.name();
        }
    }

    @Override
    //在浏览器准备跟随重定向之前调用
    public void onBeforeRedirect(BeforeRedirectParams params) {
        super.onBeforeRedirect(params);
        FXMLDocumentController.Trigger_Redirection = true;
        print_info(String.format("[*] 触发重定向:%s -> %s",params.getURL(), params.getNewURL()));
    }

    @Override
    //当请求对象被销毁时调用
    public void onDestroyed(RequestParams params) {
        super.onDestroyed(params);
    }

    @Override
    //决定是否允许设置给定的Cookie
    public boolean onCanSetCookies(String url, List<Cookie> cookies) {
        return super.onCanSetCookies(url, cookies);
    }

    @Override
    //决定是否允许获取给定的Cookie
    public boolean onCanGetCookies(String url, List<Cookie> cookies) {
        return super.onCanGetCookies(url, cookies);
    }

    @Override
    //在发送代理服务器请求头之前调用。
    public void onBeforeSendProxyHeaders(BeforeSendProxyHeadersParams params) {
        //BeforeSendProxyHeadersParams params：包含即将发送到代理服务器的请求信息及其头信息。
        super.onBeforeSendProxyHeaders(params);
    }

    @Override
    //当代理自动配置脚本（PAC）执行出错时调用
    public void onPACScriptError(PACScriptErrorParams params) {
        super.onPACScriptError(params);
    }

    @Override
    public boolean onAuthRequired(AuthRequiredParams paramAuthRequiredParams) {
        //提示需要认证 当服务器要求进行身份验证时调用
        System.out.println("需要认证");
        this.isCompleteAuth = false;
        this.isCancelAuth = false;

        Platform.runLater(new Runnable() {
            public void run() {
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                TextField user_field = new TextField();
                TextField pass_field = new TextField();
                user_field.setPromptText("用户名");
                pass_field.setPromptText("密码");

                Button ok_button = new Button("确定");
                Button cancel_button = new Button("取消");

                HBox hbox = new HBox(50.0D);
                hbox.getChildren().addAll(new Node[]{ok_button, cancel_button});

                VBox vbox = new VBox(20.0D, new Node[]{user_field, pass_field, hbox});
                vbox.setPadding(new Insets(30.0D, 30.0D, 30.0D, 30.0D));

                vbox.setAlignment(Pos.CENTER);
                hbox.setAlignment(Pos.CENTER);

                Scene scene = new Scene(vbox);
                stage.setScene(scene);
                stage.setTitle("请输入用户名密码");
                stage.sizeToScene();
                user_field.requestFocus();
                EventHandler okAction = new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent arg0) {
                        paramAuthRequiredParams.setUsername(user_field.getText());
                        paramAuthRequiredParams.setPassword(pass_field.getText());
                        MyNetworkDelegate.this.isCancelAuth = false;
                        MyNetworkDelegate.this.isCompleteAuth = true;
                        stage.close();
                    }
                };
                EventHandler cancelAction = new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent arg0) {
                        MyNetworkDelegate.this.isCancelAuth = true;
                        MyNetworkDelegate.this.isCompleteAuth = true;
                        stage.close();
                    }
                };
                stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    //关闭请求时的动作
                    @Override
                    public void handle(WindowEvent event) {
                        MyNetworkDelegate.this.isCancelAuth = true;
                        System.out.println("hehe");
                        MyNetworkDelegate.this.isCompleteAuth = true;
                    }
                });
                ok_button.setOnAction(okAction);
                user_field.setOnAction(okAction);
                pass_field.setOnAction(okAction);
                cancel_button.setOnAction(cancelAction);
                stage.showAndWait();
            }
        });
        while (!this.isCompleteAuth) {
            System.out.println("等待输入账号密码");
        }
        System.out.println(this.isCancelAuth);
        return this.isCancelAuth;
    }


    /**
     * 存储验证码数据。
     *
     * @param receivedParams 包含接收的数据和URL等信息的参数对象
     */
    public void storeCaptchaData(String currUrl, Constant.HttpMethod currMethod, DataReceivedParams receivedParams) {
        if (isCapturingCaptcha.get()) {
            return; // 如果已经在捕获验证码，则直接返回
        }

        // 如果当前URL是验证码URL请求,就开始获取验证码图片数据
        if(isSimilarLinkAndMethod(currUrl, this.captchaActualUrl, currMethod, this.captchaActualMethod)){
            byte[] receivedData = receivedParams.getData();
            try {
                isCapturingCaptcha.set(true);
                // 创建新的字节数组来存储接收到的数据
                byte[] newCaptchaBytes = new byte[tmpCaptchaBytes.length + receivedData.length];
                System.arraycopy(tmpCaptchaBytes, 0, newCaptchaBytes, 0, tmpCaptchaBytes.length);
                System.arraycopy(receivedData, 0, newCaptchaBytes, tmpCaptchaBytes.length, receivedData.length);
                this.tmpCaptchaBytes = newCaptchaBytes;

                FXMLDocumentController.captchaPictureData = this.tmpCaptchaBytes.clone(); // 克隆数组以避免外部修改

                if (this.tmpCaptchaBytes.length == 0) {
                    print_debug(String.format("获取验证码数据失败 onDataReceived is Empty From [%s]", currUrl));
                } else {
                    print_debug(String.format("获取验证码数据成功 onDataReceived:[%d] From [%s]", this.tmpCaptchaBytes.length, currUrl));
                }
            } catch (Exception e) {
                print_debug(String.format("获取验证码数据失败 onDataReceived From [%s] receivedData[%s] Error:[%s]", currUrl, receivedData.length, e.getMessage()));
            } finally {
                isCapturingCaptcha.set(false); // 确保设置回false，即使发生异常
            }
        }
    }


    public void checkLoginStatusOnBody(String currUrl, Constant.HttpMethod currMethod, DataReceivedParams receivedParams) {

        try {
            String charset = receivedParams.getCharset();
            if (isEmptyIfStr(charset)) {
                charset = "UTF-8"; // 使用大写的 UTF-8 作为标准
            }
            // 检查是否为精准匹配模式
            if (this.onlyMatchRespUrl && this.matchRespUrl != null) {
                if (isSimilarLinkAndMethod(currUrl, this.matchRespUrl, currMethod, this.matchRespMethod)) {
                    print_debug(String.format("当前进入精准匹配模式 ON body: [%s] [%s]...", currMethod, currUrl));
                    String receiveData = new String(receivedParams.getData(), charset);
                    if (receiveData.contains("GB2312")) receiveData = new String(receivedParams.getData(), "GB2312");
                    handleLoginStatus(currUrl, receiveData, false);
                }
            } else {
                String urlSuffix = new HttpUrlInfo(currUrl).getSuffix();
                if (!isEqualsOneKey(urlSuffix, GLOBAL_MATCH_BLOCK_SUFFIX, false)){
                    String receiveData = new String(receivedParams.getData(), charset);
                    if (receiveData.contains("GB2312")) receiveData = new String(receivedParams.getData(), "GB2312");
                    print_debug(String.format("当前进入粗略匹配模式 ON body: [%s->%s] NOT IN [%s]...",currUrl, urlSuffix, GLOBAL_MATCH_BLOCK_SUFFIX));
                    handleLoginStatus(currUrl, receiveData, false);
                }
            }
        } catch (UnsupportedEncodingException e) {
            //e.printStackTrace();
            printlnErrorOnUIAndConsole(String.format("响应结果关键字匹配发生错误:[%s] -> Error:[%s]", currUrl, e.getMessage()));
        }
    }

    private void handleLoginStatus(String currUrl, String receiveData, Boolean isHeader) {
        String foundStrForLoginSuccess = ElementUtils.FoundContainSubString(receiveData, loginSuccessKey);
        String foundStrForLoginFailure = ElementUtils.FoundContainSubString(receiveData, loginFailureKey);
        String foundStrForCaptchaFail = ElementUtils.FoundContainSubString(receiveData, captchaFailKey);

        int notEmptyStrNum = countNotEmptyStrings(foundStrForLoginSuccess, foundStrForCaptchaFail, foundStrForLoginFailure);

        if (notEmptyStrNum == 1) {
            //确定已经匹配成功,就将当前加载状态设置为已完成
            FXMLDocumentController.CURR_LOADING_STATUS = LOADING_FINISH.name();

            if (isHeader){
                if (isNotEmptyObj(foundStrForLoginSuccess)) {
                    FXMLDocumentController.CURR_LOGIN_STATUS = String.format("%s<->%s<->Header", LOGIN_SUCCESS.name(), currUrl);
                    printlnInfoOnUIAndConsole(String.format("响应头部匹配: 登录成功 %s [匹配结果:%s]", FXMLDocumentController.CURR_LOGIN_STATUS, foundStrForLoginSuccess));
                }
                if (isNotEmptyObj(foundStrForLoginFailure)) {
                    FXMLDocumentController.CURR_LOGIN_STATUS = String.format("%s<->%s<->Header", LOGIN_FAILURE.name(), currUrl);
                    printlnErrorOnUIAndConsole(String.format("响应头部匹配: 登录失败 %s [匹配结果:%s]", FXMLDocumentController.CURR_LOGIN_STATUS, foundStrForLoginFailure));
                }
                if (isNotEmptyObj(foundStrForCaptchaFail)) {
                    FXMLDocumentController.CURR_LOGIN_STATUS = String.format("%s<->%s<->Header", ERROR_CAPTCHA.name(), currUrl);
                    printlnErrorOnUIAndConsole(String.format("响应头部匹配: 验证码错误 %s [匹配结果:%s]", FXMLDocumentController.CURR_LOGIN_STATUS, foundStrForCaptchaFail));
                }
            } else {
                //如果已经从响应头部匹配出结果、就忽略状态更新、否则继续匹配
                if (isEmptyObj(FXMLDocumentController.CURR_LOGIN_STATUS)){
                    if (isNotEmptyObj(foundStrForLoginSuccess)) {
                        FXMLDocumentController.CURR_LOGIN_STATUS = String.format("%s<->%s<->Body", LOGIN_SUCCESS.name(), currUrl);
                        printlnInfoOnUIAndConsole(String.format("响应内容匹配: 登录成功 %s [匹配结果:%s]", FXMLDocumentController.CURR_LOGIN_STATUS, foundStrForLoginSuccess));
                    }

                    if (isNotEmptyObj(foundStrForLoginFailure)) {
                        FXMLDocumentController.CURR_LOGIN_STATUS = String.format("%s<->%s<->Body", LOGIN_FAILURE.name(), currUrl);
                        printlnErrorOnUIAndConsole(String.format("响应内容匹配: 登录失败 %s [匹配结果:%s]", FXMLDocumentController.CURR_LOGIN_STATUS, foundStrForLoginFailure));
                    }

                    if (isNotEmptyObj(foundStrForCaptchaFail)) {
                        FXMLDocumentController.CURR_LOGIN_STATUS = String.format("%s<->%s<->Body", ERROR_CAPTCHA.name(), currUrl);
                        printlnErrorOnUIAndConsole(String.format("响应内容匹配: 验证码错误 %s [匹配结果:%s]", FXMLDocumentController.CURR_LOGIN_STATUS, foundStrForCaptchaFail));
                    }
                }
            }
        }

        if (notEmptyStrNum == 0 && !isHeader && isEmptyObj(FXMLDocumentController.CURR_LOGIN_STATUS)){
            //printlnErrorOnUIAndConsole(String.format("当前请求[%s]所有响应关键字匹配出错!!\n响应长度:[%s]响应内容:[%s]", currUrl, receive.length(), receive));
            printlnErrorOnUIAndConsole(String.format("当前请求[%s]响应关键字匹配失败!!响应长度:[%s]", currUrl, receiveData.length()));
        }

        if (notEmptyStrNum > 1) {
            printlnErrorOnUIAndConsole(String.format("匹配关键字异常[Status:%s] 存在多个匹配结果: [Success:%s|CaptchaFail:%s|LoginFailure:%s]", FXMLDocumentController.CURR_LOGIN_STATUS, foundStrForLoginSuccess, foundStrForCaptchaFail, foundStrForLoginFailure));
        }
    }
}
