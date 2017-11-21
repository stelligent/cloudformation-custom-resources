How to create and package java lamda function
-------------------

1. git clone git@github.com:stelligent/cloudformation-custom-resources.git
2. cd cloudformation-custom-resources/lambda/java
3. mvn package
4. jar(zip) package will be created in target/customresource-1.0.0.jar
5. Upload this to S3 bucket. Remember to pass this bucket name in as ParameterValue for ParameterKey="S3Bucket"
