#!/bin/bash

# Compress and upload html
cd ./web/
gzip *html
aws s3 cp --exclude ".git/*" --content-encoding gzip *.html.gz s3://images.social/
gzip -d *.html.gz

# Upload CSS
aws s3 cp --exclude ".git/*" *.css s3://images.social/

# Upload other files
aws s3 cp robots.txt s3://images.social/

# Reset path
cd -
