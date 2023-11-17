<h1>OPEN API Client</h1>
<p>This is simple CLI client to test OPEN API call</p>
<h3>Prerequisite</h3>
<p>
    <ul>
        <li>Java installed in the loca (note: I use JAVA 21 in this sample, but if you have different JAVA version locally, change the "properties" tag in pom.xml
        <li>Maven installed in local</li>
        <li>if you use the .jar file directly, ensure JAVA 21 and above is installed on your machine</li>
    </ul>    
</p>
<h3>Setup</h3>
<p>
    <ul>
        <li>Clone this project</li>
        <li>Go to that root dir of the project</li>
        <li>Run Maven install - $mvn clean install</li>
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
        <li>(Optional) args[5] is additional option argument and you can specify one of these options:<br>
        	<ul>
        	<li>"json-only" --> modify the output of this app to return only JSON response body.</li>
        	<li>"x,x" --> 2 comma-separated number. This option is to perform repetitive call. The first number is # of repetition of the API call. The second number is the number of second the API client will wait between each call.
                    See example usage scenario below</li>
        	</ul>      
        </li>                                
    </ul>
</p>    
<br>
<p>Sample usage:<br>
Scenario #1 : API call to list all available ghost location - <br>
$java -jar OpenAPIClient.jar /home/user1/.edgerc GET "/diagnostic-tools/v2/ghost-locations/available"
<br>
<br>    
Scenario #2: API call to perform Fast invalidate by URL in Production - https://developer.akamai.com/api/purge/ccu/resources.html#postinvalidateurl<br>
$java -jar OpenAPIClient.jar /home/user1/mytokens.txt POST "/ccu/v3/invalidate/url/production" /home/user1/headers.json /home/user1/body.json
<br>
<br>
Scenario #3: Reporting API call to get details definition of 'bytes-by-ip' report  - https://developers.akamai.com/api/core_features/reporting/bytes-by-ip.html#getreportdetails<br>
but user only want to see JSON response from endpoint (without additional verbose).<br>
$java -jar OpenAPIClient.jar /home/user1/mytokens.txt GET "/reporting-api/v1/reports/bytes-by-ip/versions/1" "-" "-" "json-only"
<br>
<br>
Scenario #4: Perform 10 times PAPI 'list contracts' API call with 5 seconds wait between each call  - https://developers.akamai.com/api/core_features/property_manager/v1.html#getcontracts<br>
$java -jar OpenAPIClient.jar /home/user1/mytokens.txt GET "/papi/v1/contracts" "-" "-" "-" "10,5"
<br>
<br>
any feedback or issue, feel free to email esenopra@akamai.com with subject "OpenCLIClient - Feedback"
</p>
