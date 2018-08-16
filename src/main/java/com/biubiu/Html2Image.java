package com.biubiu;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.IOException;

/**
 * 需要先安装phantomjs
 * @See {https://blog.csdn.net/qq_33544988/article/details/80390198}
 * Created by Haibiao.Zhang on 2018/8/16.
 */
public class Html2Image {

    public static void main(String[] args) throws IOException {
        PhantomJSDriver driver = null;
        try {
            //设置必要参数
            DesiredCapabilities dcaps = new DesiredCapabilities();
            //ssl证书支持
            dcaps.setCapability("acceptSslCerts", true);
            //截屏支持
            dcaps.setCapability("takesScreenshot", true);
            //设置头
            dcaps.setCapability("phantomjs.page.settings.userAgent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.112 Safari/537.36");
            //css搜索支持
            dcaps.setCapability("cssSelectorsEnabled", true);
            //js支持
            dcaps.setJavascriptEnabled(true);
            //驱动支持
            dcaps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, "D:\\Downloads\\phantomjs-2.1.1-windows\\bin\\phantomjs.exe");
            //创建无界面浏览器对象

            driver = new PhantomJSDriver(dcaps);
            driver.get("file:///D:/Documents/WeChat%20Files/zhang201330340631/Files/contract.html");

            int size = 1;
            for (int i = 0; i <= size; i++) {
                driver.executeScript("window.scrollBy(0, 2000);");//触发下拉刷新
                Thread.sleep(50);
                System.out.println(i);
            }
            File scrFile = driver.getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(scrFile, new File("e:/baidu_selenium.png"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            driver.close();//关闭浏览器
            driver.quit();//注意调用 不quit会有多个driver进程
        }
    }

}
