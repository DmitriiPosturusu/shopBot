provider "aws" {
  region = "eu-west-1"
  access_key = "*"
  secret_key = "*"
}



resource "aws_sns_topic" "user_data_notifications" {
  name = "user-data-notifications"
}


resource "aws_sns_topic_subscription" "email_subscription" {
  topic_arn = aws_sns_topic.user_data_notifications.arn
  protocol  = "email"
  endpoint  = "*"
}



resource "aws_security_group" "allow_ssh_http" {
  name        = "allow_ssh_http"
  description = "Security group to allow SSH and HTTP access"

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"] 
  }

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]  
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"] 
  }

  tags = {
    Name = "allow-ssh-http"
  }
}

resource "aws_iam_role" "ec2_combined_role" {
  name = "ec2_combined_role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Principal = {
          Service = "ec2.amazonaws.com"
        },
        Action = "sts:AssumeRole"
      }
    ]
  })
}


resource "aws_iam_policy" "s3_access_policy" {
  name        = "s3_access_policy"
  description = "Allow EC2 to list and get objects from S3 bucket"

  policy = jsonencode({
    Version: "2012-10-17",
    Statement: [
      {
        Effect: "Allow",
        Action: [
          "s3:GetObject",
          "s3:ListBucket"
        ],
        Resource: [
          "arn:aws:s3:::s3-docker-compose",
          "arn:aws:s3:::s3-docker-compose/*"
        ]
      }
    ]
  })
}



resource "aws_iam_policy" "ec2_sns_publish_policy" {
  name        = "ec2_sns_publish_policy"
  description = "Allow EC2 instance to publish to SNS topic"

  policy = jsonencode({
    Version: "2012-10-17",
    Statement: [
      {
        Effect: "Allow",
        Action: "sns:Publish",
        Resource: aws_sns_topic.user_data_notifications.arn
      }
    ]
  })
}



resource "aws_iam_role_policy_attachment" "attach_sns_policy" {
  role       = aws_iam_role.ec2_combined_role.name
  policy_arn = aws_iam_policy.ec2_sns_publish_policy.arn
}
resource "aws_iam_role_policy_attachment" "attach_s3_policy" {
  role       = aws_iam_role.ec2_combined_role.name
  policy_arn = aws_iam_policy.s3_access_policy.arn
}


resource "aws_iam_instance_profile" "ec2_instance_profile" {
  name = "ec2_instance_profile_dev"
  role = aws_iam_role.ec2_combined_role.name
}



resource "aws_instance" "my_ec2" {
  ami           = "ami-0fa8fe6f147dc938b"
  instance_type = "t2.micro"
  tags = {
    Name = "shop-bot-app"
  }
  key_name = "ShopKeyPair"
  vpc_security_group_ids = [aws_security_group.allow_ssh_http.id]
  iam_instance_profile = aws_iam_instance_profile.ec2_instance_profile.name


  user_data = <<-EOF
    #!/bin/bash
      sudo yum update -y
      sudo yum install docker -y
      sudo service docker start
      sudo usermod -a -G docker ec2-user
      sudo chkconfig docker on
      sudo yum install -y git
      sudo curl -L https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/bin/docker-compose
      sudo chmod +x /usr/local/bin/docker-compose
      sudo wget -P /tmp/docker-compose/ https://s3-docker-compose.s3.amazonaws.com/.env
      sudo wget -P /tmp/docker-compose/ https://s3-docker-compose.s3.amazonaws.com/docker-compose.yml
      sudo docker-compose  -f /tmp/docker-compose/docker-compose.yml --env-file /tmp/docker-compose/.env up -d
    # Send SNS notification after script completion
    aws sns publish --topic-arn "${aws_sns_topic.user_data_notifications.arn}" --message "User data script has finished execution"
  EOF

}

output "instance_public_ip" {
  value = aws_instance.my_ec2.public_ip
}

output "sns_topic_arn" {
  value = aws_sns_topic.user_data_notifications.arn
}
