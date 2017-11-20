import json
import httplib
import logging

logger = logging.getLogger()
logger.setLevel(logging.INFO)

def handler(event, context):
    logger.info('REQUEST RECEIVED:\n {}'.format(event))
    logger.info('REQUEST RECEIVED:\n {}'.format(context))
    if event['RequestType'] == 'Create':
        logger.info('CREATE!')
        sendResponse(event, context, "SUCCESS", { "Message": "Resource creation successful!" })
    elif event['RequestType'] == 'Update':
        logger.info('UPDATE!')
    elif event['RequestType'] == 'Delete':
        logger.info('DELETE!')
    else:
        logger.info('FAILED!')

def sendResponse(event, context, responseStatus, responseData):
    full_url = event['ResponseURL'][8:-1]
    host, path = full_url.split('/', 1)
    path = '/' + path

    responseBody = json.dumps({
        "Status": responseStatus,
        "Reason": "See the details in CloudWatch Log Stream: " + context.log_stream_name,
        "PhysicalResourceId": context.log_stream_name,
        "StackId": event['StackId'],
        "RequestId": event['RequestId'],
        "LogicalResourceId": event['LogicalResourceId'],
        "Data": responseData
    })

    headers = {
        "Content-Type": "",
        "content-length": len(responseBody)
    }

    logger.info('Host: {}'.format(host))
    logger.info('Path: {}'.format(path))
    logger.info('Full URL: {}{}{}'.format('https://', host, path))
    logger.info('ResponseBody: {}'.format(responseBody))
    logger.info('Headers: {}'.format(headers))
    

    h = httplib.HTTPSConnection(host, 443)
    h.set_debuglevel(1)
    h.request('PUT', path, responseBody, headers)
    response = h.getresponse()
    h.close()
