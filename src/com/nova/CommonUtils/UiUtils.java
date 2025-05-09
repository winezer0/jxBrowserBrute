package com.nova.CommonUtils;

import com.nova.FXMLDocumentController;
import com.nova.LoadConfig.Constant;
import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.JSBoolean;
import com.teamdev.jxbrowser.chromium.JSValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;

import static com.nova.LoadConfig.Constant.EleFoundStatus.*;
import static com.nova.LoadConfig.MyConst.*;
import static com.nova.PrintLog.PrintLog.*;

public class UiUtils {
    //一些工具类方法
    public static void setWithCheck(Object eleObj, Object Value) {
        if (Value != null) {
            if (eleObj instanceof TextField) {
                //输入文本框的类型
                TextField textField = (TextField) eleObj;
                textField.setText((String) Value);
            } else if (eleObj instanceof CheckBox){
                //勾选框的类型
                CheckBox checkBox = (CheckBox) eleObj;
                checkBox.setSelected((Boolean) Value);
            }  else if (eleObj instanceof ComboBox) {
                // 组合框 下拉列表框的类型
                ComboBox comboBox = (ComboBox) eleObj;
                comboBox.setValue(Value);
            } else if (eleObj instanceof RadioButton) {
                //单选按钮
                RadioButton radioButton = (RadioButton) eleObj;
                radioButton.setSelected((Boolean) Value);
            } else {
                print_error(String.format("The element type is not supported yet [%s] -> [%s]",eleObj,Value));
            }
        }
    }

    public static void printlnDebugOnUIAndConsole(String appendText) {
        if (appendText != null){
            print_debug(appendText);
            //FXMLDocumentController.getInstance().appendTextToTextArea(String.format("[*] %s\n", appendText));
        }
    }

    public static void printlnInfoOnUIAndConsole(String appendText) {
        if (appendText != null){
            print_info(appendText);
            FXMLDocumentController.getInstance().appendTextToTextArea(String.format("[+] %s\n", appendText));
        }
    }

    public static void printlnErrorOnUIAndConsole(String appendText) {
        if (appendText != null){
            print_error(appendText);
            FXMLDocumentController.getInstance().appendTextToTextArea(String.format("[-] %s\n", appendText));
        }
    }

    /**
     * 通过浏览器JS代码执行器来输入元素操作
     * @param browser
     * @param locateInfo
     * @param eleFindType
     * @param inputText
     * @return
     */
    public static Constant.EleFoundStatus setInputValueByJS(Browser browser, String locateInfo, Constant.EleFindType eleFindType, String inputText) {
        Constant.EleFoundStatus eleFoundStatusAction;
        String jsCode = null;
        switch (eleFindType) {
            case CSS:
                // JavaScript code to find an element by CSS selector and set its value.
                jsCode = "function setInputValueByCSS(cssSelector, value) {" +
                        "   try {" +
                        "       var node = document.querySelector(cssSelector);" +
                        "       if (node && node instanceof HTMLElement) {" +
                        "           node.value = value;" +
                        "           var event = new Event('input', { 'bubbles': true, 'cancelable': true });" +
                        "           node.dispatchEvent(event);" +
                        "           return { success: true, message: 'Input successful.' };" +
                        "       } else {" +
                        "           return { success: false, message: 'Element not found or not an HTML element.' };" +
                        "       }" +
                        "   } catch (error) {" +
                        "       return { success: false, message: error.message };" +
                        "   }" +
                        "}" +
                        "setInputValueByCSS('" + locateInfo.replace("'", "\\'") + "', '" + inputText.replace("'", "\\'") + "');";
                break;
            case XPATH:
                // JavaScript code to find an element by XPath and set its value.
                jsCode = "function setInputValueByXPath(xpath, value) {" +
                        "   try {" +
                        "       var result = document.evaluate(xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null);" +
                        "       var node = result.singleNodeValue;" +
                        "       if (node && node instanceof HTMLElement) {" +
                        "           node.value = value;" +
                        "           var event = new Event('input', { 'bubbles': true, 'cancelable': true });" +
                        "           node.dispatchEvent(event);" +
                        "           return { success: true, message: 'Input successful.' };" +
                        "       } else {" +
                        "           return { success: false, message: 'Element not found or not an HTML element.' };" +
                        "       }" +
                        "   } catch (error) {" +
                        "       return { success: false, message: error.message };" +
                        "   }" +
                        "}" +
                        "setInputValueByXPath('" + locateInfo.replace("'", "\\'") + "', '" + inputText.replace("'", "\\'") + "');";
                break;
            case ID:
            case NAME:
            case CLASS:
            default:
                printlnErrorOnUIAndConsole("Js Mode Not Support [id|name|class] Mode!!! Please Input [CSS] or [XPATH] Locate Info Again.");
                break;
        }

        if (jsCode == null){
            printlnErrorOnUIAndConsole(String.format("Js Mode Only Support [css|xpath] Mode!!! You Input Mode is [%s]", eleFindType));
            return BREAK;
        }

        try {
            // Execute the JavaScript code in the context of the currently loaded web page and get the return value.
            JSValue jsValue = browser.executeJavaScriptAndReturnValue(jsCode);
            // Check if the returned JSValue is an object and contains expected properties.
            if (jsValue.isObject()) {
                JSValue success = jsValue.asObject().getProperty("success");
                JSValue message = jsValue.asObject().getProperty("message");
                if (success.isBoolean() && message.isString()) {
                    JSBoolean isSuccess = success.asBoolean();
                    // Print the message for debugging purposes.
                    // System.out.println(isSuccess.getValue(), msg);
                    if (isSuccess.getValue()) {
                        //定位并输入元素成功
                        eleFoundStatusAction = SUCCESS;
                    } else {
                        eleFoundStatusAction = GLOBAL_FIND_ELE_NULL_ACTION;
                        String msg = message.asString().getValue();
                        printlnErrorOnUIAndConsole(String.format("定位元素失败 (影响结果false) 动作:[%s] MSG[%s]", eleFoundStatusAction, msg));
                    }
                } else {
                    eleFoundStatusAction = GLOBAL_FIND_ELE_NULL_ACTION;
                    String msg = message.asString().getValue();
                    printlnErrorOnUIAndConsole(String.format("定位元素失败 (响应格式非预期) 动作:[%s] MSG[%s]", eleFoundStatusAction, msg));
                }
                return eleFoundStatusAction;
            }

            // If we reach here, something unexpected happened.
            eleFoundStatusAction = GLOBAL_FIND_ELE_NULL_ACTION;
            printlnErrorOnUIAndConsole(String.format("未知定位异常 (JS执行结果格式未知) 动作:[%s]", eleFoundStatusAction));
        } catch (Exception e){
            // If we reach here, something unexpected happened.
            eleFoundStatusAction = CONTINUE;
            printlnErrorOnUIAndConsole(String.format("未知定位异常 (JS执行发生未知错误) 动作:[%s] ERROR:[%s]", eleFoundStatusAction, e.getMessage()));
        }
        return eleFoundStatusAction;
    }
}
