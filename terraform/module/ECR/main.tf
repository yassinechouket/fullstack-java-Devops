resource "aws_ecr_repository" "app_repo" {
  name                 = var.name
  image_tag_mutability = "IMMUTABLE"
  force_delete         = true # only for dev/testing, avoid in production

  image_scanning_configuration {
    scan_on_push = true
  }
}