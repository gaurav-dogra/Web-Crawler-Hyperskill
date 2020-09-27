package crawler;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebCrawler extends JFrame {
    private final List<String> urls = new ArrayList<>();
    private final List<String> titles = new ArrayList<>();
    private final WebCrawlerTableModel model = new WebCrawlerTableModel(urls, titles);
    private final JTable table = new JTable(model);
    private final JTextField urlTextField = new JTextField(40);
    private final JButton runButton = new JButton("Parse");
    private final JLabel titleLabel = new JLabel();
    private final JTextField exportUrlTextField = new JTextField(40);
    private final JButton exportButton = new JButton("Save");

    public WebCrawler() {
        setFieldSettings();
        setPanels();
        initialize();
    }

    private void setFieldSettings() {
        table.setEnabled(false);
        table.setName("TitlesTable");
        urlTextField.setName("UrlTextField");
        titleLabel.setName("TitleLabel");
        runButton.setName("RunButton");
        exportUrlTextField.setName("ExportUrlTextField");
        exportButton.setName("ExportButton");
        runButton.addActionListener(ae -> buttonAction());
        exportButton.addActionListener(ae -> saveAction());
    }

    private void buttonAction() {
        final String url = urlTextField.getText();
        System.out.println("url = " + url);
        String siteText = "";
        try (InputStream inputStream = new BufferedInputStream(new URL(url).openStream())) {
            siteText = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        titleLabel.setText(parseTitle(siteText));
        addOriginalUrlToTable(url);
        addurls(siteText);
        addTitles();
        deleteMissingTitles();
        model.fireTableDataChanged();
    }

    private void saveAction() {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(exportUrlTextField.getText()), StandardCharsets.UTF_8))) {
            for (int i = 0; i < urls.size(); i++) {
                writer.write(urls.get(i));
                writer.newLine();
                writer.write(titles.get(i));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setPanels() {
        Container pane = this.getContentPane();
        pane.removeAll();
        pane.setLayout(new GridBagLayout());
        this.getRootPane().setDefaultButton(runButton);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.fill = GridBagConstraints.HORIZONTAL;

        constraints.weightx = 0.5;
        constraints.gridwidth = 1;
        constraints.gridy = 0;
        constraints.gridx = 0;
        pane.add(new JLabel("URL: "), constraints);

        constraints.gridy = 0;
        constraints.gridx = 1;
        pane.add(urlTextField, constraints);

        constraints.gridy = 0;
        constraints.gridx = 2;
        pane.add(runButton, constraints);

        constraints.gridy = 1;
        constraints.gridx = 0;
        pane.add(new JLabel("Title: "), constraints);

        constraints.gridy = 1;
        constraints.gridx = 1;
        pane.add(titleLabel, constraints);

        constraints.gridy = 2;
        constraints.gridx = 0;
        constraints.gridwidth = 3;
        constraints.weightx = 1;
        pane.add(getScrollPane(), constraints);

        constraints.gridwidth = 1;
        constraints.gridy = 3;
        constraints.gridx = 0;
        constraints.weightx = 0.5;
        pane.add(new JLabel("Export: "), constraints);

        constraints.gridy = 3;
        constraints.gridx = 1;
        pane.add(exportUrlTextField, constraints);

        constraints.gridy = 3;
        constraints.gridx = 2;
        pane.add(exportButton, constraints);

    }

    private JScrollPane getScrollPane() {
        table.setEnabled(false); // ? already false in constructor
        JScrollPane scroller = new JScrollPane(table);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        return scroller;
    }

    private void initialize() {
        setTitle("Web Crawler");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private String parseTitle(String text) {
        Pattern pattern = Pattern.compile("<title>(.+)</title>");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "Not Found";
        }
    }

    private void addOriginalUrlToTable(String url) {
        urls.clear();
        urls.add(url);
    }

    private void addurls(String siteText) {
        Pattern pattern = Pattern.compile("(?i)<a\\s+(?:[^>]*?\\s+)?href=([\"'])(.*?)\\1");
        Matcher matcher = pattern.matcher(siteText);
        String initialUrl = urls.get(0);
        String protocol = initialUrl.substring(0, initialUrl.indexOf("//"));
        Matcher baseUrlMatch = Pattern.compile("^.+/").matcher(initialUrl);
        baseUrlMatch.find(); // I don't get this
        String baseUrl = baseUrlMatch.group();
        StringBuilder url;
        while (matcher.find()) {
            url = new StringBuilder();
            String text = matcher.group(2);
            if (text.startsWith("#")) {
                continue;
            }
            if (!text.startsWith("http")) {
                if (text.startsWith("//")) {
                    url.append(protocol).append(text);
                } else if (text.startsWith("/")) { // I don't get this
                    url.append(initialUrl).append(text);
                } else {
                    url.append(baseUrl).append(text);
                }
            } else {
                url.append(text);
            }
            urls.add(url.toString());
        }
        System.out.printf("added %d urls form %s\n", urls.size(), urls.get(0));
    }

    private void addTitles() {
        titles.clear();
        String siteText = "";
        for (String url : urls) {
            try {
                URLConnection conn = new URL(url).openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0");
                int code = ((HttpURLConnection) conn).getResponseCode();
                if (code == 404 || !conn.getContentType().startsWith("text/html")) {
                    titles.add("Not Found");
                    continue;
                }
                try (InputStream is = new BufferedInputStream(conn.getInputStream())) {
                    siteText = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            titles.add(parseTitle(siteText));
        }
        System.out.printf("added %d titles from %s\n", titles.size(), titles.get(0));
    }

    private void deleteMissingTitles() {
        int j = 0;
        for (int i = 0; i < urls.size(); i++) {
            if (!titles.get(i).equals("Not Found")) {
                urls.set(j, urls.get(i));
                titles.set(j++, titles.get(i));
            }
        }
        updateLists(j, urls);
        updateLists(j, titles);
    }

    private void updateLists(int j, List<String> list) {
        List<String> t = new ArrayList<>(list);
        list.clear();
        list.addAll(t.subList(0,j));
    }

}