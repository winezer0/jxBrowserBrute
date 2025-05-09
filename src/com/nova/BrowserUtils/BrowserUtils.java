package com.nova.BrowserUtils;

import com.nova.CommonUtils.ElementUtils;
import com.nova.CommonUtils.UiUtils;
import com.nova.CommonUtils.Utils;
import com.nova.LoadConfig.Constant;
import com.teamdev.jxbrowser.chromium.*;
import com.teamdev.jxbrowser.chromium.dom.By;
import com.teamdev.jxbrowser.chromium.dom.DOMDocument;
import com.teamdev.jxbrowser.chromium.dom.internal.Element;
import com.teamdev.jxbrowser.chromium.dom.internal.InputElement;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nova.CommonUtils.ElementUtils.isContainOneKeyByEach;
import static com.nova.LoadConfig.MyConst.*;
import static com.nova.PrintLog.PrintLog.print_debug;

public class BrowserUtils {
    //清除浏览器当前 Cookies
    public static void clearCookieStorage(Browser browser) {
        //清除cookie
        //参考 JxBrowser 之五：清除cache和cookie以及SSL证书处理 https://www.yii666.com/article/677652.html
        CookieStorage cookieStorage = browser.getCookieStorage();
        if (!cookieStorage.getAllCookies().isEmpty()){
            cookieStorage.deleteAll();
            cookieStorage.save();
        }
    }

    /**
     * 解析 cookie 字符串到一个 Map。
     * @param cookiesStr 待解析的 cookie 字符串。
     * @return 包含 cookie 名称和值的 Map。
     */
    public static Map<String, String> parseCookiesStr(String cookiesStr) {
        Map<String, String> cookies = new HashMap<>();
        Pattern pattern = Pattern.compile("[^; ]+=[^; ]+");
        Matcher matcher = pattern.matcher(cookiesStr);

        while (matcher.find()) {
            String cookieEntry = matcher.group();
            String[] keyValue = cookieEntry.split("=", 2); // 最多分割一次
            if (keyValue.length == 2) {
                cookies.put(keyValue[0].trim(), keyValue[1].trim());
            } else {
                // 处理没有值的情况（例如：SessionID=; Path=/）
                cookies.put(cookieEntry.trim(), "");
            }
        }
        return cookies;
    }

    //设置浏览器当前Cookies 有些场景下需要先设置cookie才能访问登录页面
    public static void setBrowserCookies(Browser browser, String urlStr, String cookiesStr) {
        // Cookie 操作 ： https://teamdev.com/jxbrowser/docs/6/guides/cookies/

//        urlStr = "http://34.96.183.238:81/admin/public/doLogin.html";
//        cookiesStr = "Cookie: thinkphp_show_page_trace=0|0; PHPSESSID=cde568e4057972b8f1f1e7afae4baad1";

        if (urlStr == null|| cookiesStr == null  || urlStr.trim().isEmpty() || cookiesStr.trim().isEmpty()){
            System.out.println("未输入有效URL和Cookies字符串...");
            return;
        }

        //获取URL相关数据
        String target = Utils.extractDomain(urlStr,true);
        String domain = Utils.extractDomain(urlStr,false);

        if (domain==null||target==null||domain.trim().isEmpty()||target.trim().isEmpty()){
            System.out.printf("解析 URL [%s] 结果为空!!!%n", urlStr);
            return;
        }

        //解析请求cookie数据
        Map<String, String> cookies = parseCookiesStr(cookiesStr);
        if (cookies == null || cookies.isEmpty()){
            System.out.printf("解析 Cookies [%s] 结果为空!!!%n", cookiesStr);
            return;
        }

        // 设置Cookie
        CookieStorage cookieStorage = browser.getCookieStorage();
        for (Map.Entry<String, String> cookie : cookies.entrySet()) {
            String cookieName = cookie.getKey();
            String cookieValue = cookie.getValue();
            System.out.printf("Setting %s <-> %s  <-> %s:%s%n", target,domain,cookieName,cookieValue);

            //设置持久化Cookie
            cookieStorage.setCookie(target, cookieName, cookieValue, domain,"/", (System.currentTimeMillis() +  7 * 24 * 60 * 60) * 1000, false, false);
            //设置会话Cookie 设置失败
            //cookieStorage.setSessionCookie(target, cookieName, cookieValue, domain, "/", false, false);
        }
        //保存cookie设置
        cookieStorage.save();
        //输出Cookie设置
        //List<Cookie> allCookies = browser.getCookieStorage().getAllCookies(target);
        //输出指定URL的Cookies
        List<Cookie>  allCookies = browser.getCookieStorage().getAllCookies(target);
        System.out.printf("New Cookies: size:%s <-> %s%n", allCookies.size(), cookiesToString(allCookies));
    }

    //转换当前Cookies到字符串
    public static String cookiesToString(List<Cookie> cookies ) {
        StringBuilder cookiesStringBuilder = new StringBuilder();
        for (Cookie cookie:cookies){
//            String cookieString = String.format(
//                    "Cookie{name='%s', value='%s', domain='%s', path='%s', creationTime=%d, unixCreationTime=%d,
//                    expirationTime=%d, unixExpirationTime=%d, secure=%b, httpOnly=%b, session=%b}",
//                    cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(),
//                    cookie.getCreationTime(), cookie.getUnixCreationTime(), cookie.getExpirationTime(),
//                    cookie.getUnixExpirationTime(), cookie.isSecure(), cookie.isHTTPOnly(), cookie.isSession()
//            );
            String cookieString = String.format(
                    "Cookie{name='%s', value='%s', domain='%s', path='%s'",
                    cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath()
            );
            // 将每个 Cookie 的字符串添加到 StringBuilder 中，并在每个 Cookie 之间添加换行符
            cookiesStringBuilder.append(cookieString).append(System.lineSeparator());
        }
        // 返回拼接后的字符串
        return cookiesStringBuilder.toString();
    }

    public static void OpenUrlWithLocalBrowser(String url) {
        //使用系统浏览器打开网页
        try {
            URI uri = URI.create(url);
            Desktop dp = Desktop.getDesktop();
            if (dp.isSupported(Desktop.Action.BROWSE))
                dp.browse(uri);
        } catch (NullPointerException localNullPointerException) {
        } catch (Exception e) {
            try {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    public static InputElement findInputElementByOption(DOMDocument doc, String elementValue, Constant.EleFindType eleFindType ) {
        //输入用户名元素 //需要添加输入XPath|css元素
        InputElement inputElement;
        switch (eleFindType) {
            case ID:
                inputElement = (InputElement) doc.findElement(By.id(elementValue));
                break;
            case NAME:
                inputElement = (InputElement) doc.findElement(By.name(elementValue));
                break;
            case CLASS:
                inputElement = (InputElement) doc.findElement(By.className(elementValue));
                break;
            case CSS:
                inputElement = (InputElement) doc.findElement(By.cssSelector(elementValue));
                break;
            case XPATH:
            default:
                inputElement = (InputElement) doc.findElement(By.xpath(elementValue));
        }
        return inputElement;
    }

    public static Element findElementByOption(DOMDocument doc, String elementValue, Constant.EleFindType eleFindType ) {
        //输入用户名元素 //需要添加输入XPath|css元素
        Element element;
        switch (eleFindType) {
            case ID:
                element = (Element) doc.findElement(By.id(elementValue));
                break;
            case NAME:
                element = (Element) doc.findElement(By.name(elementValue));
                break;
            case CLASS:
                element = (Element) doc.findElement(By.className(elementValue));
                break;
            case CSS:
                element = (Element) doc.findElement(By.cssSelector(elementValue));
                break;
            case XPATH:
            default:
                element = (Element) doc.findElement(By.xpath(elementValue));
        }
        return element;
    }

    public static void setBrowserProxyMode(Browser browser, boolean useProxy, String browserProxyStr, String loginProto) {
        //浏览器代理设置
        if (browser != null) {
            if (useProxy && ElementUtils.isNotEmptyObj(browserProxyStr)) {
                //输入的不是完整的格式 127.0.0.1:8080 需要配置代理协议
                if (!isContainOneKeyByEach(browserProxyStr, "http://|socks://|https://", false)){
                    browserProxyStr = String.format("%s://%s", loginProto, browserProxyStr);
                }

                //参考 使用代理 https://www.kancloud.cn/neoman/ui/802531
                browserProxyStr = browserProxyStr.replace("://", "=");
                browser.getContext().getProxyService().setProxyConfig(new CustomProxyConfig(browserProxyStr));
                print_debug(String.format("Browser Proxy Was Configured [%s]", browserProxyStr));
            } else {
                browser.getContext().getProxyService().setProxyConfig(new DirectProxyConfig());
                print_debug("Browser Proxy Was Configured Direct Proxy mode");
            }
        }
    }

    /**
     * 查找元素并输入
     * @ browser_close_action 浏览器关闭时的异常动作
     * @ find_ele_illegal_action 页面中元素操作异常的动作
     * @ find_ele_null_action 页面中没有找到元素的动作
     * @ find_ele_exception_action 页面中元素操作其他异常的动作
     * @param document 页面的文档对象
     * @param locate_info 定位信息
     * @param eleFindType 定位选项
     * @param input_string 输入值
     * @return
     */
    public static Constant.EleFoundStatus findElementAndInput(DOMDocument document, String locate_info, Constant.EleFindType eleFindType, String input_string) {
        Constant.EleFoundStatus eleFoundStatusAction = Constant.EleFoundStatus.SUCCESS;
        try {
            InputElement findElement = findInputElementByOption(document, locate_info, eleFindType);
            Map<String, String> attributes = findElement.getAttributes();
            //findElement.click(); // 尝试前后新增 .click() 解决部分场景内容输入后提示没有内容的问题 无效果
            findElement.setValue(input_string);
            // for (String attrName : attributes.keySet()) { System.out.println(attrName + " = " + attributes.get(attrName)); }
        }
        catch (IllegalStateException illegalStateException) {
            String eMessage = illegalStateException.getMessage();
            System.out.println(eMessage);
            if (eMessage.contains("Channel is already closed")) {
                eleFoundStatusAction = GLOBAL_BROWSER_CLOSE_ACTION;
                UiUtils.printlnErrorOnUIAndConsole(String.format("浏览器已关闭 (IllegalStateException) 动作:[%s]", eleFoundStatusAction));
            }else {
                illegalStateException.printStackTrace();
                eleFoundStatusAction = GLOBAL_FIND_ELE_ILLEGAL_ACTION;
                UiUtils.printlnErrorOnUIAndConsole(String.format("illegal State Exception 动作:[%s]", eleFoundStatusAction));
            }
        } catch (NullPointerException nullPointerException) {
            eleFoundStatusAction = GLOBAL_FIND_ELE_NULL_ACTION;
            UiUtils.printlnErrorOnUIAndConsole(String.format("定位元素失败 (nullPointerException) 动作:[%s]", eleFoundStatusAction));
        } catch (Exception exception) {
            exception.printStackTrace();
            eleFoundStatusAction = GLOBAL_FIND_ELE_EXCEPTION_ACTION;
            UiUtils.printlnErrorOnUIAndConsole(String.format("未知定位异常 (unknown exception) 动作:[%s]", eleFoundStatusAction));
        }
        return eleFoundStatusAction;
    }

    /***
     * 支持重试的元素查找方案
     * maxRetries 尝试次数
     * retryInterval 重试间隔时间，单位：毫秒
     */
    public static Constant.EleFoundStatus findElementAndInputWithRetries(DOMDocument document, String locateInfo, Constant.EleFindType eleFindType, String inputString, int maxRetries, long retryInterval) {

        int retries = 0;
        Constant.EleFoundStatus action_status = Constant.EleFoundStatus.FAILURE;

        while (!Constant.EleFoundStatus.SUCCESS.equals(action_status) && retries < maxRetries) {
            action_status = findElementAndInput(document, locateInfo, eleFindType, inputString);
            if (Constant.EleFoundStatus.SUCCESS.equals(action_status)) { break; }

            // 延迟500毫秒后重试
            try {
                TimeUnit.MILLISECONDS.sleep(retryInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Thread was interrupted during sleep.");
            }

            retries++;
        }
        return action_status;
    }
}
