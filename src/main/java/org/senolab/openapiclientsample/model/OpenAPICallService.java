package org.senolab.openapiclientsample.model;

import com.akamai.edgegrid.signer.ClientCredential;
import com.akamai.edgegrid.signer.exceptions.RequestSigningException;
import com.akamai.edgegrid.signer.googlehttpclient.GoogleHttpClientEdgeGridInterceptor;
import com.akamai.edgegrid.signer.googlehttpclient.GoogleHttpClientEdgeGridRequestSigner;
import com.google.api.client.http.*;
import com.google.api.client.http.apache.ApacheHttpTransport;
import org.senolab.openapiclientsample.edgercutil.Edgerc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URI;

public class OpenAPICallService {
    private final int MAX_BODY = 131072;
    private String[] apiClientInfo;
    private long start = System.currentTimeMillis(),
            end;
    private StringBuilder uriPath;
    private ClientCredential credential;
    private HttpRequest request;
    private HttpResponse response;

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
        System.out.println("HTTP Request headers: \n"+request.getHeaders());
    }

    public void execute() throws IOException, RequestSigningException {
        //Sign request and execute
        GoogleHttpClientEdgeGridRequestSigner requestSigner = new GoogleHttpClientEdgeGridRequestSigner(credential);
        requestSigner.sign(request);
        start = System.currentTimeMillis();
        System.out.println("Executing the HTTP request...");
        response = request.execute();

        //Print HTTP response code + response headers
        System.out.println("HTTP Response code: "+response.getStatusCode());
        System.out.println("HTTP Response headers: \n"+response.getHeaders());
        //Extract the HTTP Response code and response
        if(response.getContent() != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getContent(), "UTF-8"));
            String json = reader.readLine();
            System.out.println("HTTP Response Body: ");
            while (json != null) {
                System.out.println(json+"\n");
                json = reader.readLine();
            }
            reader.close();
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

    private HttpRequestFactory createSigningRequestFactory() {
        HttpTransport httpTransport = new ApacheHttpTransport();
        return httpTransport.createRequestFactory(new HttpRequestInitializer() {
            public void initialize(HttpRequest request) throws IOException {
                request.setInterceptor(new GoogleHttpClientEdgeGridInterceptor(credential));
            }
        });
    }
}
