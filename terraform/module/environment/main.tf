module "network" {
  source = "../network"

  availability_zones = ["eu-west-3a", "eu-west-3b"]
  bastion_ingress    = var.bastion_ingress
  cidr               = "10.0.0.0/16"
  name               = var.name
  eks_cluster_name = var.name
}

module "ecr" {
  source = "../ECR"
  name = var.name
}



resource "aws_iam_role" "terraform_admin_role" {
  name = "${var.name}-terraform-admin-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          AWS = "arn:aws:iam::${data.aws_caller_identity.this.account_id}:root"
        }
        Action = "sts:AssumeRole"
      }
    ]
  })
  tags = {
    Name        = "${var.name}-terraform-admin-role"
    Environment = "production"
    ManagedBy   = "Terraform"
  }

}
resource "aws_iam_role_policy_attachment" "terraform_admin_policy" {
  role       = aws_iam_role.terraform_admin_role.name
  policy_arn = "arn:aws:iam::aws:policy/AdministratorAccess"
}

module "eks"{
  source = "../eks"

  cluster_name=var.name
  vpc_id=module.network.vpc_id
  subnet_ids=module.network.private_subnets
  terraform_admin_role_arn = aws_iam_role.terraform_admin_role.arn

  depends_on = [
    aws_iam_role_policy_attachment.terraform_admin_policy
  ]
}







