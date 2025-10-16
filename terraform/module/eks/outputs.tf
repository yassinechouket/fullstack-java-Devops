output "eks_cluster_name" {
  description = "EKS cluster name."
  value       = module.eks.cluster_name
}