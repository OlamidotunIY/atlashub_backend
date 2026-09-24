# Manual Server Deployment Guide

If the `make start` automation ever fails, or if you just want to do things manually, follow these steps to deploy your backend to the Azure VM.

## 1. SSH Into the Server
Find the public IP of your VM (either in the Azure Portal or by running `terraform output -raw k3s_vm_public_ip` in the `infrastructure/terraform` folder).

Open PowerShell and run:
```powershell
ssh -i ~/.ssh/id_rsa_azure ubuntu@<YOUR_VM_IP>
```
*(Alternatively, you can just run `make ssh` from your local machine to automatically log in).*

---

## 2. Clone the Code and Start Docker
Once you are logged into the server (your prompt will say `ubuntu@atlashub-k3s-node`), run these commands to pull your code and build the containers:

```bash
# Clone the repo (only needed the first time)
git clone https://github.com/OlamidotunIY/atlashub_backend.git atlashub

# Navigate to the code and ensure it's up to date
cd atlashub
git pull

# Build the Java backend and start the databases in the background
cd infrastructure/docker
docker compose -f docker-compose.prod.yml up -d --build
```
Once it says `Running 4/4`, you can type `exit` to leave the server.

---

## 3. Restore the Database
Back on your local Windows machine, restore your database backup by piping it directly into the remote MySQL container:

```powershell
cmd.exe /c "type atlashub_backup.sql | ssh -i ~/.ssh/id_rsa_azure -o StrictHostKeyChecking=no ubuntu@<YOUR_VM_IP> `"docker exec -i atlashub-mysql mysql -u root -proot atlashub`""
```

---

## 4. Checking Logs
If something isn't working, SSH into the server and check the backend logs:
```bash
cd atlashub/infrastructure/docker
docker compose -f docker-compose.prod.yml logs -f atlashub-backend
```
