/////////////////////////////////////////// Hi . This is Hamid's RSS Reader /////////////////////////////////////////
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import org.jsoup.Jsoup;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.NodeList;
import java.io.File;

////////////////////////////////////////////////////    MAIN     ////////////////////////////////////////////////////
public class Main {
    public static File file ;
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        file = new File("data.txt");
        while (true){
            System.out.println("Welcome to Hamid's RSS reader!\nType a valid number for your desired action:");
            System.out.println("[1] Show updates\n[2] Add URL\n[3] Remove URL\n[4] Exit");

            int menu = scanner.nextInt();

            if (menu == 1){
                String URL = scanner.next();
                AddURL(URL);
            } else if (menu == 2) {
                String URL = scanner.next();
                DeleteURL(URL);
            } else if (menu == 3) {
                ShowUpdate();
            } else if (menu == 4) {
                break;
            }
        }
    }
////////////////////////////////////////////////////    MAIN     ////////////////////////////////////////////////////


////////////////////////////////////////////////////   METHODS   ////////////////////////////////////////////////////
    public static void AddURL(String URL) throws Exception {
        // Do we have file
        if(!file.exists()){
            file.createNewFile();
        }
        // Check URL
        Scanner scanner = new Scanner(file);
        boolean UrlEx=false;
        while (scanner.hasNextLine()){
            String line1 = scanner.nextLine();
            String[] line2 = line1.split(";");
            if(line2[1].equals(URL)){
                System.out.println("Already We Have This URL");
                UrlEx=true;
                break;
            }
        }
        scanner.close();
        // Adding URL to our file
        if(UrlEx == false) {
            PrintStream printStream = new PrintStream(new FileOutputStream(file, true));
            printStream.append(extractPageTitle(fetchPageSource(URL))).append(";");printStream.append(URL).append(";");
            printStream.append(extractRssUrl(URL)).append(";").append("\n");printStream.close();
        }
    }

    public static void DeleteURL(String url) throws Exception {

        if (!file.exists()) {
            file.createNewFile();
        }
        String content = Content(file);
        Scanner scanner = new Scanner(content);
        PrintStream printStream = new PrintStream(new FileOutputStream(file, false));

        boolean UrlExist=false;
        while (scanner.hasNextLine()){
            String line = scanner.nextLine();
            String[] lineInfo = line.split(";");
            if(!(lineInfo[1].equals(url))){
                AddURL(lineInfo[1]);
            }
            else{
                UrlExist=true;
            }
        }
        scanner.close();

        if(UrlExist == false){
            System.out.println("This url doesn't exist!");
        }

    }
    public static void ShowUpdate() throws IOException {
        if(!file.exists()){
            file.createNewFile();
        }

        Scanner filescanner = new Scanner(file);
        int number=1;
        while (filescanner.hasNextLine()){
            String line = filescanner.nextLine();
            String[] lineInfo = line.split(";");
            System.out.println("["+number+"]"+" "+lineInfo[0]);
            number++;
        }
        System.out.println("press 0 and back to menu");

        Scanner input = new Scanner(System.in);
        int RssNumber = input.nextInt();
        if(RssNumber == 0){
            return;
        }
        filescanner.close();
        Scanner filescanner2 = new Scanner(file);
        for(int i=1; i<number; i++){
            if(i != RssNumber){
                filescanner2.nextLine();
            }
            else{
                String line = filescanner2.nextLine();
                String[] lineInfo = line.split(";");
                retrieveRssContent(lineInfo[2]);
            }
        }
    }
    public static String Content(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        StringBuilder content= new StringBuilder();
        while (scanner.hasNextLine()){
            content.append(STR."\{scanner.nextLine()}\n");
        }
        return content.toString();
    }



/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////// URL methods ////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static String extractPageTitle(String html)
    {
        try
        {
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            return doc.select("title").first().text();
        }
        catch (Exception e)
        {
            return "Error: no title tag found in page source!";
        }
    }

    public static void retrieveRssContent(String rssUrl)
    {
        try {
            String rssXml = fetchPageSource(rssUrl);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            StringBuilder xmlStringBuilder = new StringBuilder();
            xmlStringBuilder.append(rssXml);
            ByteArrayInputStream input = new ByteArrayInputStream(
                    xmlStringBuilder.toString().getBytes("UTF-8"));
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
            }
        catch (Exception e)
        {
            System.out.println("Error in retrieving RSS content for " + rssUrl + ": " + e.getMessage());
            }
        }
    public static String extractRssUrl(String url) throws IOException
    {
        org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
        return doc.select("[type='application/rss+xml']").attr("abs:href");
    }
    public static String fetchPageSource(String urlString) throws Exception
    {
        URI uri = new URI(urlString);
        URL url = uri.toURL();
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML , like Gecko) Chrome/108.0.0.0 Safari/537.36");
        return toString(urlConnection.getInputStream());
    }
    private static String toString(InputStream inputStream) throws IOException
    {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream , "UTF-8"));
        String inputLine;
        StringBuilder stringBuilder = new StringBuilder();
        while ((inputLine = bufferedReader.readLine()) != null)
            stringBuilder.append(inputLine);
        return stringBuilder.toString();
    }

}