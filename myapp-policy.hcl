# myapp-policy.hcl
path "secret/data/myapp/*" {
  capabilities = ["read", "create", "update"]
}
