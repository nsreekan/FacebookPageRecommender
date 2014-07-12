package fbrecommender;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.restlet.util.Template;

/**
 * This class is a REST resource that fetches the Friends data
 * from a Facebook profile. 
 * @author SatNam621
 *
 */
public class FriendsResource extends Resource{
	
	private static Template htmlTemplate = new Template(
			"Fetched data from FB and wrote into file c:/data/jsonOutput.txt !!! It is now {DATE}.");
	
	@Override
	public void init(Context context, Request request, Response response) {
		super.init(context, request, response);

		// This representation has only two types of representations.
		getVariants().add(new Variant(MediaType.TEXT_HTML));
		getVariants().add(new Variant(MediaType.TEXT_PLAIN));
	}
	
	@Override
	public Representation represent(Variant variant) {
		String jsonoutput = null;
		try {
		Reference ref = getRequest().getOriginalRef();
		String last = ref.toString();
		System.out.println(last);
		String[] array = last.split("code=");
		if(array.length >1){
		String code = array[1];
	
		String MY_ACCESS_TOKEN = "";
		String authURL = "https://graph.facebook.com/oauth/access_token?"
				+ "client_id=203189479866012&redirect_uri="
				+ "http://localhost:8181/FaceBookUsingRest&"
				+ "client_secret=f0cb62baa3763bf14513a77cd34eda67"
				+ "&scope=friends_likes&code="
				+ code;
		URL url;
		
			url = new URL(authURL);
		
		String result = readURL(url);
		String[] pairs = result.split("&");

		for (String pair : pairs) {
			String[] kv = pair.split("=");
			if (kv[0].equals("access_token")) {
				MY_ACCESS_TOKEN = kv[1];
			}
		} // end of for loop
		
		
		System.out.println("********************accesstoken *************" + MY_ACCESS_TOKEN);
		
		System.out.println("Fetching data from FB");
		
		ApacheHttpClient.fetchFBData(MY_ACCESS_TOKEN);
		}	
	} catch (MalformedURLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		
		final Map<String, Object> variables = Collections.singletonMap("DATE",
				(Object) new Date());
		return new StringRepresentation(htmlTemplate.format(variables),
				MediaType.TEXT_HTML);
	}
	
	private String readURL(URL url) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		System.out.println(url.toString());
		InputStream is = url.openStream();
		int r;
		while ((r = is.read()) != -1) {
			baos.write(r);
		}
		return new String(baos.toByteArray());
	}

}
