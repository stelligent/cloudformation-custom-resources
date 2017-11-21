CFN Custom Resources
===============

AWS lambda java function to manage Cloudformation Custom Resources

Create cfn custom resource
-------------------

aws cloudformation create-stack \
--stack-name test-create-custom-resource-cfn-20171120154514\
--template-body file://cfn/json/create-custom-resource.json\
--capabilities CAPABILITY_IAM\
--disable-rollback\
--parameters\
ParameterKey="S3Bucket",ParameterValue="jonny-test-custom-resource-bucket"\
ParameterKey="S3Key",ParameterValue="customresource.zip"\
ParameterKey="ModuleName",ParameterValue="com.stelligent.customresource"\
ParameterKey="HandlerName",ParameterValue="CustomResourceHandler"\
ParameterKey="LambdaRuntime",ParameterValue="java8"


Use cfn custom resource
-------------------

aws cloudformation create-stack \
--stack-name test-use-custom-resource-cfn-20171120153114\
--template-body file://cfn/json/use-custom-resource.json \
--disable-rollback \
--parameters \
ParameterKey="CustomFunctionArn",\
ParameterValue="$value-of-lambda-created-by-create-cfn-run"
