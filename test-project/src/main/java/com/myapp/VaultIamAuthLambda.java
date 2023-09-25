package com.myapp;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityResult;

import java.util.Base64;
import java.util.HashMap;

public class VaultIamAuthLambda implements RequestHandler<Object, String> {

    @Override
    public String handleRequest(Object input, Context context) {
        try {
            context.getLogger().log("Initializing Vault...");
            // Initialize Vault
            VaultConfig vaultConfig = new VaultConfig()
                    .address("http://44.211.132.58:8200/")
                    .build();
            Vault vault = new Vault(vaultConfig);

            context.getLogger().log("Fetching AWS Credentials...");
            // Get AWS Credentials
            AWSCredentials awsCredentials = new DefaultAWSCredentialsProviderChain().getCredentials();

            context.getLogger().log("Creating STS client...");
            // Create STS client
            AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .build();

            context.getLogger().log("Creating and processing GetCallerIdentity request...");
            // Create and process GetCallerIdentity request
            GetCallerIdentityRequest callerIdentityRequest = new GetCallerIdentityRequest();
            GetCallerIdentityResult callerIdentityResult = stsClient.getCallerIdentity(callerIdentityRequest);

            // Base64 encode the ARN
            String encodedArn = Base64.getEncoder().encodeToString(callerIdentityResult.getArn().getBytes());

            // Create request parameters
            HashMap<String, String> parameters = new HashMap<>();
            parameters.put("Action", "GetCallerIdentity");
            parameters.put("Version", "2011-06-15");

            // Base64 encode the request parameters
            String iamRequestUrl = Base64.getEncoder().encodeToString("https://sts.amazonaws.com".getBytes());
            String iamRequestBody = Base64.getEncoder().encodeToString("Action=GetCallerIdentity&Version=2011-06-15".getBytes());
            String iamRequestHeaders = Base64.getEncoder().encodeToString("some-header-value".getBytes());  // You would replace "some-header-value" with the actual headers

            context.getLogger().log("Performing IAM-based login...");
            // Authenticate
            vault.auth().loginByAwsIam("aws", iamRequestUrl, iamRequestBody, iamRequestHeaders, "your-role-name");

            context.getLogger().log("Reading and writing secrets...");
            // Read secret
            final String readSecret = vault.logical().read("secret/data/myapp/database").getData().get("username");

            // Write secret
            vault.logical().write("secret/data/myapp/newsecret", new HashMap<String, Object>() {{
                put("key", "value");
            }});

            return "Successfully read and wrote secrets";

        } catch (Exception e) {
            e.printStackTrace();
            context.getLogger().log("Failed: " + e.getMessage());
            return "Failed: " + e.getMessage();
        }
    }
}
