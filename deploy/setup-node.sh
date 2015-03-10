#!/bin/bash

echo "Package updates and upgrades"
apt-get update > /dev/null
apt-get upgrade -y

echo "Installing base packages"
apt-get install -y curl wget vim iostat
