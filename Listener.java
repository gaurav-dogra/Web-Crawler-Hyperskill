package crawler;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Listener implements ActionListener {
    private final static Logger LOGGER =
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private JTextArea textArea;
    private JTextField textField;
    private JButton button;
    private JLabel label;

    public Listener(JTextField textField, JButton button,
                    JLabel label, JTextArea textArea) {
        this.textField = textField;
        this.button = button;
        this.label = label;
        this.textArea = textArea;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        LOGGER.setLevel(Level.INFO);
        final String url = textField.getText();
        try (InputStream inputStream = new BufferedInputStream(new URL(url).openStream())) {
            String siteText = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            process(siteText);
        } catch (IOException e) {
            LOGGER.severe("Not able to access the URL");
        }
    }

    public void process(String text) {
        System.out.println(text);
        setLabel(text);
        setTextArea(text);
    }

    private void setLabel(String text) {
        label.setText(getTitle(text));
    }

    private String getTitle(String text) {
        String patternString = "<title>(.*)<\\/title>";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
             return matcher.group(1);
        } else {
            return "";
        }
    }

    private void setTextArea(String text) {
        textArea.setText(text);
    }

}
