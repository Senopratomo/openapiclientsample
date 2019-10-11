package org.senolab.openapiclientsample.edgercutil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Edgerc {


    public static String[] extractTokens (String edgerc) {
        /**
         * creds contain credentials info in the following index within the array
         * 0 - baseUri
         * 1 - host
         * 2 - clientToken
         * 3 - accessToken
         * 4 - clientSecret
         * 5 - maxBody
         */

        String[] creds = new String[6];

        try {
            BufferedReader bf = new BufferedReader(new FileReader(edgerc));
            String reader = bf.readLine();
            while (reader != null) {
                if (reader.startsWith("[")) {
                    reader = bf.readLine();
                    continue;
                } else if(reader.length() < 5) {
                    reader = bf.readLine();
                    continue;
                }
                String[] tempArr = reader.split("=",2);
                String key = tempArr[0].replaceAll("\\s", "");
                String val = tempArr[1].replaceAll("\\s", "");
                if (key.equalsIgnoreCase("host")) {
                    creds[0] = val;
                    creds[1] = val;
                } else if (key.equalsIgnoreCase("client_token")) {
                    creds[2] = val;
                } else if (key.equalsIgnoreCase("access_token")) {
                    creds[3] = val;
                } else if (key.equalsIgnoreCase("client_secret")) {
                    creds[4] = val;
                } else if (key.equalsIgnoreCase("max-body")) {
                    creds[5] = val;
                } else {
                    System.out.println("error in file");
                }
                //System.out.println(reader);
                reader = bf.readLine();
            }
            bf.close();
            /**
             * debug section
             System.out.println("Base URI: "+baseUri);
             System.out.println("Host: "+host);
             System.out.println("Client token: "+clientToken);
             System.out.println("Access token: "+accessToken);
             System.out.println("Client secret: "+clientSecret);
             System.out.println("Max Body: "+maxBody);
             **/

        } catch (IOException ioe) {
            System.out.println("There is a problem reading your .edgerc file! Ensure the full path of the file is correct \n"
                    + "errorname: IOE");
            System.exit(0);
        } catch (Exception e) {
            System.out.println("There is a problem reading your .edgerc file! Ensure the full path of the file is correct \n"
                    + "errorname: e");
            System.exit(0);
        }
        return creds;
    }
}