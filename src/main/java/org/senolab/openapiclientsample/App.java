package org.senolab.openapiclientsample;

import com.akamai.edgegrid.signer.exceptions.RequestSigningException;
import org.senolab.openapiclientsample.model.OpenAPICallService;

import java.io.IOException;

public class App {


    public static void main(String[] args) {
        //Currently work only for GET request at the moment
        //TODO: support POST, PUT, DELETE
        //TODO: accept HTTP request header key/val pair as argument
        //TODO: accept HTTP Request Body as argument
        if(args.length == 3) {
            switch (args.length) {
                case 3:
                    try {
                        OpenAPICallService apiCallService = new OpenAPICallService(args[0], args[1], args[2]);
                        apiCallService.execute();
                    } catch (IOException e) {
                        System.out.println("Something wrong during I/O process");
                        e.printStackTrace();
                    } catch (RequestSigningException e) {
                        System.out.println("Something wrong during request signing process");
                        e.printStackTrace();
                    }
                    break;
            }

        } else {
            System.out.println("This CLI take 3 arguments: \n" +
                    "1) The .edgerc file containing all the tokens and secret\n" +
                    "2) The HTTP method\n" +
                    "3) URL path of the API call (not including hostname and protocol) surrounded with double-quotes\n" +
                    "\n" +
                    "Sample command:\n" +
                    "$java -jar openapiclientsample-1.0-SNAPSHOT.jar /home/user/.edgerc GET \"/gtm-api/v1/reports/domain-list\"");
        }
    }
}
