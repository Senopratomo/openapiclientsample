package org.senolab.openapiclientsample.model;

import com.akamai.edgegrid.signer.ClientCredential;
import com.akamai.edgegrid.signer.exceptions.RequestSigningException;
import com.akamai.edgegrid.signer.googlehttpclient.GoogleHttpClientEdgeGridInterceptor;
import com.akamai.edgegrid.signer.googlehttpclient.GoogleHttpClientEdgeGridRequestSigner;
import com.google.api.client.http.*;
import com.google.api.client.http.apache.ApacheHttpTransport;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.senolab.openapiclientsample.edgercutil.Edgerc;

import java.io.*;
import java.net.InetAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class OpenAPICallService {
    private final int MAX_BODY = 131072;
    private String[] apiClientInfo;
    private long start = System.currentTimeMillis(),
            end;
    private StringBuilder uriPath;
    private ClientCredential credential;
    private HttpRequest request;
    private HttpResponse response;
    private boolean hasContentDisposition = false;

    public OpenAPICallService(String edgerc, String httpMethod, String httpPath) throws IOException {
        System.out.println("Reading your token credentials from "+edgerc+" ....");
        apiClientInfo = Edgerc.extractTokens(edgerc);
        System.out.println("Extracting tokens information....");
        //Build the URL based on the path specified and the host from edgerc file
        uriPath = buildURIPath(apiClientInfo[0], httpPath);

        //Build the credential based on tokens provided in edgerc file
        credential = buildCredential(apiClientInfo[2], apiClientInfo[3], apiClientInfo[4], apiClientInfo[0]);

        //Build the HTTP request
        request = buildHttpRequest(httpMethod, uriPath.toString());
        response = null;

        //Setting Host header
        HttpHeaders headers = request.getHeaders();
        headers.set("Host", apiClientInfo[1]);
        //Getting Akamai IP for verbose debugging purpose
        System.out.println("Resolving hostname....");
        InetAddress address = InetAddress.getByName(apiClientInfo[1]);
        System.out.println("Akamai IP: "+address.getHostAddress());
    }

    public OpenAPICallService(String edgerc, String httpMethod, String httpPath, String headerFile) throws IOException, ParseException {
        System.out.println("Reading your token credentials from "+edgerc+" ....");
        apiClientInfo = Edgerc.extractTokens(edgerc);
        System.out.println("Extracting tokens information....");
        //Build the URL based on the path specified and the host from edgerc file
        uriPath = buildURIPath(apiClientInfo[0], httpPath);

        //Build the credential based on tokens provided in edgerc file
        credential = buildCredential(apiClientInfo[2], apiClientInfo[3], apiClientInfo[4], apiClientInfo[0]);

        //Build the HTTP request
        request = buildHttpRequest(httpMethod, uriPath.toString());
        response = null;

        //Add additional header
        HttpHeaders headers = setHttpHeaders(headerFile);
        //Setting Host header
        headers.set("Host", apiClientInfo[1]);
        //Getting Akamai IP for verbose debugging purpose
        System.out.println("Resolving hostname....");
        InetAddress address = InetAddress.getByName(apiClientInfo[1]);
        System.out.println("Akamai IP: "+address.getHostAddress());
    }

    public OpenAPICallService(String edgerc, String httpMethod, String httpPath, String headerFile, String jsonBodyFile) throws IOException, ParseException {
        System.out.println("Reading your token credentials from "+edgerc+" ....");
        apiClientInfo = Edgerc.extractTokens(edgerc);
        System.out.println("Extracting tokens information....");
        //Build the URL based on the path specified and the host from edgerc file
        uriPath = buildURIPath(apiClientInfo[0], httpPath);

        //Build the credential based on tokens provided in edgerc file
        credential = buildCredential(apiClientInfo[2], apiClientInfo[3], apiClientInfo[4], apiClientInfo[0]);

        //Build the HTTP request
        request = buildHttpRequest(httpMethod, uriPath.toString(), jsonBodyFile);
        response = null;

        //Add additional header
        HttpHeaders headers = setHttpHeaders(headerFile);
        //Setting Host header
        headers.set("Host", apiClientInfo[1]);
        //Getting Akamai IP for verbose debugging purpose
        System.out.println("Resolving hostname....");
        InetAddress address = InetAddress.getByName(apiClientInfo[1]);
        System.out.println("Akamai IP: "+address.getHostAddress());

    }

    public void execute() throws IOException, RequestSigningException {
        //Sign request and execute
        GoogleHttpClientEdgeGridRequestSigner requestSigner = new GoogleHttpClientEdgeGridRequestSigner(credential);
        requestSigner.sign(request);
        System.out.println("HTTP Request headers: ");
        for(String key : request.getHeaders().keySet()) {
            System.out.println(key + ": " + request.getHeaders().get(key));
        }
        start = System.currentTimeMillis();
        System.out.println("Executing the HTTP request...");
        response = request.execute();

        //Print HTTP response code + response headers
        System.out.println("HTTP Response code: "+response.getStatusCode());
        System.out.println("HTTP Response headers: ");
        for(String key : response.getHeaders().keySet()) {
            System.out.println(key + ": " + response.getHeaders().get(key));
            if (key.equalsIgnoreCase("content-disposition")) {
                hasContentDisposition = true;
            }
        }
        //Extract the HTTP Response code and response
        if(response.getContent() != null && !hasContentDisposition) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getContent(), "UTF-8"));
            String json = reader.readLine();
            System.out.println("HTTP Response Body: ");
            while (json != null) {
                System.out.println(json+"\n");
                json = reader.readLine();
            }
            reader.close();
        } else if(response.getContent() != null && hasContentDisposition) {
            String contentDisposition = response.getHeaders().get("content-disposition").toString();
            String fileName = contentDisposition.replaceAll("\\[", "").replaceAll("\\]","")
                    .split("=")[1];
            OutputStream outputStream = new FileOutputStream(fileName);
            response.download(outputStream);
            outputStream.close();
            System.out.println("HTTP Response Body: downloaded to file "+fileName);

        }

        end = System.currentTimeMillis();
        System.out.println("Time taken: "+(end-start)+" milis");
    }


    private ClientCredential buildCredential(String clientToken, String accessToken, String clientSecret, String host) {
        return new ClientCredential.ClientCredentialBuilder()
                .clientToken(clientToken)
                .accessToken(accessToken)
                .clientSecret(clientSecret)
                .host(host)
                .build();
    }

    private StringBuilder buildURIPath(String host, String path) {
        return new StringBuilder()
                .append("https://")
                .append(host)
                .append(path);
    }

    private HttpRequest buildHttpRequest(String method, String urlPath) throws IOException {
        System.out.println("Building API call....");
        HttpRequestFactory requestFactory = createSigningRequestFactory();
        URI uri = URI.create(urlPath);
        HttpRequest request = null;
        if (method.equalsIgnoreCase("get")) {
            request = requestFactory.buildGetRequest(new GenericUrl(uri));
        } else if (method.equalsIgnoreCase("delete")) {
            request = requestFactory.buildDeleteRequest(new GenericUrl(uri));
        } else if (method.equalsIgnoreCase("put")) {
            request = requestFactory.buildPutRequest(new GenericUrl(uri), null);
        } else if (method.equalsIgnoreCase("post")) {
            request = requestFactory.buildPostRequest(new GenericUrl(uri), null);
        } else {
            request = requestFactory.buildRequest(method.toUpperCase(), new GenericUrl(uri), null);
        }
        request.setFollowRedirects(true);
        request.setReadTimeout(400000);
        return request;
    }

    private HttpRequest buildHttpRequest(String method, String urlPath, String jsonBodyFile) throws IOException, ParseException {
        //Parse JSON body
        boolean isThereBody;
        isThereBody = !jsonBodyFile.equals("-");

        boolean isXML;
        if(!jsonBodyFile.endsWith(".json")) {
            isXML = true;
        } else {
            isXML = false;
        }

        String requestBody = null;
        JSONParser parser = new JSONParser();
        if (isThereBody && !isXML) {
            System.out.println("Reading input file for JSON body from "+jsonBodyFile+" ....");
            Object obj = parser.parse(new FileReader(jsonBodyFile));
            if(obj instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) obj;
                requestBody = jsonObject.toJSONString();
            }
            if(obj instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) obj;
                requestBody = jsonArray.toJSONString();
            }
            System.out.println("JSON body: ");
            System.out.println(requestBody);
        } else if(isThereBody && isXML) {
            System.out.println("Reading input file for non-JSON body from "+jsonBodyFile+" ....");
            Path path = Paths.get(jsonBodyFile);
            requestBody = Files.lines(path).collect(Collectors.joining());
            System.out.println("Request body: ");
            System.out.println(requestBody);
        } else {
            System.out.println("No request Body for this call...");
        }

        System.out.println("Building API call....");
        HttpRequestFactory requestFactory = createSigningRequestFactory();
        URI uri = URI.create(urlPath);
        HttpRequest request = null;
        if (method.equalsIgnoreCase("put")) {
            request = requestFactory.buildPutRequest(new GenericUrl(uri), ByteArrayContent.fromString(null, requestBody));
        } else if (method.equalsIgnoreCase("post")) {
            request = requestFactory.buildPostRequest(new GenericUrl(uri), ByteArrayContent.fromString(null, requestBody));
        } else if (method.equalsIgnoreCase("patch")) {
            request = requestFactory.buildPatchRequest(new GenericUrl(uri), ByteArrayContent.fromString(null, requestBody));
        } else {
            request = requestFactory.buildRequest(method.toUpperCase(), new GenericUrl(uri), ByteArrayContent.fromString(null, requestBody));
        }
        request.setFollowRedirects(true);
        request.setReadTimeout(400000);
        return request;
    }

    private HttpRequestFactory createSigningRequestFactory() {
        HttpTransport httpTransport = new ApacheHttpTransport();
        return httpTransport.createRequestFactory(new HttpRequestInitializer() {
            public void initialize(HttpRequest request) {
                request.setInterceptor(new GoogleHttpClientEdgeGridInterceptor(credential));
            }
        });
    }

    private HttpHeaders setHttpHeaders(String headersInputFile) throws IOException, ParseException {
        //Parse header input file to be added to the HTTP request
        System.out.println("Reading additional header data from "+headersInputFile+" ....");
        JSONParser parser = new JSONParser();
        Object headerObj = parser.parse(new FileReader(headersInputFile));
        JSONObject headerJsonObject = (JSONObject) headerObj;
        String headersAdded = headerJsonObject.toJSONString();
        System.out.println("Additional headers to be added: ");
        System.out.println(headersAdded);

        HttpHeaders headers = request.getHeaders();
        for (Object key : headerJsonObject.keySet()) {
            switch (((String) key).toLowerCase()) {
                case "content-type":
                    headers.setContentType((String) headerJsonObject.get(key));
                    break;
                case "accept":
                    headers.setAccept((String) headerJsonObject.get(key));
                    break;
                case "if-match":
                    headers.setIfMatch((String) headerJsonObject.get(key));
                    break;
                case "accept-encoding":
                    headers.setAcceptEncoding((String) headerJsonObject.get(key));
                    break;
                default:
                    headers.set((String) key, (String) headerJsonObject.get(key));
                    break;
            }
        }
        return headers;
    }
}
