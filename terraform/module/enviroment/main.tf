module "network" {
  source = "../network"

  availability_zones = ["eu-west-3a", "eu-west-3b"]
  bastion_ingress    = var.bastion_ingress
  cidr               = "10.0.0.0/16"
  name               = var.name
}




