provider "aws" {
  region = var.region
  access_key = "*"
  secret_key = "*"
}


resource "aws_elastic_beanstalk_environment" "my_eb" {
  name                = "${var.app_name}-${var.environment}"
  application         = var.app_name
  solution_stack_name = "EB deployment to AWS"


  setting {
    namespace = "aws:autoscaling:launchconfiguration"
    name      = "InstanceType"
    value     = var.instance_type
  }

  # Setting the environment type (load balanced for prod, single for dev)
  setting {
    namespace = "aws:elasticbeanstalk:environment"
    name      = "EnvironmentType"
    value     = var.environment == "prod" ? "LoadBalanced" : "SingleInstance"
  }

  # Minimum and maximum instance configuration for auto-scaling
  setting {
    namespace = "aws:autoscaling:asg"
    name      = "MinSize"
    value     = var.environment == "prod" ? "2" : "1"  # 2 instances for prod, 1 for dev
  }

  setting {
    namespace = "aws:autoscaling:asg"
    name      = "MaxSize"
    value     = var.environment == "prod" ? "5" : "1"  # Max 5 instances for prod, 1 for dev
  }

  setting {
    namespace = "aws:autoscaling:trigger"
    name      = "MeasureName"
    value     = "CPUUtilization"
  }

  setting {
    namespace = "aws:autoscaling:trigger"
    name      = "Statistic"
    value     = "Average"
  }

  setting {
    namespace = "aws:autoscaling:trigger"
    name      = "Unit"
    value     = "Percent"
  }

  setting {
    namespace = "aws:autoscaling:trigger"
    name      = "UpperThreshold"
    value     = "75"
  }

  setting {
    namespace = "aws:autoscaling:trigger"
    name      = "LowerThreshold"
    value     = "30"
  }

  tags = {
    Environment = var.environment
  }
}
