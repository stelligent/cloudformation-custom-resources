# cloudformation-custom-resources

This repository contains stub code for setting up Lambda-backed CloudFormation Custom Resources.

### What is a custom resource?

CloudFormation has lots of defined resources that you can use to provision AWS resources. However, if you want to provision an AWS resource that CloudFormation doesn't support yet, or if you want to include some complicated logic during your CloudFormation stack creation / update / deletion, you can use a _custom resource_ to do that easily. Custom resources are basically just Lambda functions that get called by CloudFormation. While not complicated, they do 

### How do I use this repository?

This repository contains a superset of what you'll need to get started using custom resources. It has sample templates in both JSON and YML formats (in the `cfn/` dir), as well as the scaffolding for Lambda functions in Node.js, Python, and Java (in the `lambdas/` dir). You can pick whichever one is the best choice for you and run with it.

For the sample templates, there are two flavors available: create-and-use of a custom resource in a single template, or separate templates for creation and use of the custom resouce. If you're going to be re-using your custom resource from a lot of other templates, you'll probably want to use separate templates for each, as having them in the same template will create a copy of the Lambda function for each stack. While the costs associated with that are negligible, if you're provisioning a lot of stacks it can make a mess pretty easily.

Once you've decided on which languages you'll want to use, you simple have to implement the logic for the create, read, and update actions that CloudFormation will send you. Depending on what your custom resource does, you may not need to implement them all, or you may re-use the logic for multiple actions. 

### Running the templates

_Note_: these directions assume you have the AWS CLI [installed](http://docs.aws.amazon.com/cli/latest/userguide/installing.html) and [configured](http://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html).

These templates are designed to load the Lambda functions from an S3 bucket. While it is possible to keep the entire Lambda code inside the template, that is a road that gets painful very quickly so it's best avoid. So there first thing you'll need to do is to create a bucket to stage your Lambda function code in:

    aws s3 mb s3://whatever-you-wanna-call-your-bucket

Once that's in place, you can use the following commands to zip, upload, and then kick off the CloudFormation templates. These examples are for the Python flavor, but are easily adaptable to Node.js or Java:

    create_stack=create-custom-rsc
    use_stack=use-custom-rsc
    bucket=jonny-test-custom-resource-bucket

    # if you're using Node.js or Java, change this to match that directory name
    pushd lambda/python
    rm -rf tmp
    mkdir -p tmp
    zip tmp/custom-resource.zip customresource.* 
    aws s3 cp tmp/custom-resource.zip s3://${bucket}/custom-resource.zip
    rm -rf tmp
    popd 
    # And then change the LambdaRuntime parameter
    aws cloudformation create-stack \
      --stack-name $create_stack \
      --template-body file://cfn/json/create-custom-resource.json \
      --capabilities CAPABILITY_IAM \
      --disable-rollback \
      --parameters \
        ParameterKey="S3Bucket",ParameterValue="${bucket}" \
        ParameterKey="S3Key",ParameterValue="custom-resource.zip" \
        ParameterKey="ModuleName",ParameterValue="customresource" \
        ParameterKey="LambdaRuntime",ParameterValue="python2.7"

    # the sleep call is here if you're running this in a script -- it takes about that long for the Lambda function to deploy.
    # if you're running this by hand, you can skip that part.
    sleep 30
    custom_function_arn=$(aws cloudformation describe-stacks --stack-name $create_stack --query Stacks[*].Outputs[?OutputKey==\'CustomFunctionArn\'].OutputValue --output text)
    
    aws cloudformation create-stack \
      --stack-name $use_stack \
      --template-body file://cfn/json/use-custom-resource.json \
      --disable-rollback \
      --parameters \
        ParameterKey="CustomFunctionArn",ParameterValue="$custom_function_arn"



