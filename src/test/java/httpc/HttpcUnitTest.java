package httpc;

import org.junit.jupiter.api.Test;

public class HttpcUnitTest {

    @Test
    public void mixedOptionsAndParametersShouldBeFilteredForHttpcCommand() {
        String str = "http://ip.jsontest.com";

        Httpc httpc = new Httpc();

        httpc.get(null,null,null,null, str);

    }
}

