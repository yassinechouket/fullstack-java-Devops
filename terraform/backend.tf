terraform {
  backend "s3" {
    bucket = "java-fullstack-devops-chouket"
    key    = "terraform.tfstate"
    region = "eu-west-3"
  }
}