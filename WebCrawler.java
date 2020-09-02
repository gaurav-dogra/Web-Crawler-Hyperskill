package crawler;

import javax.swing.*;

public class WebCrawler extends JFrame {

    public WebCrawler() {
        setTitle("Simple Window");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);
        setLayout(null);
        setLocationRelativeTo(null);

        JTextField textField = new JTextField();
        textField.setBounds(6, 6, 250, 15);
        textField.setName("UrlTextField");
        add(textField);

        JButton button = new JButton("Get text!");
        button.setName("RunButton");
        button.setBounds(260, 6, 110, 20);
        add(button);

        JLabel label = new JLabel();
        label.setName("TitleLabel");
        label.setBounds(6, 30, 350, 20);
        add(label);

        JTextArea textArea = new JTextArea();
        textArea.setName("HtmlTextArea");
        textArea.setEnabled(false);
        textArea.setBounds(6, 45, 366, 388);
        add(textArea);
        setVisible(true);

        button.addActionListener(new Listener(textField, button, label, textArea));
        setVisible(true);
    }
}