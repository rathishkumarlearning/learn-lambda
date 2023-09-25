storage "s3" {
  bucket = "vault-bucket-96345cd"
  region = "us-east-1"
}

listener "tcp" {
  address     = "0.0.0.0:8200"
  tls_disable = 1
}

ui = true