#!/bin/bash
echo "Initializing LocalStack S3..."
awslocal s3 mb s3://eatda-storage-local --region ap-northeast-2
echo "S3 bucket 'eatda-storage-local' created successfully."

SEED_ZIP="/seed/eatda-s3.zip"
if [ ! -f "$SEED_ZIP" ]; then
  echo "WARNING: $SEED_ZIP not found. Skipping S3 seed data upload."
  exit 0
fi

echo "Extracting S3 seed data..."
TMPDIR=$(mktemp -d)
python3 -m zipfile -e "$SEED_ZIP" "$TMPDIR"

echo "Uploading seed data to S3..."
awslocal s3 sync "$TMPDIR" s3://eatda-storage-local/ --region ap-northeast-2

rm -rf "$TMPDIR"
echo "S3 seed data uploaded successfully."
