TF_DIR = infrastructure/terraform
VM_IP = $(shell terraform -chdir=$(TF_DIR) output -raw k3s_vm_public_ip)

ifeq ($(OS),Windows_NT)
    SSH = C:\Windows\sysnative\OpenSSH\ssh.exe
    SCP = C:\Windows\sysnative\OpenSSH\scp.exe
    CAT = type
    SSH_KEY = $(USERPROFILE)\.ssh\id_rsa_azure
else
    SSH = ssh
    SCP = scp
    CAT = cat
    SSH_KEY = ~/.ssh/id_rsa_azure
endif

.PHONY: start stop backup-db restore-db ssh

# 1. Build the infrastructure from scratch and restore the database
start:
	@echo "=> Building Azure infrastructure..."
	cd $(TF_DIR) && terraform init -upgrade && terraform apply -auto-approve
	@echo "=> Waiting 45s for VM SSH to boot..."
	timeout /t 45 /nobreak
	$(MAKE) deploy-stack

# Internal target: runs AFTER infrastructure exists so $(VM_IP) evaluates correctly
deploy-stack:
	@echo "=> Checking if Docker and K3s are installed and ready..."
	$(SSH) -i $(SSH_KEY) -o StrictHostKeyChecking=no ubuntu@$(VM_IP) "while ! command -v docker >/dev/null 2>&1; do echo 'Waiting for Docker...'; sleep 5; done; while ! command -v k3s >/dev/null 2>&1; do echo 'Waiting for K3s...'; sleep 5; done; while ! sudo k3s kubectl get node >/dev/null 2>&1; do echo 'Waiting for Kubernetes to be ready...'; sleep 5; done; echo 'Infrastructure is Ready!'"
	@echo "=> Copying Secrets to VM..."
	$(eval FIREBASE_JSON := $(wildcard infrastructure/Firebase/*.json))
	$(SCP) -i $(SSH_KEY) -o StrictHostKeyChecking=no .env ubuntu@$(VM_IP):/tmp/.env
	$(SCP) -i $(SSH_KEY) -o StrictHostKeyChecking=no $(FIREBASE_JSON) ubuntu@$(VM_IP):/tmp/firebase-service-account.json
	@echo "=> Building Docker Image and Loading into K3s..."
	$(SSH) -i $(SSH_KEY) -o StrictHostKeyChecking=no ubuntu@$(VM_IP) "if [ ! -d 'atlashub' ]; then git clone https://github.com/OlamidotunIY/atlashub_backend.git atlashub; fi && cd atlashub && git pull && sudo docker build -t atlashub/app:latest . && sudo docker save atlashub/app:latest | sudo k3s ctr images import -"
	@echo "=> Applying Kubernetes Secrets..."
	$(SSH) -i $(SSH_KEY) -o StrictHostKeyChecking=no ubuntu@$(VM_IP) "export KUBECONFIG=/home/ubuntu/.kube/config && kubectl delete secret atlashub-secrets --ignore-not-found && kubectl create secret generic atlashub-secrets --from-env-file=/tmp/.env --from-file=firebase-service-account.json=/tmp/firebase-service-account.json"
	@echo "=> Applying Kubernetes Manifests..."
	$(SSH) -i $(SSH_KEY) -o StrictHostKeyChecking=no ubuntu@$(VM_IP) "export KUBECONFIG=/home/ubuntu/.kube/config && kubectl apply -k atlashub/infrastructure/k8s/base"
	@echo "=> Waiting 30s for MySQL to boot..."
	timeout /t 30 /nobreak
	@echo "=> Restoring Database..."
	$(MAKE) restore-db
	@echo "=> Environment is completely up and running!"

# 2. Backup the database and completely destroy the infrastructure
stop:
	@echo "=> Backing up the database to local machine..."
	-$(MAKE) backup-db || true
	@echo "=> Destroying all Azure infrastructure (including disks)..."
	cd $(TF_DIR) && terraform destroy -auto-approve
	@echo "=> Environment destroyed. Billing is now $$0."

# 3. SSH directly into the VM
ssh:
	@echo "=> Connecting to VM..."
	$(SSH) -i $(SSH_KEY) -o StrictHostKeyChecking=no ubuntu@$(VM_IP)

# -----------------------------------------------------------------------------
# DATABASE BACKUP & RESTORE COMMANDS
# -----------------------------------------------------------------------------

backup-db:
	@echo "=> Running MySQL Backup via SSH (Kubernetes)..."
	@echo "=> Connecting to the VM, running mysqldump inside the kubernetes pod, and saving it locally."
	$(SSH) -i $(SSH_KEY) -o StrictHostKeyChecking=no ubuntu@$(VM_IP) "export KUBECONFIG=/home/ubuntu/.kube/config && POD=\$$(kubectl get pod -l app=mysql -o jsonpath='{.items[0].metadata.name}') && kubectl exec \$$POD -- mysqldump -u root -proot atlashub" > atlashub_backup.sql

restore-db:
	@echo "=> Restoring MySQL Backup via SSH (Kubernetes)..."
	@echo "=> Sending the local SQL file to the VM and piping it into the MySQL kubernetes pod."
	$(CAT) atlashub_backup.sql | $(SSH) -i $(SSH_KEY) -o StrictHostKeyChecking=no ubuntu@$(VM_IP) "export KUBECONFIG=/home/ubuntu/.kube/config && POD=\$$(kubectl get pod -l app=mysql -o jsonpath='{.items[0].metadata.name}') && kubectl exec -i \$$POD -- mysql -u root -proot atlashub"
