/*
  Author: Razine Ahmed Bensari
  COMP445 –Winter 2020
  Data Communications & Computer Networks
  Lab Assignment # 1
  Due Date: Sunday, Feb9, 2020 by 11:59PM
  */
package RequestAndResponse;

import java.io.File;

public class Response {

    public File getResponseFile() {
        return responseFile;
    }

    public void setResponseFile(File responseFile) {
        this.responseFile = responseFile;
    }

    private File responseFile;
}
