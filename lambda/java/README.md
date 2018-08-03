AWS Lambda java8 CloudFormation custom resource example
=======================================================

How to create and package java lambda function
----------------------------------------------

1. git clone git@github.com:stelligent/cloudformation-custom-resources.git
2. cd cloudformation-custom-resources/lambda/java
3. ./gradlew buildZip
4. zip package will be created in build/distributions/java.zip
5. Upload this to S3 bucket. Remember to pass this bucket name in as ParameterValue for ParameterKey="S3Bucket"

Create cfn custom resource with java specific sample params
-----------------------------------------------------------

aws cloudformation create-stack \
--stack-name test-create-custom-resource-cfn-20171120154514\
--template-body file://cfn/json/create-custom-resource.json\
--capabilities CAPABILITY_IAM\
--disable-rollback\
--parameters\
ParameterKey="S3Bucket",ParameterValue="$value-of-s3-bucket-name"\
ParameterKey="S3Key",ParameterValue="customresource.jar"\
ParameterKey="ModuleName",ParameterValue="com.stelligent.customresource"\
ParameterKey="HandlerName",ParameterValue="CustomResourceHandler"\
ParameterKey="LambdaRuntime",ParameterValue="java8"

Use cfn custom resource with java specific sample params
-------------------

aws cloudformation create-stack \
--stack-name test-use-custom-resource-cfn-20171120153114\
--template-body file://cfn/json/use-custom-resource.json \
--disable-rollback \
--parameters \
ParameterKey="CustomFunctionArn",\
ParameterValue="$value-of-lambda-created-by-create-cfn-run"
