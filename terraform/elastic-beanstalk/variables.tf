variable "app_name" {
  description = "The name of the Elastic Beanstalk application"
  type        = string
  default     = "shop-bot-app"
}

variable "environment" {
  description = "Environment name (dev or prod)"
  type        = string
}

variable "instance_type" {
  description = "EC2 instance type for the Beanstalk environment"
  type        = string
}

variable "region" {
  description = "AWS region"
  type        = string
  default     = "eu-west-1"
}
