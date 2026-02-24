package com.daily.test;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.io.FileHandler;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.*;

public class Daily_test {

    @Test
    public void checkWebsite() throws Exception {

        String website = "https://www.koyambedumarket.in/";

        // 🔹 Check HTTP Status Code
        URL url = new URL(website);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        int statusCode = connection.getResponseCode();
        System.out.println("Status Code: " + statusCode);

        // 🔹 Setup Headless Chrome for Jenkins
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        driver.get(website);

        try {

            // 🔹 Validate Status Code
            Assert.assertEquals(statusCode, 200, "Website is DOWN!");

            // 🔹 Validate Title
            String title = driver.getTitle();
            System.out.println("Page Title: " + title);
            Assert.assertTrue(title.contains("Koyambedu"), "Title mismatch!");

            // 🔹 Take Screenshot
            String path = takeScreenshot(driver, "Success");

            // 🔹 Send Success Email
            sendEmail("Status Code: " + statusCode + " - Website is working fine.", path);

        } catch (AssertionError e) {

            String path = takeScreenshot(driver, "Failure");
            sendEmail("Status Code: " + statusCode + " - Website is DOWN! Please check immediately.", path);
            throw e;

        } finally {
            driver.quit();
        }
    }

    // 📸 Screenshot Method
    public String takeScreenshot(WebDriver driver, String name) throws Exception {

        TakesScreenshot ts = (TakesScreenshot) driver;
        File source = ts.getScreenshotAs(OutputType.FILE);

        File folder = new File("screenshots");
        if (!folder.exists()) {
            folder.mkdir();
        }

        String path = "screenshots/" + name + "_" + System.currentTimeMillis() + ".png";
        FileHandler.copy(source, new File(path));

        return path;
    }

    // 📧 Email Method
    public void sendEmail(String messageText, String attachmentPath) throws Exception {

        String from = "karan.bcom2024@gmail.com";  
        String password = "qxkqqpztiemwcgbh";  
        String to = "karan.bcom2024@gmail.com";

        System.out.println("Preparing to send email...");

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(from, password);
                    }
                });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(to));
        message.setSubject("Daily Website Monitoring Report");

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(messageText);

        MimeBodyPart filePart = new MimeBodyPart();
        filePart.attachFile(new File(attachmentPath));

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(filePart);

        message.setContent(multipart);

        Transport.send(message);

        System.out.println("Email Sent Successfully ✅");
    }
}