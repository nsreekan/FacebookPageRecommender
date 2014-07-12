package fbrecommender;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.StringTokenizer;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * This class represents the HttpClient to retrieve user's facebook 
 * data using REST client and outputs it as JSON
 * @author SatNam621
 *
 */

public class ApacheHttpClient {
	
	
	public static void fetchFBData(String token) throws ClientProtocolException, IOException
	{
		DefaultHttpClient httpClient = WebClientDevWrapper.wrapClient();
		//URLEncoder.encode(token, "UTF-8");
		HttpGet getRequest = new HttpGet(
			"https://graph.facebook.com/me/friends?fields=likes&access_token="+URLEncoder.encode(token, "UTF-8"));
		
	    System.out.println(getRequest.getURI().toString());
		
		getRequest.addHeader("accept", "application/json");
 
		HttpResponse response = httpClient.execute(getRequest);
 
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
			   + response.getStatusLine().getReasonPhrase());
		}
		BufferedReader br = new BufferedReader(
                new InputStreamReader((response.getEntity().getContent())));
		
		//response output is a json string...
		StringBuilder output = new StringBuilder();
		String str;
		
		File file = new File("c:/data/jsonOutput.txt");
		 
		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}
		
		
		System.out.println("Output from Server .... \n");
		while ((str = br.readLine()) != null) {
			output.append(str);
			break;
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		
		bw.write(output.toString());
		bw.close();

		System.out.println("Done");
		httpClient.getConnectionManager().shutdown();
		
	}
}
