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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class OpenAPICallServiceJSONOnly {
    private final int MAX_BODY = 131072;
    private String[] apiClientInfo;
    private long start = System.currentTimeMillis(),
            end;
    private StringBuilder uriPath;
    private ClientCredential credential;
    private HttpRequest request;
    private HttpResponse response;

    public OpenAPICallServiceJSONOnly(String edgerc, String httpMethod, String httpPath) throws IOException {
        apiClientInfo = Edgerc.extractTokens(edgerc);

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
        InetAddress address = InetAddress.getByName(apiClientInfo[1]);
    }

    public OpenAPICallServiceJSONOnly(String edgerc, String httpMethod, String httpPath, String headerFile) throws IOException, ParseException {
        apiClientInfo = Edgerc.extractTokens(edgerc);

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

        InetAddress address = InetAddress.getByName(apiClientInfo[1]);
    }

    public OpenAPICallServiceJSONOnly(String edgerc, String httpMethod, String httpPath, String headerFile, String jsonBodyFile) throws IOException, ParseException {
        apiClientInfo = Edgerc.extractTokens(edgerc);
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
        InetAddress address = InetAddress.getByName(apiClientInfo[1]);

    }

    public void execute() throws IOException, RequestSigningException {
        //Sign request and execute
        GoogleHttpClientEdgeGridRequestSigner requestSigner = new GoogleHttpClientEdgeGridRequestSigner(credential);
        requestSigner.sign(request);
        response = request.execute();

        //Print HTTP response code + response headers
        //Extract the HTTP Response code and response
        if(response.getContent() != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getContent(), "UTF-8"));
            String json = reader.readLine();
            while (json != null) {
                System.out.println(json+"\n");
                json = reader.readLine();
            }
            reader.close();
        }
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
            Object obj = parser.parse(new FileReader(jsonBodyFile));
            if(obj instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) obj;
                requestBody = jsonObject.toJSONString();
            }
            if(obj instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) obj;
                requestBody = jsonArray.toJSONString();
            }
        } else if(isThereBody && isXML) {
            Path path = Paths.get(jsonBodyFile);
            requestBody = Files.lines(path).collect(Collectors.joining());
        } else {
        }

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
        JSONParser parser = new JSONParser();
        Object headerObj = parser.parse(new FileReader(headersInputFile));
        JSONObject headerJsonObject = (JSONObject) headerObj;
        String headersAdded = headerJsonObject.toJSONString();

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
