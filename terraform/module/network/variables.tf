variable "bastion_ingress" {
  default     = []
  description = "List of CIDR blocks to whitelist for bastion host"
  type        = list(string)
}

variable "cidr" {
  description = "CIDR block"
  type        = string
}

variable "name" {
  description = "Name of the network"
  type        = string
}