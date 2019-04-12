# cloudformation-custom-resources

This repository contains stub code for setting up Lambda-backed CloudFormation Custom Resources.

### What is a custom resource?

CloudFormation has lots of defined resources that you can use to provision AWS resources. However, if you want to provision an AWS resource that CloudFormation doesn't support yet, or if you want to include some complicated logic during your CloudFormation stack creation / update / deletion, you can use a _custom resource_ to do that easily. Custom resources are basically just Lambda functions that get called by CloudFormation. While not complicated, they do require a bit of configuration to get going. This repository is design to kickstart building custom resources, having the scaffolding for Python, Node.js, and Java functions (_Ruby coming soon!_) and examples in both YML and JSON.

### How do I use this repository?

This repository contains a superset of what you'll need to get started using custom resources. It has sample templates in both JSON and YML formats (in the `cfn/` dir), as well as the scaffolding for Lambda functions in Node.js, Python, and Java (in the `lambdas/` dir). You can pick whichever one is the best choice for you and run with it.

For the sample templates, there are two flavors available: create-and-use of a custom resource in a single template, or separate templates for creation and use of the custom resouce. If you're going to be re-using your custom resource from a lot of other templates, you'll probably want to use separate templates for each, as having them in the same template will create a copy of the Lambda function for each stack. While the costs associated with that are negligible, if you're provisioning a lot of stacks it can make a mess pretty easily.

Once you've decided on which languages you'll want to use, you simply have to implement the logic for the create, read, and update actions that CloudFormation will send you. Depending on what your custom resource does, you may not need to implement them all, or you may re-use the logic for multiple actions. The documentation for CloudFormation custom resources is [here](http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/crpg-ref.html), and gives details on the request object your code should expect, and the responses that CloudFormation accepts.

### Running the templates

_Note_: these directions assume you have the AWS CLI [installed](http://docs.aws.amazon.com/cli/latest/userguide/installing.html) and [configured](http://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html).

These templates are designed to load the Lambda functions from an S3 bucket. While it is possible to keep the entire Lambda code inside the template, that is a road that gets painful very quickly so it's best avoid. So there first thing you'll need to do is to create a bucket to stage your Lambda function code in:

    aws s3 mb s3://whatever-you-wanna-call-your-bucket

Once that's in place, you can use the following commands to zip, upload, and then kick off the CloudFormation templates. These examples are for the Python flavor, but are easily adaptable to Node.js or Java:

    create_stack=create-custom-rsc
    use_stack=use-custom-rsc
    bucket=whatever-you-wanna-call-your-bucket

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

#### But, Java.

If you're looking to get the Java function running, the concepts are the same but the steps differ slightly because everything is harder in Java. :) Instead of making a zip file, we'll use maven to download all the dependencies and create a jar file, which we'll then upload into S3. You'll also need to include an extra parameter, `HandlerName`, so that Lambda knows which class and method to call. Here's an example of how you build and deploy the Java function:

    pushd lambda/java 
    rm -rf target 
    mvn package
    aws s3 cp target/customresource-1.0.0.jar s3://${bucket}/customresource-1.0.0.jar
    mvn clean
    popd
    aws cloudformation create-stack \
      --stack-name $create_stack \
      --template-body file://cfn/json/create-custom-resource.json \
      --capabilities CAPABILITY_IAM \
      --disable-rollback \
      --parameters \
        ParameterKey="S3Bucket",ParameterValue="${bucket}" \
        ParameterKey="S3Key",ParameterValue="customresource-1.0.0.jar" \
        ParameterKey="ModuleName",ParameterValue="com.stelligent.customresource" \
        ParameterKey="LambdaRuntime",ParameterValue="java8" \
        ParameterKey="HandlerName",ParameterValue="CustomResourceHandler"


Looking up the ARN of the function and creating a stack that uses the Java Lambda function is exactly the same.

### Problems?

We did our best to test out these examples, but if you notice any problems with any of them, we would be much obliged if you could let us know by [opening an issue](https://github.com/stelligent/cloudformation-custom-resources/issues)!

### Special Thanks

In true CloudFormation fashion, most of this work was built by finding something that kinda did what we wanted and then tweaking it until it worked. A lot of the concepts were based off the [Looking Up Amazon Machine Image IDs](http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/walkthrough-custom-resources-lambda-lookup-amiids.html) from the official AWS documentation.

Also, I wanted to extend a special thank you to @dghadge for putting together the Java Lambda function and to @lhitchon for providing very helpful advice about the Python function.
