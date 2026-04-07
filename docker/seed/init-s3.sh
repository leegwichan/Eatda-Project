#!/bin/bash
echo "Initializing LocalStack S3..."
awslocal s3 mb s3://eatda-storage-local --region ap-northeast-2
echo "S3 bucket 'eatda-storage-local' created successfully."

echo "Setting bucket policy for public read access..."
awslocal s3api put-bucket-policy --bucket eatda-storage-local --policy '{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::eatda-storage-local/*"
    }
  ]
}'
echo "Bucket policy applied."

SEED_ZIP="/seed/eatda-s3.zip"
if [ ! -f "$SEED_ZIP" ]; then
  echo "WARNING: $SEED_ZIP not found. Skipping S3 seed data upload."
  exit 0
fi

echo "Extracting S3 seed data..."
TMPDIR=$(mktemp -d)
python3 -m zipfile -e "$SEED_ZIP" "$TMPDIR"

echo "Uploading seed data to S3..."
# zip 내부에 eatda-s3/ 상위 폴더가 있으면 그 안의 내용만 업로드
if [ -d "$TMPDIR/eatda-s3" ]; then
  awslocal s3 sync "$TMPDIR/eatda-s3" s3://eatda-storage-local/ --region ap-northeast-2
else
  awslocal s3 sync "$TMPDIR" s3://eatda-storage-local/ --region ap-northeast-2
fi

rm -rf "$TMPDIR"
echo "S3 seed data uploaded successfully."
