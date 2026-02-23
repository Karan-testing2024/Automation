package com.daily.test;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.io.FileHandler;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Daily_test {

    @Test
    public void checkWebsite() throws Exception {

        
        URL url = new URL("https://www.koyambedumarket.in/");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();

        int statusCode = connection.getResponseCode();
        System.out.println("Status Code: " + statusCode);

        
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        driver.get("https://www.koyambedumarket.in/");
        driver.manage().window().maximize();

        try {

            
            Assert.assertEquals(statusCode, 200, "Website is DOWN!");

            
            String title = driver.getTitle();
            System.out.println("Page Title: " + title);
            Assert.assertTrue(title.contains("Koyambedu"), "Title mismatch!");

            
            String path = takeScreenshot(driver, "Success");
            sendEmail("Status code:" +statusCode +" --- Website is working fine ", path);

        } catch (AssertionError e) {

            
            String path = takeScreenshot(driver, "Failure");
            sendEmail("Status code:"+statusCode +"Website is not working!....please check", path);
            throw e;

        } finally {
            driver.quit();
        }
    }

   
    public String takeScreenshot(WebDriver driver, String name) throws Exception {

        TakesScreenshot ts = (TakesScreenshot) driver;
        File source = ts.getScreenshotAs(OutputType.FILE);

        File folder = new File("./screenshots");
        if (!folder.exists()) {
            folder.mkdir();
        }

        String path = "./screenshots/" + name + ".png";
        FileHandler.copy(source, new File(path));

        return path;
    }

    
    public void sendEmail(String messageText, String attachmentPath) throws Exception {

        String from = "karan.bcom2024@gmail.com";     
        String password = "qxkq qpzt iemw cgbh";     
        String to = "karan.bcom2024@gmail.com";          

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

        MimeMultipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(filePart);

        message.setContent(multipart);

        Transport.send(message);

        System.out.println("Email Sent Successfully ");
    }
}