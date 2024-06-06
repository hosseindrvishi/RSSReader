import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class RSSreader {
    private static final int MAX_ITEMS = 5;
    public static int k;

    public static void main(String[] args) throws Exception {
        System.out.println("Welcome to RSS reader!");
        while (k != 4) {
            guide();
        }
    }

    public static String extractPageTitle(String html) {
        Document doc = null ;
        try {
            Document doc = jsoup.parse(html);
            return doc.select("title").first().text();
        } catch (Exception e) {
            return "Error: no title tag found in page source!";
        }
    }

    public static void retrieveRssContent(String rssUrl) {
        try {
            String rssXml = fetchPageSource(rssUrl);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            ByteArrayInputStream input = new ByteArrayInputStream(rssXml.getBytes(StandardCharsets.UTF_8));
            org.w3c.dom.Document doc = documentBuilder.parse(input);
            NodeList itemNodes = doc.getElementsByTagName("item");
            for (int i = 0; i < MAX_ITEMS; ++i) {
                Node itemNode = itemNodes.item(i);
                if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) itemNode;
                    System.out.println("Title: " + element.getElementsByTagName("title").item(0).getTextContent());
                    System.out.println("Link: " + element.getElementsByTagName("link").item(0).getTextContent());
                    System.out.println("Description: " + element.getElementsByTagName("description").item(0).getTextContent());
                }
            }
        } catch (Exception e) {
            System.out.println("Error in retrieving RSS content for " + rssUrl + ": " + e.getMessage());
        }
    }

    public static String extractRssUrl(String url) throws IOException {
        org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
        return doc.select("[type='application/rss+xml']").attr("abs:href");
    }

    public static String fetchPageSource(String urlString) throws Exception {
        URI uri = new URI(urlString);
        URL url = uri.toURL();
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36");
        return toString(urlConnection.getInputStream());
    }

    private static String toString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String inputLine;
        StringBuilder stringBuilder = new StringBuilder();
        while ((inputLine = bufferedReader.readLine()) != null) stringBuilder.append(inputLine);
        return stringBuilder.toString();
    }

    public static void guide() throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Type a valid number for your desired action: \n" + "[1] Show updates \n" + "[2] Add URL \n" + "[3] Remove URL \n" + "[4] Exit");
        k = scanner.nextInt();
        if (k == 1) {
            showUpdates();
        } else if (k == 2) {
            addUrl();
        } else if (k == 3) {
            removeUrl();
        }
    }
    public static void showUpdates() throws FileNotFoundException {
        Scanner scanner = new Scanner(System.in);
        File file = new File("data.txt");
        Scanner fileScanner = new Scanner(file);
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        int n = 1;
        System.out.println("Show updates for:" + "\n" + "[0] " + "All websites");
        String line;
        try {
            List<String> list = new ArrayList<>();
            while ((line = bufferedReader.readLine()) != null) {
                String[] lineSplit = line.split(";");
                System.out.printf("[" + n + "]" + "\t" + lineSplit[0] + "\n");
                n++;
            }
            System.out.println("Enter -1 to return");
            int k = scanner.nextInt();
            if (k == -1) {
                guide();

            }else if (k==0) {
                int i=0;
                while (fileScanner.hasNextLine()){
                    list.add(fileScanner.nextLine());
                    String chosenLine = list.get(i);
                    String[] rssPart = chosenLine.split(";");
                    retrieveRssContent(rssPart[2]);
                    i++;
                }
            } else {
                while (fileScanner.hasNextLine()) {
                    list.add(fileScanner.nextLine());
                }
                String chosenLine = list.get(k - 1);
                String[] rssPart = chosenLine.split(";");
                retrieveRssContent(rssPart[2]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    public static void addUrl() throws IOException {
        System.out.println("Please enter website URL to add:");
        Scanner scanner = new Scanner(System.in);
        File file = new File("data.txt");
        Scanner fileScanner = new Scanner(file);
        String url = scanner.next();
        try {
            List<String> list = new ArrayList<>();
            while (fileScanner.hasNextLine()) {
                list.add(fileScanner.nextLine());
            }
            if (list.contains(extractPageTitle(fetchPageSource(url)) + ";" + url + ";" + extractRssUrl(url))) {
                System.out.println(url + " already exists");
            } else {
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true));
                bufferedWriter.write(extractPageTitle(fetchPageSource(url)) + ";" + url + ";" + extractRssUrl(url)+"\n");

                System.out.println("Added " + url + " successfully.");
                bufferedWriter.close();

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static void removeUrl() throws IOException {
        System.out.println("Please enter website URL to remove:");
        Scanner scanner = new Scanner(System.in);
        File file = new File("data.txt");
        Scanner fileScanner = new Scanner(file);
        String url = scanner.next();
        int i = 0;
        try {
            List<String> list = new ArrayList<>();

            while (fileScanner.hasNextLine()) {
                list.add(fileScanner.nextLine());
            }
            if (list.contains(extractPageTitle(fetchPageSource(url)) + ";" + url + ";" + extractRssUrl(url))) {
                file.delete();
                File file2 = new File("data.txt");
                FileWriter fileWriter=new FileWriter(file2);

                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file2, true));
                while (i < list.size()) {
                    String s= list.get(i);
                    if (!(s.equals(extractPageTitle(fetchPageSource(url)) + ";" + url + ";" + extractRssUrl(url)))) {
                        bufferedWriter.write(list.get(i)+"\n");
                    }
                    i++ ;
                }

                bufferedWriter.close();
                System.out.println("Removed " + url + " successfully.");
            }
            else {
                System.out.println("Couldn't find " + url);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
