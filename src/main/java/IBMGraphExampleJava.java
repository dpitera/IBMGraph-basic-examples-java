import java.io.FileReader;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;


public class IBMGraphExampleJava {
	public static void main(String[] args) throws JSONException, ParseException, IOException {
		String baseURI = null;
		String sessionURI = null;
		String basicAuth = null;
		String gdsToken = null;
		String gdsTokenAuth = null;
		HttpClient client = new DefaultHttpClient();
		
		//Replace your credentials here
		String apiURL = null;
        String username = null;
        String password = null;
        
        if (apiURL == null || username == null || password == null) {
        	System.out.println("Please provision your own Graph service and update your credentials in the code.");
        }
        
        // The baseURI is the same as apiURL minus the /g suffix
		baseURI = apiURL.substring(0, apiURL.length()-2);
		sessionURI = baseURI + "/_session";
        
        // Form the basic authorization string
		System.out.println("Begin basic authorization string");      
        // Create the basic authorization string
        byte[] userpass = (username + ":" + password).getBytes();
        byte[] encoding = Base64.getEncoder().encode(userpass);
        basicAuth = "Basic " + new String(encoding);
        System.out.println("basicAuth is " + basicAuth);
		System.out.println("End basic authorization");
		
		System.out.println("\n\n*****************************\n");
              	
		//Get session token, for much faster authentication
        gdsTokenAuth = getGDSToken(sessionURI, basicAuth, client);
        
        //Use this to create a new graph
        //This can be useful because schemas are immutable and you can begin in a clean environment
        System.out.println("creating graph");
        String graphID = UUID.randomUUID().toString().replaceAll("-", "");
		String postURLGraph = baseURI + "/_graphs/g" + graphID;
		HttpPost httpPostGraph = new HttpPost(postURLGraph);
		httpPostGraph.setHeader("Authorization", gdsTokenAuth);
		HttpResponse httpResponseGraph = client.execute(httpPostGraph);
		HttpEntity httpEntityGraph = httpResponseGraph.getEntity();
		String contentGraph = EntityUtils.toString(httpEntityGraph);
		EntityUtils.consume(httpEntityGraph);
		JSONObject jsonContentGraph = new JSONObject(contentGraph);
		System.out.println("response from creating graph" + jsonContentGraph.toString());
		//update apiURL 
		apiURL = jsonContentGraph.getString("dbUrl");
		System.out.println("new apiURL " + apiURL);
		
		System.out.println("\n\n*****************************\n");
       
		// Define the graph schema
        System.out.println("Begin defining graph schema");
        String schemaFileName = "./src/main/java/sample-schema-file.json";
        FileReader schemaFileReader = new FileReader(schemaFileName);
        JSONObject postData = new JSONObject(schemaFileReader);
        HttpPost httpPost = new HttpPost(apiURL + "/schema");
        httpPost.setHeader("Authorization", gdsTokenAuth);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Accept", "application/json");
        StringEntity strEnt = new StringEntity(postData.toString(), ContentType.APPLICATION_JSON);
        httpPost.setEntity(strEnt);
        HttpResponse httpResponse = client.execute(httpPost);
		HttpEntity httpEntity = httpResponse.getEntity();
		String content = EntityUtils.toString(httpEntity);
		EntityUtils.consume(httpEntity);
		JSONObject jsonContent = new JSONObject(content);
		JSONObject result = jsonContent.getJSONObject("result");
		JSONArray data = result.getJSONArray("data");
		if (data.length() > 0) {
		    JSONObject response = data.getJSONObject(0);
		    System.out.println("response from creating schema" + response);
		}
		
		System.out.println("\n\n*****************************\n");		
        
		//Create a vertex, v1
        System.out.println("creating vertex");
		String v1 = null;
		String postURL = apiURL + "/vertices";
		postData = new JSONObject();
		postData.put("name", "david");
		postData.put("address", "Boston");
		postData.put("phone", "888-888-8888");
		httpPost = new HttpPost(postURL);
		strEnt = new StringEntity(postData.toString(), ContentType.APPLICATION_JSON);
		httpPost.setEntity(strEnt);
		httpPost.setHeader("Authorization", gdsTokenAuth);
		httpResponse = client.execute(httpPost);
		httpEntity = httpResponse.getEntity();
		content = EntityUtils.toString(httpEntity);
		EntityUtils.consume(httpEntity);
		jsonContent = new JSONObject(content);
		result = jsonContent.getJSONObject("result");
		data = result.getJSONArray("data");
		if (data.length() > 0) {
		    JSONObject response = data.getJSONObject(0);
		    System.out.println("response from creating vertex, v1" + response);
		    v1 = response.getString("id");
		}
		
		System.out.println("\n\n*****************************\n");
		
		//Get vertex by ID
		System.out.println("getting vertex by id");
		HttpGet httpGet = new HttpGet(apiURL + "/vertices/" + v1);
		httpGet.setHeader("Authorization", gdsTokenAuth);
		httpResponse = client.execute(httpGet);
		httpEntity = httpResponse.getEntity();
		content = EntityUtils.toString(httpEntity);
		EntityUtils.consume(httpEntity);
		jsonContent = new JSONObject(content);
		result = jsonContent.getJSONObject("result");
		data = result.getJSONArray("data");
		if (data.length() > 0) {
		    JSONObject response = data.getJSONObject(0);
		    System.out.println("response from get vertex by id" + response);
		}
		
		System.out.println("\n\n*****************************\n");
		
		//Get vertex by indexed property
		System.out.println("getting vertex by indexed property");
		httpGet = new HttpGet(apiURL + "/vertices?name=david");
		httpGet.setHeader("Authorization", gdsTokenAuth);
		httpResponse = client.execute(httpGet);
		httpEntity = httpResponse.getEntity();
		content = EntityUtils.toString(httpEntity);
		EntityUtils.consume(httpEntity);
		jsonContent = new JSONObject(content);
		result = jsonContent.getJSONObject("result");
		data = result.getJSONArray("data");
		if (data.length() > 0) {
		    JSONObject response = data.getJSONObject(0);
		    System.out.println("response from gettinng vertex by indexed property" + response);
		}
		
		System.out.println("\n\n*****************************\n");
		
		//Updating a vertex
		System.out.println("updating a vertex");
		postURL = apiURL + "/vertices/" + v1;
		postData = new JSONObject();
		postData.put("phone", "999-999-9999");
		httpPost = new HttpPost(postURL);
		httpPost.setHeader("Authorization", gdsTokenAuth);
		strEnt = new StringEntity(postData.toString(), ContentType.APPLICATION_JSON);
		httpPost.setEntity(strEnt);
		httpResponse = client.execute(httpPost);
		httpEntity = httpResponse.getEntity();
		content = EntityUtils.toString(httpEntity);
		EntityUtils.consume(httpEntity);
		jsonContent = new JSONObject(content);
		result = jsonContent.getJSONObject("result");
		data = result.getJSONArray("data");
		if (data.length() > 0) {
		    JSONObject response = data.getJSONObject(0);
		    System.out.println("response from updating vertex is " + response);
		}
		
		System.out.println("\n\n*****************************\n");
		
		//Get vertex by multiple indexed properties and label
		System.out.println("getting vertex by multiple indexed properties and label");
		httpGet = new HttpGet(apiURL + "/vertices?name=david&phone=999-999-9999&label=vertex");
		httpGet.setHeader("Authorization", gdsTokenAuth);
		httpResponse = client.execute(httpGet);
		httpEntity = httpResponse.getEntity();
		content = EntityUtils.toString(httpEntity);
		EntityUtils.consume(httpEntity);
		jsonContent = new JSONObject(content);
		result = jsonContent.getJSONObject("result");
		data = result.getJSONArray("data");
		if (data.length() > 0) {
		    JSONObject response = data.getJSONObject(0);
		    System.out.println("response from gettinng vertex by multiple indexed properties and label" + response);
		}
		
		System.out.println("\n\n*****************************\n");
		
		//Now let's do some edge operations. To create an edge, we need to create an additional vertex.
		//Create vertex, v2
		System.out.println("creating vertex, v2");
		String v2 = null;
		postURL = apiURL + "/vertices";
		postData = new JSONObject();
		postData.put("name", "ricardo");
		postData.put("address", "california");
		postData.put("phone", "111-111-1111");
		httpPost = new HttpPost(postURL);
		strEnt = new StringEntity(postData.toString(), ContentType.APPLICATION_JSON);
		httpPost.setEntity(strEnt);
		httpPost.setHeader("Authorization", gdsTokenAuth);
		httpResponse = client.execute(httpPost);
		httpEntity = httpResponse.getEntity();
		content = EntityUtils.toString(httpEntity);
		EntityUtils.consume(httpEntity);
		jsonContent = new JSONObject(content);
		result = jsonContent.getJSONObject("result");
		data = result.getJSONArray("data");
		if (data.length() > 0) {
		    JSONObject response = data.getJSONObject(0);
		    System.out.println("response from creating vertex, v2" + response);
		    v2 = response.getString("id");
		}
		
		System.out.println("\n\n*****************************\n");
		
		//Create edge
		String e1 = null;
		System.out.println("creating an edge between vertices v1 and v2");
		postURL = apiURL + "/edges";
		String vertexId1 = v1;
		String vertexId2 = v2;
		postData = new JSONObject();
		postData.put("outV", vertexId1);
		postData.put("inV", vertexId2);
		postData.put("label", "knows");
		httpPost = new HttpPost(postURL);
		httpPost.setHeader("Authorization", gdsTokenAuth);
		strEnt = new StringEntity(postData.toString(), ContentType.APPLICATION_JSON);
		httpPost.setEntity(strEnt);
		httpResponse = client.execute(httpPost);
		httpEntity = httpResponse.getEntity();
		content = EntityUtils.toString(httpEntity);
		EntityUtils.consume(httpEntity);
		jsonContent = new JSONObject(content);
		result = jsonContent.getJSONObject("result");
		data = result.getJSONArray("data");
		if (data.length() > 0) {
		    JSONObject response = data.getJSONObject(0);
		    System.out.println("response from creating an edge between vertices v1 and v2" + response);
		    e1 = response.getString("id");
		}
		
		System.out.println("\n\n*****************************\n");
		
		//Get an edge by id
		System.out.println("Getting an edge by id");
		httpGet = new HttpGet(apiURL + "/edges/" + e1);
		httpGet.setHeader("Authorization", basicAuth);
		httpResponse = client.execute(httpGet);
		httpEntity = httpResponse.getEntity();
		content = EntityUtils.toString(httpEntity);
		EntityUtils.consume(httpEntity);
		jsonContent = new JSONObject(content);
		result = jsonContent.getJSONObject("result");
		data = result.getJSONArray("data");
		if (data.length() > 0) {
		    JSONObject response = data.getJSONObject(0);
		    System.out.println("response from getting edge by id" + response);
		}
		
		System.out.println("\n\n*****************************\n");
		
		//You can similarly update an edge using its id, or get an edge by its label and indexed properties.
		//Will not show here since it is the same thing for vertices, except you use the `/edges` endpoint.
		//Furthermore, I did not create any index properties for the edges above.
		//Note: Vertex and edge labels are immutable. Edge labels are required.
		
		//Basic gremlin query
		System.out.println("running a basic gremlin query");
		postURL = apiURL + "/gremlin";
		postData = new JSONObject();
		postData.put("gremlin", "def g = graph.traversal(); g.V(" + v1 + ").out()");
		httpPost = new HttpPost(postURL);
		httpPost.setHeader("Authorization", gdsTokenAuth);
		strEnt = new StringEntity(postData.toString(), ContentType.APPLICATION_JSON);
		httpPost.setEntity(strEnt);
		httpResponse = client.execute(httpPost);
		httpEntity = httpResponse.getEntity();
		content = EntityUtils.toString(httpEntity);
		EntityUtils.consume(httpEntity);
		jsonContent = new JSONObject(content);
		result = jsonContent.getJSONObject("result");
		data = result.getJSONArray("data");
		if (data.length() > 0) {
		    JSONObject response = data.getJSONObject(0);
		    System.out.println("response from basic gremlin query" + response);
		}
		
		//Delete an edge
		HttpDelete httpDelete = new HttpDelete(apiURL + "/edges/" + e1);
		httpDelete.setHeader("Authorization", gdsTokenAuth);
		httpResponse = client.execute(httpDelete);
		httpEntity = httpResponse.getEntity();
		content = EntityUtils.toString(httpEntity);
		EntityUtils.consume(httpEntity);
		
		//Delete a vertex
		httpDelete = new HttpDelete(apiURL + "/vertices/" + v1);
		httpDelete.setHeader("Authorization", gdsTokenAuth);
		httpResponse = client.execute(httpDelete);
		httpEntity = httpResponse.getEntity();
		content = EntityUtils.toString(httpEntity);
		EntityUtils.consume(httpEntity);
	}

	private static String getGDSToken(String sessionURI, String basicAuth, HttpClient client) 
			throws ClientProtocolException, IOException, JSONException {
		String gdsToken;
		String gdsTokenAuth = null;
    	System.out.println("Start getting session Authorization Token");
		HttpGet httpGet = new HttpGet(sessionURI);
		httpGet.setHeader("Authorization", basicAuth);
		HttpResponse httpResponse = client.execute(httpGet);
		HttpEntity httpEntity = httpResponse.getEntity();
		String content = EntityUtils.toString(httpEntity);
		System.out.println("content is " + content);
		EntityUtils.consume(httpEntity);
		JSONObject jsonContent = new JSONObject(content);
		gdsToken = jsonContent.getString("gds-token");
		System.out.println("The gdsToken value is " + gdsToken);
		gdsTokenAuth = "gds-token " + gdsToken;
        System.out.println("gdsTokenAuth is " + gdsTokenAuth);
      	System.out.println("End getting session Authorization Token");
		System.out.println("\n\n*****************************\n");
		return gdsTokenAuth;
	}
}
