#!/bin/bash
echo "Initializing LocalStack S3..."
awslocal s3 mb s3://eatda-storage-local --region ap-northeast-2
echo "S3 bucket 'eatda-storage-local' created successfully."
