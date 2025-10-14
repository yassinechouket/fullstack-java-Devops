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
}