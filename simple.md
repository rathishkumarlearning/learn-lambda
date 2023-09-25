```bash
run the script create_aws_key_pair.sh
aws s3api create-bucket --bucket vault-bucket-96345cd --region us-east-1

terraform init
terraform apply -auto-approve

export VAULT_SERVER_IP=$(aws ec2 describe-instances --filters "Name=tag:Name,Values=vault-server" --query 'Reservations[*].Instances[*].PublicIpAddress' --output text)
echo $VAULT_SERVER_IP

scp -i "vault_kp.pem" /Users/austin/Documents/GitHub/repos/rathishkumarlearning/ethan-learning-lambda/cred-rotate/val/vault-config.hcl ec2-user@$VAULT_SERVER_IP:/home/ec2-user/vault-config.hcl
ssh -i "vault_kp.pem" ec2-user@$VAULT_SERVER_IP

wget https://releases.hashicorp.com/vault/1.10.0/vault_1.10.0_linux_amd64.zip
unzip vault_1.10.0_linux_amd64.zip
sudo mv vault /usr/local/bin/

vault server -config=vault-config.hcl
export VAULT_SERVER_IP=$(aws ec2 describe-instances --filters "Name=tag:Name,Values=vault-server" --query 'Reservations[*].Instances[*].PublicIpAddress' --output text)
ssh -i "vault_kp.pem" ec2-user@$VAULT_SERVER_IP
export VAULT_ADDR='http://127.0.0.1:8200'
vault operator init

Unseal Key 1: U7RG7y4uKgu2KNiGaVUkeJNwtVjvBKafGhPB/ytjqFRX
Unseal Key 2: ahP+h5Mr8psU1FWnHoUNbQucVHp3ZpCmfZ4Y0VQJzJRt
Unseal Key 3: 1mOsqCtZHQiXmuEFYopfH2485E1ZIjhubNzMeyEQyIv/
Unseal Key 4: SUn61Dt1ZQS3e3XSAAh+TBtjgc/QqT/WGoOxPbFWi3qN
Unseal Key 5: RCew5jKqMyBy+n8S3ljGAJxX+DNrrtwoTO4vwD0MPMc8

Initial Root Token: hvs.eg0ZVwB3O0KrMzRwKxkHzfbc


vault operator unseal [UNSEAL_KEY_1]  (do all)
UNSEAL_KEYS=$(vault operator init -key-shares=5 -key-threshold=3 | grep 'Unseal Key' | awk '{print $NF}')
ROOT_TOKEN=$(vault operator init -key-shares=5 -key-threshold=3 | grep 'Initial Root Token' | awk '{print $NF}')
vault login [ROOT_TOKEN]
vault secrets enable -path=secret kv
vault kv put secret/creds username="vaultuser1" password="vaultpass1"
vault kv get secret/creds
http://3.88.162.6:8200/ui/


```


To perform IAM authentication from a Java Lambda function to the Vault server and read values, you can follow these steps:

### Steps to Perform IAM Authentication with Vault from Java Lambda:

1. **Vault Setup**:
    - Ensure that the Vault server is set up correctly, and the AWS authentication method is enabled.

2. **Lambda IAM Role**:
    - The Lambda function should have an IAM role attached to it. This IAM role is used to authenticate against Vault.

3. **Java Lambda Function**:
    - Create a Java Lambda function to perform IAM authentication and read values from the Vault server.

4. **Vault IAM Authentication**:
    - In the Java Lambda function, use the AWS SDK to create a signed request. This signed request is used to authenticate against Vault.

5. **Read Values**:
    - Once authenticated, you can read values from the Vault.

---

#### Java Lambda Code Snippet to Perform IAM Authentication and Read Values

Here's a simplified Java code snippet that uses the AWS SDK and Vault Java Driver to authenticate and read values. This assumes that you've already set up Vault and enabled AWS authentication.

```java
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;

public class VaultIAMAuthLambdaHandler {
    public String handleRequest(Object input) {
        try {
            // AWS Credentials for IAM role attached to Lambda
            AWSCredentials awsCredentials = new DefaultAWSCredentialsProviderChain().getCredentials();

            // Vault Configuration
            VaultConfig vaultConfig = new VaultConfig()
                    .address("http://YOUR_VAULT_SERVER_ADDRESS")
                    .build();

            Vault vault = new Vault(vaultConfig);

            // Perform IAM Authentication
            // Replace 'aws' with the mount point for the AWS auth method if you've changed it
            final String iamLoginPayload = "{ ... }";  // Generate this payload using AWS SDK
            vault.auth().loginByAwsIam("aws", awsCredentials.getAWSAccessKeyId(), awsCredentials.getAWSSecretKey(), iamLoginPayload);

            // Read Secrets (For example, secret/hello)
            final String secret = vault.logical().read("secret/hello").getData().get("value");

            return "Secret Value: " + secret;

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
```

Enabling the AWS authentication method in Vault allows to use AWS IAM credentials to authenticate with Vault. 
This method of authentication is particularly useful when running applications on AWS, as we can leverage the existing IAM roles and policies.

Below are the steps to enable AWS authentication in Vault:

### Step 1: SSH into the Vault Server

SSH into the EC2 instance where your Vault server is running.

### Step 2: Set Vault Environment Variables

If not already set, you'll need to set the `VAULT_ADDR` environment variable to point to your Vault server. You might also need to set `VAULT_TOKEN` if your Vault server requires it for admin tasks.

```bash
export VAULT_ADDR='http://44.211.132.58:8200'  # Replace with your Vault server's address
export VAULT_TOKEN='hvs.eg0ZVwB3O0KrMzRwKxkHzfbc'  # Replace with your token (hvs.5fa2Dppw82QPXFeA8mpV4jfO)
```

### Step 3: Enable AWS Authentication

Run the following Vault command to enable AWS authentication:

```bash
vault auth enable aws
```

This command enables AWS authentication at the default path ("aws"). You can specify a different path if you like.

### Step 4: Configure AWS Authentication

Next, you'll need to configure the AWS authentication method to know how to connect to the AWS API. 
This often involves specifying the AWS access credentials and region. 
If Vault is running on an AWS EC2 instance with an IAM role that has sufficient permissions, this step may not be necessary.

```bash
vault write auth/aws/config/client \
    access_key='AKIASVPHAVBLQEIPIZX6' \
    secret_key='ONXhdtr8hauhTCWMCtByp+UP6blCkcWSlnOIEg6r' \
    region='us-east-1'
```

Replace `'aws-access-key-id'` and `'aws-secret-access-key'` with your AWS credentials, and `'us-west-2'` with your AWS region.

### Step 5: Create a Role in Vault for AWS Authentication

Finally, create a role in Vault that maps to AWS IAM roles or EC2 instance profiles. This will determine what policies are assigned after authentication.

Here's how to create a Vault role that maps to an AWS IAM role:

```bash
vault write auth/aws/role/dev-role-iam \
    auth_type=iam \
    bound_iam_principal_arn=arn:aws:iam::123456789012:role/your-iam-role \
    policies=dev-policy
```

Replace `arn:aws:iam::123456789012:role/your-iam-role` with the ARN of your IAM role, 
and `dev-policy` with the Vault policy you want to assign to authenticated entities.

Now your Vault server should be configured to allow AWS IAM authentication.

You can use this setup to authenticate your Java Lambda function, 
as described in the previous message.

Certainly, you can manually create an IAM role in the AWS Management Console 
and then associate it with your Vault setup. 
Here are the steps to create the IAM role:

### Creating an IAM Role Manually in AWS

1. **Log in to AWS Console**: Open the AWS Management Console and navigate to the IAM service.

2. **Create Role**: Click on "Roles" in the left sidebar and then click the "Create role" button.

3. **Choose AWS service**: Select "AWS service" as the type of trusted entity. Then, select the service that will use this role, which could be "Lambda" if you're creating a role for a Lambda function.

4. **Attach Permissions Policies**: Attach any necessary permissions policies that your function will need. 
   This is highly dependent on what your function will be doing.

5. **Review**: Give the role a name and an optional description. Review the settings and click "Create role".

6. **Copy ARN**: After creating the role, open the role to view its details and copy the "Role ARN".

arn:aws:iam::183557466199:role/vault_role

### Updating Vault with IAM Role

Now you'll want to associate this IAM role with a role in Vault.
Make sure you're SSH'd into your Vault server and have set your `VAULT_ADDR` and `VAULT_TOKEN` environment variables. Run the following command to associate the IAM role with a role in Vault:

```bash
vault write auth/aws/role/dev-role-iam \
    auth_type=iam \
    bound_iam_principal_arn=arn:aws:iam::183557466199:role/vault_role \
    policies=<YOUR_VAULT_POLICY>
```

Replace `<YOUR_IAM_ROLE_ARN>` with the ARN of the IAM role you just created, and `<YOUR_VAULT_POLICY>` with the Vault policy you want to assign to authenticated entities.

Now your Vault setup should allow authentication for entities that assume this IAM role.

Once you've verified everything works as expected, you can then proceed to automate this process with Terraform.


---
If your Lambda function's main purpose is to authenticate with Vault and read secrets, 
then the IAM role attached to the Lambda function will require a limited set of permissions. 
Specifically, the role needs:

1. **STS Permissions**: The ability to make `sts:GetCallerIdentity` calls, which is often required for Vault AWS authentication.

Here is a sample JSON for the IAM role's permissions policy to allow `sts:GetCallerIdentity`:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowGetCallerIdentity",
      "Effect": "Allow",
      "Action": "sts:GetCallerIdentity",
      "Resource": "*"
    }
  ]
}
```

### Steps to Attach this Policy to an IAM Role in AWS Console:

1. **IAM Dashboard**: Go to the IAM dashboard in the AWS Management Console.

2. **Roles**: Click on "Roles" on the left sidebar.

3. **Create Role**: Click the "Create role" button.

4. **AWS Service**: Choose "AWS service" as the trusted entity type and select "Lambda" since you're creating a role for a Lambda function. Click "Next: Permissions".

5. **Create Policy**: Instead of selecting an existing policy, click on "Create policy". Switch to the JSON tab and paste the above JSON. Click "Review policy", give it a name, and click "Create policy".

6. **Attach Policy**: Go back to the "Create role" tab, refresh the list of policies, and select the policy you just created. Click "Next: Tags", optionally add tags, and then click "Next: Review".

7. **Role Name**: Give the role a name and review the permissions. Click "Create role" when you're ready.

8. **Copy Role ARN**: After creating the role, you can find it in the list of roles. Click on the role and copy the Role ARN, which you'll use to configure Vault.

After attaching this role to your Lambda function and setting up Vault to recognize this IAM role as explained in previous messages, your Lambda function should be able to authenticate with Vault and read secrets.

---

/home/ec2-user
[ec2-user@ip-172-31-88-85 ~]$ ls
vault-config.hcl  vault_1.10.0_linux_amd64.zip
[ec2-user@ip-172-31-88-85 ~]$

### Create and Upload the Policy
```hcl
# myapp-policy.hcl
path "secret/data/myapp/*" {
  capabilities = ["read"]
}
```
- Upload Policy: Run the following command to upload the policy to Vault:
`vault policy write myapp-policy myapp-policy.hcl`

- Associate Policy with IAM Role
```
vault write auth/aws/role/dev-role-iam \
    auth_type=iam \
    bound_iam_principal_arn=arn:aws:iam::183557466199:role/vault_role \
    policies=myapp-policy

vault kv put secret/data/myapp/database username=db-user password=db-pass (Creating a Secret at secret/data/myapp/database)
vault kv put secret/data/myapp/api api_key=123456789 (Updating a Secret at secret/data/myapp/api)
vault kv list secret/data/myapp (Listing Secrets at secret/data/myapp)
vault kv get secret/data/myapp/database (Reading a Secret at secret/data/myapp/database)

````
# Lambda Function
```
Building and Deploying
Navigate to the my-lambda-function directory.
Run mvn clean install to build the project.
This will produce a .jar file in the target/ directory.
Upload this .jar file to AWS Lambda.
```