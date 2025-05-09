package com.fuping;


import com.fuping.LoadConfig.MyConst;
import com.teamdev.jxbrowser.chromium.ProductInfo;
import com.teamdev.jxbrowser.chromium.ay;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;

import static com.fuping.LoadConfig.MyConst.globalProgramVersion;
import static com.fuping.PrintLog.PrintLog.print_debug;

public class jxBrowserBrute extends Application {

    //静态代码块 用于破解 jxBrowser 6.15
    static {
        try {
            Field e = ay.class.getDeclaredField("e");
            e.setAccessible(true);
            Field f = ay.class.getDeclaredField("f");
            f.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(e, e.getModifiers() & ~Modifier.FINAL);
            modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
            e.set(null, new BigInteger("1"));
            f.set(null, new BigInteger("1"));
            modifiersField.setAccessible(false);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Parent root = FXMLLoader.load(CrackCaptchaLogin.class.getResource("FXMLDocument.fxml"));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLDocument.fxml"));
        loader.setControllerFactory(param -> FXMLDocumentController.getInstance());
        Parent root = loader.load();

        primaryStage.setScene(new Scene(root, 1000, 700));
        primaryStage.setTitle(String.format("jxBrowserBrute  (%s)", globalProgramVersion));
        // 确保 Stage 是可调整大小的
        primaryStage.setResizable(true);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
        primaryStage.show();
    }

    public static void main(String[] args) {
        print_debug("Inner JxBrowser Version: " + ProductInfo.getVersion());
        //初始化配置文件读取
        MyConst.initialize();
        //启动UI
        launch(args);
    }
}
