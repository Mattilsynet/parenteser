terraform {
  required_version = ">= 1.1.7"
  backend "gcs" {
    bucket = "tf-state-parenteser-b480"
    prefix = "tf"
  }
}

provider "google" {
  region = "europe-north1"
  impersonate_service_account = "tf-admin-sa@parenteser-b480.iam.gserviceaccount.com"
}

provider "google-beta" {
  region = "europe-north1"
  impersonate_service_account = "tf-admin-sa@parenteser-b480.iam.gserviceaccount.com"
}
