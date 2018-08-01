'''Custom generic CloudFormation resource example'''

import json
import logging
import signal
from urllib2 import build_opener, HTTPHandler, Request

LOGGER = logging.getLogger()
LOGGER.setLevel(logging.INFO)


def handler(event, context):
    '''Handle Lambda event from AWS'''
    # Setup alarm for remaining runtime minus a second
    signal.alarm((context.get_remaining_time_in_millis() / 1000) - 1)
    try:
        LOGGER.info('REQUEST RECEIVED:\n %s', event)
        LOGGER.info('REQUEST RECEIVED:\n %s', context)
        if event['RequestType'] == 'Create':
            LOGGER.info('CREATE!')
            sendResponse(event, context, "SUCCESS",
                         {"Message": "Resource creation successful!"})
        elif event['RequestType'] == 'Update':
            LOGGER.info('UPDATE!')
            sendResponse(event, context, "SUCCESS",
                         {"Message": "Resource update successful!"})
        elif event['RequestType'] == 'Delete':
            LOGGER.info('DELETE!')
            sendResponse(event, context, "SUCCESS",
                         {"Message": "Resource deletion successful!"})
        else:
            LOGGER.info('FAILED!')
            sendResponse(event, context, "FAILED",
                         {"Message": "Unexpected event received from CloudFormation"})
    except:
        LOGGER.info('FAILED!')
        sendResponse(event, context, "FAILED", {
                     "Message": "Exception during processing"})


def sendResponse(event, context, responseStatus, responseData):
    responseBody = json.dumps({
        "Status": responseStatus,
        "Reason": "See the details in CloudWatch Log Stream: " + context.log_stream_name,
        "PhysicalResourceId": context.log_stream_name,
        "StackId": event['StackId'],
        "RequestId": event['RequestId'],
        "LogicalResourceId": event['LogicalResourceId'],
        "Data": responseData
    })

    LOGGER.info('ResponseURL: %s', event['ResponseURL'])
    LOGGER.info('ResponseBody: %s', responseBody)

    opener = build_opener(HTTPHandler)
    request = Request(event['ResponseURL'], data=responseBody)
    request.add_header('Content-Type', '')
    request.add_header('Content-Length', len(responseBody))
    request.get_method = lambda: 'PUT'
    response = opener.open(request)
    print("Status code: {}".format(response.getcode()))
    print("Status message: {}".format(response.msg))


def timeout_handler(_signal, _frame):
    '''Handle SIGALRM'''
    raise Exception('Time exceeded')


signal.signal(signal.SIGALRM, timeout_handler)
