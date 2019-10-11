<h1>OPEN API Client</h1>
<p>This is simple CLI client to test OPEN API call</p>
<h3>Prerequisite</h3>
<p>
    <ul>
        <li>Java installed in the loca (note: I use JAVA 11 in this sample, but if you have different JAVA version locally, change the "properties" tag in pom.xml
        <li>Maven installed in local</li>
    </ul>    
</p>
<h3>To run this client</h3>
<p>
    <ul>
        <li>Clone this project</li>
        <li>Go to that root dir of the project</li>
        <li>Run Maven install - $mvn clean install
    </ul>
</p>

<p>
    The CLI takes 3 arguments
    <ol>
        <li>The .edgerc file containing all the tokens and secret</li>
        <li>The HTTP method</li>
        <li>URL path of the API call (not including hostname and protocol) surrounded with double-quotes</li>
    </ol>
</p>
<p>Sample call:<br>
$java -jar akamaigtmclientsample-1.0-SNAPSHOT.jar /home/user/.edgerc GET "/gtm-api/v1/reports/domain-list"
</p>