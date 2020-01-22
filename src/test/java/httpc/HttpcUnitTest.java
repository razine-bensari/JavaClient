package httpc;

import org.junit.jupiter.api.Test;

import java.net.URL;

public class HttpcUnitTest {

    @Test
    public void mixedOptionsAndParametersShouldBeFilteredForHttpcCommand() {
        String str = "http://example.com/path/to/page?name=ferret&color=purple";

        try {
            URL url = new URL(str);
            System.out.println(url.getQuery());
        } catch (Exception e){
            System.out.printf("%s", e.getMessage());
        }

    }
}

