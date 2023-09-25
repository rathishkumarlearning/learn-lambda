#!/bin/bash

# Define key name
key_name="vault_kp"

# Delete any existing .pem files in the current directory
rm -f ./*.pem

# Delete existing key pair (if exists)
aws ec2 delete-key-pair --key-name $key_name

# Create key pair and save to .pem file
aws ec2 create-key-pair --key-name $key_name --query 'KeyMaterial' --output text > $key_name.pem

# Change permissions
chmod 400 $key_name.pem
ls -l $key_name.pem

echo "Key pair $key_name.pem is ready for use."
