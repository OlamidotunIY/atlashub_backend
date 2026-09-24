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

## 4. Stopping or Restarting the API
If you need to completely stop the backend and databases (this won't delete your data):
```bash
cd atlashub/infrastructure/docker
docker compose -f docker-compose.prod.yml down
```

If you just want to restart the Java backend (for example, if it crashed):
```bash
cd atlashub/infrastructure/docker
docker compose -f docker-compose.prod.yml restart app
```

---

## 5. Copying your `.env` File to the Server
If you have a `.env` file on your local Windows machine that you need to securely transfer to the Docker folder on the VM, open PowerShell on your Windows machine and run:

```powershell
scp -i ~/.ssh/id_rsa_azure .env ubuntu@<YOUR_VM_IP>:~/atlashub/infrastructure/docker/.env
```

---

## 6. Copying Firebase Credentials (Required for Startup)
The Java backend requires a Firebase Service Account JSON file to boot.

First, create the secrets folder on the remote server by running this on your local Windows machine:
```powershell
ssh -i ~/.ssh/id_rsa_azure ubuntu@<YOUR_VM_IP> "mkdir -p ~/atlashub/infrastructure/docker/secrets"
```

Next, securely copy your local Firebase JSON file into that folder:
```powershell
scp -i ~/.ssh/id_rsa_azure path\to\your\local-firebase.json ubuntu@<YOUR_VM_IP>:~/atlashub/infrastructure/docker/secrets/firebase-service-account.json
```

---

## 7. Checking Logs
If something isn't working, SSH into the server and check the backend logs:
```bash
cd atlashub/infrastructure/docker
docker compose -f docker-compose.prod.yml logs -f app
```
