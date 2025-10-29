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



/*
depends_on = [
  aws_iam_role_policy_attachment.terraform_admin_policy
]
```

**Why do we need this?**

Terraform creates resources in parallel (at the same time). Without `depends_on`:
```
Time 0: Start creating IAM role ━━━━━━━━━━━━━━━━━━━━━━→ Done at Time 5
Time 0: Start creating Policy attachment ━━━━━━━━━━━→ Done at Time 3
Time 0: Start creating EKS cluster ━━━━━━━━━━━━━━━━→ ERROR! Role not ready!
```

With `depends_on`:
```
Time 0: Start creating IAM role ━━━━━━━━━━━━━━━━━━━━━━→ Done at Time 5
Time 5: Start creating Policy attachment ━━━━━━━━━━━→ Done at Time 8
Time 8: Start creating EKS cluster ━━━━━━━━━━━━━━━━→ Success!
```

**In simple words:** "Hey Terraform, don't create the EKS cluster until the IAM role AND its permissions are fully ready!"


1. Create IAM Role ✅
2. Attach permissions to the role ✅
3. Wait for steps 1 & 2 to finish
4. Create EKS cluster with the role ARN ✅
   ↓
  SUCCESS! Everything works! 🎉


*/



