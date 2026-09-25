# Infrastructure & Deployment Guide

This guide contains all the essential commands to manage your Azure + Kubernetes (K3s) infrastructure. 

## 🏗️ 1. Makefile Automation (Run on Windows)
These commands are run locally on your Windows machine from the root `atlashub-backend` folder.

| Command | Description |
| :--- | :--- |
| `make start` | **From Scratch:** Provisions the Azure VM, waits for boot, installs Docker/K3s, copies secrets, builds the image, and deploys all Kubernetes pods. |
| `make deploy-stack` | **Fast Update:** Use this if the VM is already running. It checks for secrets, optionally builds the image (if missing), and applies any updated Kubernetes YAML files. |
| `make build-image` | **Force Rebuild:** Forces the remote VM to pull your latest git code, rebuild the Docker image, and load it into K3s. |
| `make stop` | **Destroy Everything:** Automatically backs up your MySQL database to your local machine (`atlashub_backup.sql`), then destroys the Azure VM and all disks (Billing = $0). |
| `make backup-db` | Manually triggers a MySQL database dump from the Kubernetes pod to your local machine. |
| `make restore-db` | Manually pushes your local `atlashub_backup.sql` file into the live MySQL Kubernetes pod. |
| `make ssh` | Instantly logs you into the Azure VM so you can run Kubernetes commands. |

---

## ☸️ 2. Kubernetes Commands (Run inside the VM)
To run these, first type `make ssh` on your Windows machine to enter the remote server. K3s has standard `kubectl` built-in!

### Checking if Apps are Running
* **See all running apps:** `kubectl get pods`
* **See everything in the cluster:** `kubectl get all`
* **Check if your domain is mapped:** `kubectl get ingress`

### Viewing Logs (Debugging)
* **Stream live logs for the Spring Boot API:** 
  `kubectl logs -f -l app=atlashub-app`
* **Stream live logs for MySQL:** 
  `kubectl logs -f -l app=mysql`
* **Stream live logs for Kafka:** 
  `kubectl logs -f -l app=kafka`

### Troubleshooting Crashes
If a pod says `CrashLoopBackOff` or `Error`, find out exactly why:
1. First, get the exact pod name: `kubectl get pods`
2. Describe the pod to see Kubernetes events/errors:
   `kubectl describe pod <exact-pod-name>`
3. Check the crash logs:
   `kubectl logs <exact-pod-name> --previous`

### Restarting Services
If you ever need to forcefully restart your Spring Boot app without rebuilding the image:
* `kubectl rollout restart deployment atlashub-app`

---

## ☁️ 3. Terraform Commands (Run on Windows)
You rarely need to run these manually because `make start` and `make stop` handle them, but they are useful for debugging.

| Command | Description |
| :--- | :--- |
| `cd infrastructure/terraform` | Move into the Terraform directory first! |
| `terraform output k3s_vm_public_ip` | Prints the exact public IP address of your current Azure VM. |
| `terraform state list` | Lists all the Azure resources Terraform is currently tracking. |
