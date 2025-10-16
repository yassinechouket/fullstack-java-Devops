
module "eks"{
    source  = "terraform-aws-modules/eks/aws"
    version = "~> 20.8"

    cluster_name    = var.cluster_name
    cluster_version = "1.29"

    cluster_addons = {
        vpc-cni = {}
        coredns = {}
        kube-proxy = {}
        # aws-load-balancer-controller = {} 
    }

    cluster_endpoint_public_access = true

    vpc_id=var.vpc_id
    subnet_ids=var.subnet_ids


    eks_managed_node_groups = {
        main = {
            min_size     = 1
            max_size     = 3
            desired_size = 2

            instance_types = ["t3.small"]
            disk_size = 20
            ami_type = "AL2_x86_64"

            update_config = {
                max_unavailable = 1
            }

            iam_role_additional_policies = {
                eks_worker_node = "arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy"
                eks_cni = "arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy"
                ecr_read_only = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
            }
        }
    }

    # Access Entries (IAM roles/users who are allowed cluster access)
    access_entries = {
        terraform_admin = {
            principal_arn = var.terraform_admin_role_arn

            policy_associations = {
                admin_access = {
                    policy_arn = "arn:aws:eks::aws:cluster-access-policy/AmazonEKSAdminPolicy"
                    access_scope = {
                        type = "cluster"
                    }
                }
            }
        }
    }

}