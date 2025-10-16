module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "5.21.0"

  cidr= var.cidr
  name   = var.name
  azs = slice(var.availability_zones, 0, 2)

  private_subnets = ["10.0.1.0/24", "10.0.2.0/24"]
  public_subnets  = ["10.0.101.0/24", "10.0.102.0/24"]

  enable_nat_gateway = true  # ← This creates the NAT Gateway
  single_nat_gateway = true   # ← Creates 1 NAT in first public subnet .
  map_public_ip_on_launch = true


  public_subnet_tags = {
    "kubernetes.io/cluster/${var.eks_cluster_name}" = "shared"
    "kubernetes.io/role/elb"                    = "1"
  }

  private_subnet_tags = {
    "kubernetes.io/cluster/${var.eks_cluster_name}" = "shared"
    "kubernetes.io/role/internal-elb"           = "1"
  }

}