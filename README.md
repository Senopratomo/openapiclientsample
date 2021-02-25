<h1>OPEN API Client</h1>
<p>This is simple CLI client to test OPEN API call</p>
<h3>Prerequisite</h3>
<p>
    <ul>
        <li>Java installed in the loca (note: I use JAVA 11 in this sample, but if you have different JAVA version locally, change the "properties" tag in pom.xml
        <li>Maven installed in local</li>
    </ul>    
</p>
<h3>Setup</h3>
<p>
    <ul>
        <li>Clone this project</li>
        <li>Go to that root dir of the project</li>
        <li>Run Maven install - $mvn clean install
    </ul>
</p>

<h3>How to use this client</h3>
<p>This CLI takes 3 - 6 arguments separated by a single space depends on the API call and options that you require:
    <ul>
        <li>(Required) args[0] is location of .edgerc file. This file contain Akamai API client credentials (client token,
                access token, secret, host, and max body size) which necessary for EdgeGrid lib
                sample:
                [default]
                host = https://akab-xxxxx.luna.akamaiapis.net
                client_token = akab-xxxxx
                client_secret = xxxxx
                access_token = xxxx
                max-body = 131072</li>
        <li>(Required) args[1] is HTTP method</li>
        <li>(Required) args[2] is path to endpoint enclosed in double quotes (eg: "/api/v2/somepath?q1=1")</li>
        <li>(Optional) args[3] is a json file containing list of HTTP headers that you want to pass on to the API call in JSON format. If there is no extra header being added, put "-"
                the key will be HTTP header name and the value will be value of that header
                     sample1: header.json to send "Content-Type" header with value "application/json" in API call
                     {"Content-Type":"application/json"}
                                sample2: headers.json to send "Content-Type" header with value "application/json" and also "If-Match" header with value of "6aed418629b4e5c0"
                                {"Content-Type":"application/json","If-Match":"6aed418629b4e5c0"}</li>
        <li>(Optional) args[4] (for PUT and POST) is path to the HTTP request body. If there is no body being pass, put "-"</li>
        <li>(Optional) args[5] is modify the output of this app to return only JSON response body. Please type "json-only" to have this call return only JSON</li>                                
    </ul>
</p>    
<br>
<p>Sample usage:<br>
Scenario #1 : API call to list all available ghost location - 
$java -jar OpenAPIClient.jar /home/user1/.edgerc GET "/diagnostic-tools/v2/ghost-locations/available"
<br>
<br>    
Scenario #2: API call to perform Fast invalidate by URL in Production - https://developer.akamai.com/api/purge/ccu/resources.html#postinvalidateurl
$java -jar OpenAPIClient.jar /home/user1/mytokens.txt POST "/ccu/v3/invalidate/url/production" /home/user1/headers.json /home/user1/body.json
<br>
<br>
any feedback or issue, feel free to email esenopra@akamai.com with subject "OpenCLIClient - Feedback"
</p>
