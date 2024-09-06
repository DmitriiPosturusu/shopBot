## AWS S3 Bucket and IAM User with Access Policy

This Terraform configuration creates an AWS S3 bucket and an IAM user with a specific access policy. The policy allows the user to list all S3 buckets and get/put objects in the S3 bucket created by the script.

### Features
- **S3 Bucket Creation**: Creates an S3 bucket with private access.
- **IAM User**: Creates an IAM user with permissions to interact with the S3 bucket.
- **Access Policies**:
    - The user can list all S3 buckets.
    - The user can get and put objects in the specific S3 bucket.
- **S3 Bucket Policy**: Allows the user to manage objects in the bucket.
- **Access Keys**: Outputs the IAM user’s access key ID and secret access key for API access.

---

### Prerequisites

To use this configuration, you need:
- **Terraform** installed ([Installation guide](https://www.terraform.io/downloads)).
- **AWS CLI** (optional, but helpful for managing AWS credentials) ([Installation guide](https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2.html)).
- **AWS credentials** configured locally or set via environment variables.

### Setup

1. **Clone the repository or create the configuration file:**

   Clone this repository or copy the provided Terraform configuration into your working directory.

2. **Configure AWS credentials**:

   Ensure your local environment has access to the AWS account where you want to create the S3 bucket and IAM user. Set your AWS credentials via one of the following methods:

    - Use environment variables:
      ```bash
      export AWS_ACCESS_KEY_ID="your-access-key-id"
      export AWS_SECRET_ACCESS_KEY="your-secret-access-key"
      ```
    - Or configure credentials via AWS CLI:
      ```bash
      aws configure
      ```

3. **Initialize Terraform**:

   Initialize the working directory with Terraform. This step downloads the necessary provider plugins (AWS in this case).
   ```bash
   terraform init
   ```

4. **Apply the configuration**:

   Apply the configuration to create the S3 bucket and IAM user. This step will create the resources in your AWS account.
   ```bash
   terraform apply
   ```

   You will be asked to confirm the action by typing `yes`.

5. **Outputs**:

   After running the configuration, Terraform will output the IAM user’s access credentials (`aws_access_key_id` and `aws_secret_access_key`). Use these credentials to authenticate and interact with the S3 bucket.

---

### Resources Created

- **IAM User**:
    - A new IAM user with access to list all S3 buckets and get/put objects in the created S3 bucket.

- **S3 Bucket**:
    - A new S3 bucket with private access.
    - The IAM user has access to manage objects in this bucket.

- **Policies**:
    - An IAM policy that allows the user to list buckets and access the specific bucket.
    - An S3 bucket policy allowing the user to manage objects.

---

### Example Usage

1. **Using the IAM User credentials**:

   The output from the Terraform script provides the IAM user credentials. These can be used in AWS CLI or SDKs to interact with the S3 bucket.

   For example, using the AWS CLI:
   ```bash
   aws s3 ls --profile new-user
   aws s3 cp file.txt s3://my-s3-bucket/ --profile new-user
   ```

2. **Modifying the bucket name**:

   To change the bucket name, edit the `bucket` attribute in the `aws_s3_bucket` resource:
   ```hcl
   resource "aws_s3_bucket" "my_bucket" {
     bucket = "your-new-bucket-name"
     ...
   }
   ```

---

### Clean Up

To remove the resources created by Terraform, run the following command:
```bash
terraform destroy
```

---

### Troubleshooting

- **Permission Issues**: Ensure that your AWS credentials have sufficient permissions to create IAM users and S3 buckets.
- **Bucket Name Conflicts**: S3 bucket names are globally unique. If the name is already taken, modify the `bucket` attribute in the configuration.
- **Sensitive Data**: Ensure that you handle the IAM user’s credentials securely and avoid sharing them publicly.

---

