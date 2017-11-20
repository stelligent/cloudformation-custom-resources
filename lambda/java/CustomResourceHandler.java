package com.stelligent.customresource;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import org.json.JSONObject;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class CustomResourceHandler implements RequestHandler<Map<String, Object>, Object> {

	@Override
	public Object handleRequest(Map<String, Object> input, Context context) {
		LambdaLogger logger = context.getLogger();
		logger.log("Input: " + input);

		String requestType = (String) input.get("RequestType");

		JSONObject responseData = new JSONObject();

		if (requestType!=null && requestType.equalsIgnoreCase("Create")) {
			logger.log("CREATE!");
			// Put your custom create logic here
			responseData.put("Message", "Resource creation successful!");
			return sendResponse(input, context, "SUCCESS", responseData);
		} else if (requestType!=null && requestType.equalsIgnoreCase("Update")) {
			logger.log("UDPATE!");
			// Put your custom update logic here
			responseData.put("Message", "Resource update successful!");
			return sendResponse(input, context, "SUCCESS", responseData);
		} else if (requestType!=null && requestType.equalsIgnoreCase("Delete")) {
			logger.log("DELETE!");
			// Put your custom delete logic here 
			responseData.put("Message", "Resource deletion successful!");
			return sendResponse(input, context, "SUCCESS", responseData);
		} else {
			logger.log("FAILURE!");
			return sendResponse(input, context, "FAILURE", responseData);
		}
	}

	public Object sendResponse(final Map<String, Object> input, final Context context, final String responseStatus,
			JSONObject responseData) {

		String responseURL = (String) input.get("ResponseURL");		
		context.getLogger().log("ResponseURL: " + responseURL);

		URL url;
		try {
			url = new URL(responseURL);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("PUT");
			OutputStreamWriter response = new OutputStreamWriter(connection.getOutputStream());
			JSONObject responseBody = new JSONObject();

			responseBody.put("Status", responseStatus);
			responseBody.put("PhysicalResourceId", context.getLogStreamName());
			responseBody.put("StackId", input.get("StackId"));
			responseBody.put("RequestId", input.get("RequestId"));
			responseBody.put("LogicalResourceId", input.get("LogicalResourceId"));
			responseBody.put("Data", responseData);
			
			response.write(responseBody.toString());
			response.close();
			context.getLogger().log("Response Code: " + connection.getResponseCode());
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

}

