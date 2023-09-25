
resource "aws_iam_role" "vault_s3_access" {
  name = var.iam_role_name

  assume_role_policy = jsonencode({
    Version   = "2012-10-17",
    Statement = [
      {
        Action    = "sts:AssumeRole",
        Effect    = "Allow",
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy" "vault_s3_policy" {
  name = var.iam_role_policy_name
  role = aws_iam_role.vault_s3_access.id

  policy = jsonencode({
    Version   = "2012-10-17",
    Statement = [
      {
        Action = [
          "s3:*"
        ],
        Effect   = "Allow",
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_instance_profile" "vault_s3_profile" {
  name = var.iam_instance_profile_name
  role = aws_iam_role.vault_s3_access.name
}

resource "aws_instance" "vault_server" {
  ami                  = var.ami_id
  instance_type        = var.instance_type
  key_name             = var.key_name
  iam_instance_profile = aws_iam_instance_profile.vault_s3_profile.name
  vpc_security_group_ids = [aws_security_group.allow_ssh_and_vault.id]
  tags = {
    Name = var.instance_name
  }
}
