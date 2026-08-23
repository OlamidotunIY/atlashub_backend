#!/bin/bash
set -e

# Update package lists
sudo apt-get update
sudo apt-get upgrade -y

# Configure Firewall to allow Kubernetes and HTTP/HTTPS traffic
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 80 -j ACCEPT
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 443 -j ACCEPT
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 6443 -j ACCEPT
sudo netfilter-persistent save

# Install K3s (Lightweight Kubernetes)
# We disable traefik if we want to install our own, but actually K3s built-in traefik is fine.
# By default, K3s includes containerd, CoreDNS, Traefik, Klipper load-balancer
curl -sfL https://get.k3s.io | sh -

# Wait for node to be ready
sleep 15

# Export kubeconfig for ubuntu user
mkdir -p /home/ubuntu/.kube
sudo cp /etc/rancher/k3s/k3s.yaml /home/ubuntu/.kube/config
sudo chown ubuntu:ubuntu /home/ubuntu/.kube/config
export KUBECONFIG=/home/ubuntu/.kube/config

echo "K3s installation complete. Node status:"
kubectl get nodes
