
# Existing variables
variable "key_name" {
  description = "Name of the AWS key pair"
  default     = "vault_kp"
}

variable "security_group_name" {
  description = "Name of the security group"
  default     = "allow_ssh_and_vault"
}

variable "cidr_blocks" {
  description = "CIDR blocks for security group ingress"
  default     = "0.0.0.0/0"
}

# New variables for IAM
variable "iam_role_name" {
  description = "IAM role name"
  default     = "vault-s3-access"
}

variable "iam_role_policy_name" {
  description = "IAM role policy name"
  default     = "vault-s3-policy"
}

variable "iam_instance_profile_name" {
  description = "IAM instance profile name"
  default     = "vault-s3-profile"
}

variable "ami_id" {
  description = "AMI ID for EC2 instance"
  default     = "ami-03a6eaae9938c858c"
}

variable "instance_type" {
  description = "EC2 instance type"
  default     = "t2.micro"
}

variable "instance_name" {
  description = "Name tag for the EC2 instance"
  default     = "vault-server"
}
