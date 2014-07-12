package fbrecommender;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.restlet.util.Template;

/**
 * A simple resource with two representations.
 * 
 */
public class FacebookMainResource extends Resource {

	/**
	 * Formating template for HTML representation of this resource.
	 */
	private static Template htmlTemplate = new Template(
			"<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">" +
"<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"><title>TIM 260 Project - Get pages to recommend to FB user </title></head><body>	<a href=\"https://www.facebook.com/dialog/oauth?client_id=203189479866012&redirect_uri=http://localhost:8181/FaceBookUsingRest\">Click Here</a></body></html>");

	/**
	 * Formating template for text representation of this resource.
	 */
	private static Template textTemplate = new Template(
			"Hello, world! It is now {DATE}.");

	@Override
	public void init(Context context, Request request, Response response) {
		super.init(context, request, response);

		// This representation has only two types of representations.
		getVariants().add(new Variant(MediaType.TEXT_HTML));
		getVariants().add(new Variant(MediaType.TEXT_PLAIN));
	}

	/**
	 * Returns a full representation for a given variant.
	 */
	@Override
	public Representation represent(Variant variant) {
		final Map<String, Object> variables = Collections.singletonMap("DATE",
				(Object) new Date());
		if (MediaType.TEXT_HTML.equals(variant.getMediaType())) {
			return new StringRepresentation(htmlTemplate.format(variables),
					MediaType.TEXT_HTML);
		}
		return new StringRepresentation(textTemplate.format(variables),
				MediaType.TEXT_HTML);
	}
}
