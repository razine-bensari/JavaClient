package httpc;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class HttpcUnitTest {

    @Test
    public void mixedOptionsAndParametersShouldBeFilteredForHttpcCommand() {
        String str = "http://httpbin.org/ip";

        Httpc httpc = new Httpc();

        httpc.get(null,null,null,null, str);



    }

    @Test
    public void httpcCommandGetWithOneHeader() {
        String str = "http://httpbin.org/ip";

        Httpc httpc = new Httpc();

        String[] headers = {"header1:value1"};

        httpc.get(headers,null,null,null, str);
    }

    @Test
    public void httpcCommandGetWithThreeHeader() {
        String str = "http://httpbin.org/ip";

        Httpc httpc = new Httpc();

        String[] headers = {"header1:value1", "header2:value2", "header3:value3"};

        httpc.get(headers,null,null,null, str);

    }

    @Test
    public void httpcCommandGetWithQuery() {
        String str = "http://httpbin.org/ip";

        Httpc httpc = new Httpc();

        String[] headers = {"header1:value1", "header2:value2", "header3:value3"};

        String[] query = {"query1=value1"};

        httpc.get(headers,null,query,null, str);

    }

    @Test
    public void httpcCommandPostWithQuery() {
        String str = "http://httpbin.org/ip";

        Httpc httpc = new Httpc();

        String[] headers = {"header1:value1", "header2:value2", "header3:value3"};

        String[] query = {"query1=value1"};

        httpc.post(null,  null, headers,null, query, null, str);

    }

    @Test
    public void httpcCommandPostWithSeveralQuery() {
        String str = "http://httpbin.org/ip";

        Httpc httpc = new Httpc();

        String[] headers = {"header1:value1", "header2:value2", "header3:value3"};

        String[] query = {"query1=value1", "query2=value2"};

        httpc.post(null,  null, headers,null, query, null, str);

    }

    @Test
    public void httpcCommandPostWithSeveralQueryAndUrlQuery() {
        String str = "http://httpbin.org/ip?queryInURL=valueInUrl";

        Httpc httpc = new Httpc();

        String[] headers = {"header1:value1", "header2:value2", "header3:value3"};

        String[] query = {"query1=value1", "query2=value2"};

        httpc.post(null,  null, headers,null, query, null, str);

    }

    @Test
    public void httpcCommandPostWithFileAndBody() {
        String str = "http://httpbin.org/ip?queryInURL=valueInUrl";

        Httpc httpc = new Httpc();

        String filename = "file.txt";

        String[] headers = {"header1:value1", "header2:value2", "header3:value3"};

        String[] query = {"query1=value1", "query2=value2"};

        httpc.post("thisissbody",  filename, headers,null, query, null, str);
    }

    @Test
    public void temp() {
        try{
            String fileBody = "file.txt";
            File file = Paths.get(fileBody).toFile();
            System.out.println("This is the path: " + Paths.get(fileBody).toString());
            if(!file.exists()){
                System.out.println("File not found!");
                System.exit(1);
            }
            ArrayList<String> linesFromFile = (ArrayList<String>) Files.readAllLines(Paths.get(fileBody), StandardCharsets.UTF_8);
            StringBuilder str = new StringBuilder();
            for (String line : linesFromFile) {
                str.append(line);
            }
            String testFromFile = str.toString();
            System.out.println(testFromFile);
        } catch (Exception e ) {
            System.out.println(e.getCause() + e.getMessage());
        }
    }
}

