#Create an IAM user with permissions to list all S3 buckets and manage objects (put/get) in the specific S3 bucket created.
#Generate the access keys for the IAM user, allowing the user to interact with the bucket using their credentials.
#Output user access key and secret key can be used in application for downloading product pictures.

provider "aws" {
  region = "*"
  access_key = "*"
  secret_key = "*"
}



resource "aws_iam_user" "s3_user" {
  name = "s3-access-user"
}


resource "aws_s3_bucket" "s3_bucket" {
  bucket = "shop-bot-pictures-dev"

  tags = {
    Name        = "S3Bucket"
    Environment = "Dev"
  }
}

resource "aws_s3_bucket_policy" "bucket_policy" {
  bucket = aws_s3_bucket.s3_bucket.id
  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect   = "Allow",
        Action   = [
          "s3:PutObject",
          "s3:GetObject"
        ],
        Resource = "${aws_s3_bucket.s3_bucket.arn}/*",
        Principal = {
          AWS = aws_iam_user.s3_user.arn
        }
      }
    ]
  })
}


resource "aws_iam_policy" "s3_access_policy" {
  name        = "S3AccessPolicy"
  description = "Policy to allow listing S3 buckets and getting objects from a specific bucket"
  policy      = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect   = "Allow",
        Action   = [
          "s3:ListBucket"
        ],
        Resource = "*"
      },
      {
        Effect   = "Allow",
        Action   = [
          "s3:GetObject",
          "s3:PutObject"
        ],
        Resource = "${aws_s3_bucket.s3_bucket.arn}/*"
      }
    ]
  })
}


resource "aws_iam_user_policy_attachment" "attach_policy" {
  user       = aws_iam_user.s3_user.name
  policy_arn = aws_iam_policy.s3_access_policy.arn
}


resource "aws_iam_access_key" "s3_user_access_key" {
  user = aws_iam_user.s3_user.name
}


output "aws_access_key_id" {
  value = aws_iam_access_key.s3_user_access_key.id
}

output "aws_secret_access_key" {
  value = aws_iam_access_key.s3_user_access_key.secret
  sensitive = true
}




