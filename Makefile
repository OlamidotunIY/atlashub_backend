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
	@echo "=> Copying Secrets to VM (if not exists)..."
	$(eval FIREBASE_JSON := $(wildcard infrastructure/Firebase/*.json))
	$(eval FIREBASE_JSON_NAME := $(notdir $(FIREBASE_JSON)))
	@$(SSH) -i $(SSH_KEY) -o StrictHostKeyChecking=no ubuntu@$(VM_IP) "test -f /tmp/.env" && echo "=> .env already exists on VM, skipping." || $(SCP) -i $(SSH_KEY) -o StrictHostKeyChecking=no .env ubuntu@$(VM_IP):/tmp/.env
	@$(SSH) -i $(SSH_KEY) -o StrictHostKeyChecking=no ubuntu@$(VM_IP) "test -f /tmp/$(FIREBASE_JSON_NAME)" && echo "=> Firebase JSON already exists on VM, skipping." || $(SCP) -i $(SSH_KEY) -o StrictHostKeyChecking=no $(FIREBASE_JSON) ubuntu@$(VM_IP):/tmp/$(FIREBASE_JSON_NAME)
	@echo "=> Ensuring Docker Image exists in K3s..."
	@$(SSH) -i $(SSH_KEY) -o StrictHostKeyChecking=no ubuntu@$(VM_IP) "sudo k3s ctr images ls | grep -q atlashub/app:latest" || "$(MAKE)" build-image
	@echo "=> Applying Kubernetes Secrets..."
	$(SSH) -i $(SSH_KEY) -o StrictHostKeyChecking=no ubuntu@$(VM_IP) "export KUBECONFIG=/home/ubuntu/.kube/config && kubectl delete secret atlashub-secrets firebase-secrets --ignore-not-found && kubectl create secret generic atlashub-secrets --from-env-file=/tmp/.env && kubectl create secret generic firebase-secrets --from-file=/tmp/$(FIREBASE_JSON_NAME)"
	@echo "=> Applying Kubernetes Manifests..."
	$(SSH) -i $(SSH_KEY) -o StrictHostKeyChecking=no ubuntu@$(VM_IP) "if [ ! -d 'atlashub' ]; then git clone https://github.com/OlamidotunIY/atlashub_backend.git atlashub; fi && cd atlashub && git checkout master && git pull origin master"
	$(SSH) -i $(SSH_KEY) -o StrictHostKeyChecking=no ubuntu@$(VM_IP) "export KUBECONFIG=/home/ubuntu/.kube/config && kubectl apply -k atlashub/infrastructure/k8s/base"
	@echo "=> Waiting for MySQL to boot and accept connections..."
	@$(SSH) -i $(SSH_KEY) -o StrictHostKeyChecking=no ubuntu@$(VM_IP) "export KUBECONFIG=/home/ubuntu/.kube/config && while true; do POD=$$(kubectl get pod -l app=mysql -o jsonpath='{.items[0].metadata.name}' 2>/dev/null); if [ -n \"$$POD\" ]; then if kubectl exec $$POD -- mysqladmin ping -u root -proot --silent 2>/dev/null; then break; fi; fi; echo 'Waiting for MySQL container...'; sleep 5; done; echo 'MySQL is fully ready!'"
	@echo "=> Restoring Database..."
	$(MAKE) restore-db
	@echo "=> Environment is completely up and running!"

build-image:
	@echo "=> Building Docker Image and Loading into K3s..."
	$(SSH) -i $(SSH_KEY) -o StrictHostKeyChecking=no ubuntu@$(VM_IP) "if [ ! -d 'atlashub' ]; then git clone https://github.com/OlamidotunIY/atlashub_backend.git atlashub; fi && cd atlashub && git checkout master && git pull origin master && sudo docker build -t atlashub/app:latest . && sudo docker save atlashub/app:latest | sudo k3s ctr images import -"

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

# 4. Update Cloudflare DNS
update-dns:
	@echo "=> Updating Cloudflare DNS for api.atlashub.name.ng to $(VM_IP)..."
	@$(SSH) -i $(SSH_KEY) -o StrictHostKeyChecking=no ubuntu@$(VM_IP) "\
		CF_TOKEN=\$$(grep CLOUDFLARE_API_TOKEN /tmp/.env | cut -d '=' -f2 | tr -d '\r') && \
		CF_ZONE=\$$(grep CLOUDFLARE_ZONE_ID /tmp/.env | cut -d '=' -f2 | tr -d '\r') && \
		if [ -z \"\$$CF_TOKEN\" ] || [ -z \"\$$CF_ZONE\" ]; then \
			echo 'ERROR: CLOUDFLARE_API_TOKEN or CLOUDFLARE_ZONE_ID not found in .env'; exit 1; \
		fi && \
		sudo apt-get update >/dev/null 2>&1 && sudo apt-get install -y jq >/dev/null 2>&1 && \
		echo 'Fetching existing DNS Record ID...' && \
		RECORD_ID=\$$(curl -s -X GET \"https://api.cloudflare.com/client/v4/zones/\$$CF_ZONE/dns_records?name=api.atlashub.name.ng&type=A\" -H \"Authorization: Bearer \$$CF_TOKEN\" -H \"Content-Type: application/json\" | jq -r '.result[0].id') && \
		if [ \"\$$RECORD_ID\" = \"null\" ] || [ -z \"\$$RECORD_ID\" ]; then \
			echo 'Creating new DNS record...' && \
			curl -s -X POST \"https://api.cloudflare.com/client/v4/zones/\$$CF_ZONE/dns_records\" -H \"Authorization: Bearer \$$CF_TOKEN\" -H \"Content-Type: application/json\" --data '{\"type\":\"A\",\"name\":\"api.atlashub.name.ng\",\"content\":\"$(VM_IP)\",\"ttl\":1,\"proxied\":true}' >/dev/null; \
		else \
			echo 'Updating existing DNS record...' && \
			curl -s -X PUT \"https://api.cloudflare.com/client/v4/zones/\$$CF_ZONE/dns_records/\$$RECORD_ID\" -H \"Authorization: Bearer \$$CF_TOKEN\" -H \"Content-Type: application/json\" --data '{\"type\":\"A\",\"name\":\"api.atlashub.name.ng\",\"content\":\"$(VM_IP)\",\"ttl\":1,\"proxied\":true}' >/dev/null; \
		fi && echo '\n=> DNS Update Complete!'"

# -----------------------------------------------------------------------------
# DATABASE BACKUP & RESTORE COMMANDS
# -----------------------------------------------------------------------------

backup-db:
	@echo "=> Running MySQL Backup via SSH (Kubernetes)..."
	@echo "=> Connecting to the VM, running mysqldump inside the kubernetes pod, and saving it locally."
	$(SSH) -i $(SSH_KEY) -o StrictHostKeyChecking=no ubuntu@$(VM_IP) "export KUBECONFIG=/home/ubuntu/.kube/config && POD=$$(kubectl get pod -l app=mysql -o jsonpath='{.items[0].metadata.name}') && kubectl exec $$POD -- mysqldump -u root -proot atlashub" > atlashub_backup.sql

restore-db:
	@echo "=> Restoring MySQL Backup via SSH (Kubernetes)..."
	@if not exist atlashub_backup.sql echo "=> No backup file found. Skipping restore."
	@if exist atlashub_backup.sql echo "=> Sending the local SQL file to the VM and piping it into the MySQL kubernetes pod."
	@if exist atlashub_backup.sql $(CAT) atlashub_backup.sql | $(SSH) -i $(SSH_KEY) -o StrictHostKeyChecking=no ubuntu@$(VM_IP) "export KUBECONFIG=/home/ubuntu/.kube/config && POD=$$(kubectl get pod -l app=mysql -o jsonpath='{.items[0].metadata.name}') && kubectl exec -i $$POD -- mysql -u root -proot atlashub"
