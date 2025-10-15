variable "cluster_name" {
  description = "name of the EKS cluster"
    type        = string
}
variable "vpc_id" {
  description = "The VPC ID where the EKS cluster will be deployed"
  type        = string
}
variable "subnet_ids" {
  description = "The subnet IDs where the EKS cluster will be deployed"
  type        = list(string)
}
variable "terraform_admin_role_arn" {
    description = "The ARN of the IAM role for Terraform admin access"
    type        = string
}