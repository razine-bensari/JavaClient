package httpc;

import org.junit.jupiter.api.Test;

public class HttpcUnitTest {

    @Test
    public void mixedOptionsAndParametersShouldBeFilteredForHttpcCommand() {
        String str = "http://httpbin.org/ip";

        Httpc httpc = new Httpc();

        httpc.get(null,null,null,null, str);

    }
}

