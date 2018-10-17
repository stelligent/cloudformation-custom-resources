package com.stelligent.customresource;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.json.JSONObject;

public class CustomResourceHandler implements RequestHandler<Map<String, Object>, Object> {

  @Override
  public Object handleRequest(Map<String, Object> input, Context context) {
    LambdaLogger logger = context.getLogger();
    logger.log("Input: " + input);

    final String requestType = (String) input.get("RequestType");

    ExecutorService service = Executors.newSingleThreadExecutor();
    JSONObject responseData = new JSONObject();
    try {
      if (requestType == null) {
        throw new RuntimeException();
      }

      Runnable r = new Runnable() {
        @Override
        public void run() {
          try {
            Thread.sleep(10000);
          } catch (final InterruptedException e) {
            // empty block
          }
          if (requestType.equalsIgnoreCase("Create")) {
            logger.log("CREATE!");
            // Put your custom create logic here
            responseData.put("Message", "Resource creation successful!");
            sendResponse(input, context, "SUCCESS", responseData);
          } else if (requestType.equalsIgnoreCase("Update")) {
            logger.log("UDPATE!");
            // Put your custom update logic here
            responseData.put("Message", "Resource update successful!");
            sendResponse(input, context, "SUCCESS", responseData);
          } else if (requestType.equalsIgnoreCase("Delete")) {
            logger.log("DELETE!");
            // Put your custom delete logic here
            responseData.put("Message", "Resource deletion successful!");
            sendResponse(input, context, "SUCCESS", responseData);
          } else {
            logger.log("FAILURE!");
            sendResponse(input, context, "FAILED", responseData);
          }
        }
      };
      Future<?> f = service.submit(r);
      f.get(context.getRemainingTimeInMillis() - 1000, TimeUnit.MILLISECONDS);
    } catch (final TimeoutException | InterruptedException
                   | ExecutionException e) {
      logger.log("FAILURE!");
      sendResponse(input, context, "FAILED", responseData);
      // Took too long!
    } finally {
      service.shutdown();
    }
    return null;
  }

  /** Send a response to CloudFormation regarding progress in creating resource. */
  public final Object sendResponse(
      final Map<String, Object> input,
      final Context context,
      final String responseStatus,
      JSONObject responseData) {

    String responseUrl = (String) input.get("ResponseURL");
    context.getLogger().log("ResponseURL: " + responseUrl);

    URL url;
    try {
      url = new URL(responseUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoOutput(true);
      connection.setRequestMethod("PUT");

      JSONObject responseBody = new JSONObject();
      responseBody.put("Status", responseStatus);
      responseBody.put("PhysicalResourceId", context.getLogStreamName());
      responseBody.put("StackId", input.get("StackId"));
      responseBody.put("RequestId", input.get("RequestId"));
      responseBody.put("LogicalResourceId", input.get("LogicalResourceId"));
      responseBody.put("Data", responseData);

      OutputStreamWriter response = new OutputStreamWriter(connection.getOutputStream());
      response.write(responseBody.toString());
      response.close();
      context.getLogger().log("Response Code: " + connection.getResponseCode());

    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }

}
