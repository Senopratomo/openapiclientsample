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
                switch(args[5]) {
                    case "json-only":
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
                        break;
                    default:
                        String[] repetitionInterval = args[5].split(",", 2);
                        int reps = Integer.parseInt(repetitionInterval[0]);
                        int sleepPerInterval = Integer.parseInt(repetitionInterval[1]) * 1000;
                        for(int i=0; i < reps; i++) {
                            System.out.println(args[1]+" "+args[2]);
                            if (args[3].equalsIgnoreCase("-") && args[4].equalsIgnoreCase("-")) {
                                OpenAPICallService apiCallService = new OpenAPICallService(args[0], args[1], args[2]);
                                apiCallService.execute();
                            } else {
                                OpenAPICallService apiCallService = new OpenAPICallService(args[0], args[1], args[2], args[3], args[4]);
                                apiCallService.execute();
                            }
                            System.out.println("");
                            Thread.sleep(sleepPerInterval);
                        }
                        break;
                }


            } else {
                System.out.println("OpenAPIClient v1.3.5 \n\n"
                        + "This CLI takes 3 - 6 arguments separated by a single space depends on the API call and options that you require:  \n"
                        + "args[0] is location of .edgerc file. This file contain Akamai API client credentials (client token, \n"
                        + "access token, secret, host) which necessary for EdgeGrid lib \n"
                        + "sample: \n"
                        + "[default] \n"
                        + "host = https://akab-xxxxx.luna.akamaiapis.net \n"
                        + "client_token = akab-xxxxx \n"
                        + "client_secret = xxxxx \n"
                        + "access_token = xxxx \n"
                        + "\n"
                        + "args[1] is HTTP method \n"
                        + "args[2] is path to endpoint enclosed in double quotes (eg: \"/api/v2/somepath?q1=1\") \n"
                        + "args[3] is a json file containing list of HTTP headers that you want to pass on to the API call in JSON format. If there is no extra header being added, put \"-\" \n"
                        + "the key will be HTTP header name and the value will be value of that header \n"
                        + "     sample1: header.json to send \"Content-Type\" header with value \"application/json\" in API call \n"
                        + "     {\"Content-Type\":\"application/json\"} \n"
                        + "		sample2: headers.json to send \"Content-Type\" header with value \"application/json\" and also \"If-Match\" header with value of \"6aed418629b4e5c0\" \n"
                        + "		{\"Content-Type\":\"application/json\",\"If-Match\":\"6aed418629b4e5c0\"} \n"
                        + "args[4] (for PUT and POST) is path to the HTTP request body. If there is no body being pass, put \"-\" \n"
                        + "args[5] is additional option argument and you can specify one of these options:\n"
                        + " - \"json-only\" --> modify the output of this app to return only JSON response body.\n"
                        + " - \"x,x\" --> 2 comma-separated number. This option is to perform repetitive call. \n"
                        + "      The first number is # of repetition of the API call. The second number is the number of second the API client will wait between each call.\n"
                        + "      See example usage scenario below"
                        + "\n"
                        + "Sample usage of this tool:\n"
                        + "Scenario #1: API call to list all available ghost location - https://developer.akamai.com/api/luna/diagnostic-tools/resources.html#getghostavailable \n"
                        + "$java -jar OpenAPIClient.jar /home/user1/.edgerc GET \"/diagnostic-tools/v2/ghost-locations/available\" \n"
                        + "\n"
                        + "Scenario #2: API call to perform Fast invalidate by URL in Production - https://developer.akamai.com/api/purge/ccu/resources.html#postinvalidateurl \n"
                        + "$java -jar OpenAPIClient.jar /home/user1/mytokens.txt POST \"/ccu/v3/invalidate/url/production\" /home/user1/headers.json /home/user1/body.json  \n"
                        + "\n"
                        + "Scenario #3: Reporting API call to get details definition of 'bytes-by-ip' report  - https://developers.akamai.com/api/core_features/reporting/bytes-by-ip.html#getreportdetails \n"
                        + "but user only want to see JSON response from endpoint (without additional verbose).\n"
                        + "$java -jar OpenAPIClient.jar /home/user1/mytokens.txt GET \"/reporting-api/v1/reports/bytes-by-ip/versions/1\" \"-\" \"-\" \"json-only\"  \n"
                        + "\n"
                        + "Scenario #4: Perform 10 times PAPI 'list contracts' API call with 5 seconds wait between each call  - https://developers.akamai.com/api/core_features/property_manager/v1.html#getcontracts \n"
                        + "$java -jar OpenAPIClient.jar /home/user1/mytokens.txt GET \"/papi/v1/contracts\" \"-\" \"-\" \"10,5\"\n\n"
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
        } catch (InterruptedException e) {
            System.out.println("Something wrong during thread execution! Please try again");
            e.printStackTrace();
        }
    }
}
