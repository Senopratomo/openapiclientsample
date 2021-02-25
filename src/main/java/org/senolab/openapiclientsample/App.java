package org.senolab.openapiclientsample;

import com.akamai.edgegrid.signer.exceptions.RequestSigningException;
import com.google.api.client.http.HttpResponseException;
import org.json.simple.parser.ParseException;
import org.senolab.openapiclientsample.model.OpenAPICallService;
import org.senolab.openapiclientsample.model.OpenAPICallServiceJSONOnly;

import java.io.IOException;

public class App {

    public static void main(String[] args) {
        try {
            if (args.length == 3) {
                OpenAPICallService apiCallService = new OpenAPICallService(args[0], args[1], args[2]);
                apiCallService.execute();
            } else if (args.length == 4) {
                OpenAPICallService apiCallService = new OpenAPICallService(args[0], args[1], args[2], args[3]);
                apiCallService.execute();
            } else if (args.length == 5) {
                OpenAPICallService apiCallService = new OpenAPICallService(args[0], args[1], args[2], args[3], args[4]);
                apiCallService.execute();
            } else if (args.length == 6) {
                if (args[3].equalsIgnoreCase("-") && args[4].equalsIgnoreCase("-") && args[5].equalsIgnoreCase("json-only")) {
                    OpenAPICallServiceJSONOnly apiCallService = new OpenAPICallServiceJSONOnly(args[0], args[1], args[2]);
                    apiCallService.execute();
                } else if (args[4].equalsIgnoreCase("-") && args[5].equalsIgnoreCase("json-only")) {
                    OpenAPICallServiceJSONOnly apiCallService = new OpenAPICallServiceJSONOnly(args[0], args[1], args[2], args[3]);
                    apiCallService.execute();
                } else if (args[5].equalsIgnoreCase("json-only")) {
                    OpenAPICallServiceJSONOnly apiCallService = new OpenAPICallServiceJSONOnly(args[0], args[1], args[2], args[3], args[4]);
                    apiCallService.execute();
                }

            } else {
                System.out.println("OpenAPIClient v1.0.1 \n\n"
                        + "This CLI takes 3 - 6 arguments separated by a single space depends on the API call and options that you require:  \n"
                        + "args[0] is location of .edgerc file. This file contain Akamai API client credentials (client token, \n"
                        + "access token, secret, host, and max body size) which necessary for EdgeGrid lib \n"
                        + "sample: \n"
                        + "[default] \n"
                        + "host = https://akab-xxxxx.luna.akamaiapis.net \n"
                        + "client_token = akab-xxxxx \n"
                        + "client_secret = xxxxx \n"
                        + "access_token = xxxx \n"
                        + "max-body = 131072 \n"
                        + "args[1] is HTTP method \n"
                        + "args[2] is path to endpoint enclosed in double quotes (eg: \"/api/v2/somepath?q1=1\") \n"
                        + "args[3] is a json file containing list of HTTP headers that you want to pass on to the API call in JSON format. If there is no extra header being added, put \"-\" \n"
                        + "the key will be HTTP header name and the value will be value of that header \n"
                        + "     sample1: header.json to send \"Content-Type\" header with value \"application/json\" in API call \n"
                        + "     {\"Content-Type\":\"application/json\"} \n"
                        + "		sample2: headers.json to send \"Content-Type\" header with value \"application/json\" and also \"If-Match\" header with value of \"6aed418629b4e5c0\" \n"
                        + "		{\"Content-Type\":\"application/json\",\"If-Match\":\"6aed418629b4e5c0\"} \n"
                        + "args[4] (for PUT and POST) is path to the HTTP request body. If there is no body being pass, put \"-\" \n"
                        + "args[5] is modify the output of this app to return only JSON response body. Please type \"json-only\" to have this call return only JSON \n"
                        + "\n"
                        + "Sample usage of this tool:\n"
                        + "Scenario #1 : API call to list all available ghost location - https://developer.akamai.com/api/luna/diagnostic-tools/resources.html#getghostavailable \n"
                        + "$java -jar OpenAPIClient.jar /home/user1/.edgerc GET \"/diagnostic-tools/v2/ghost-locations/available\" \n"
                        + "\n"
                        + "Scenario #2: API call to perform Fast invalidate by URL in Production - https://developer.akamai.com/api/purge/ccu/resources.html#postinvalidateurl \n"
                        + "$java -jar OpenAPIClient.jar /home/user1/mytokens.txt POST \"/ccu/v3/invalidate/url/production\" /home/user1/headers.json /home/user1/body.json  \n"
                        + "any feedback or issue, feel free to email esenopra@akamai.com with subject \"OpenCLIClient - Feedback\" \n"
                        + " ");
            }
        } catch (HttpResponseException hre) {
            System.out.println("HTTP Response code: "+hre.getStatusCode());
            System.out.println("HTTP Response headers: \n"+hre.getHeaders());
            System.out.println("HTTP Response body: \n"+hre.getContent());
        } catch (ParseException e) {
            System.out.println("Unable to parse the file containing the additional headers and/or the file containing the JSON body");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Something wrong during I/O process");
            e.printStackTrace();
        } catch (RequestSigningException e) {
            System.out.println("Something wrong during request signing process");
            e.printStackTrace();
        }
    }
}
