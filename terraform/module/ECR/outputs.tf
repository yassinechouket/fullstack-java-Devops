
output "repository_name" {
  value = aws_ecr_repository.app_repo.name
}

output "repository_arn" {
  value = aws_ecr_repository.app_repo.arn
}
