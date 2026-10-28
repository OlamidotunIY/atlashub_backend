TF_DIR = infrastructure/terraform
VM_IP = $(shell terraform -chdir=$(TF_DIR) output -raw k3s_vm_public_ip)
JWT_KEY_DIR = infrastructure/JWT
JWT_PRIVATE_KEY = $(JWT_KEY_DIR)/atlashub-jwt-private.pem
JWT_PUBLIC_KEY = $(JWT_KEY_DIR)/atlashub-jwt-public.pem
SSH_OPTIONS = -o BatchMode=yes -o ConnectTimeout=15 -o ServerAliveInterval=15 -o ServerAliveCountMax=2 -o StrictHostKeyChecking=no
MYSQL_TUNNEL_PORT ?= 3307
MYSQL_SERVICE ?= mysql
MYSQL_SERVICE_PORT ?= 3306
MYSQL_NAMESPACE ?= default
API_NAMESPACE ?= default
API_LOG_TAIL ?= 200
MYSQL_SERVICE_IP = $(shell $(SSH) $(SSH_OPTIONS) -i $(SSH_KEY) ubuntu@$(VM_IP) "sudo k3s kubectl -n $(MYSQL_NAMESPACE) get svc $(MYSQL_SERVICE) -o jsonpath='{.spec.clusterIP}'")

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

.PHONY: start stop backup-db restore-db ssh mysql-tunnel api-logs prune-runtime-images

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
	$(SSH) $(SSH_OPTIONS) -i $(SSH_KEY) ubuntu@$(VM_IP) "while ! command -v docker >/dev/null 2>&1; do echo 'Waiting for Docker...'; sleep 5; done; while ! command -v k3s >/dev/null 2>&1; do echo 'Waiting for K3s...'; sleep 5; done; while ! sudo k3s kubectl get node >/dev/null 2>&1; do echo 'Waiting for Kubernetes to be ready...'; sleep 5; done; echo 'Infrastructure is Ready!'"
	@echo "=> Syncing Secrets to VM..."
	$(eval FIREBASE_JSON := $(wildcard infrastructure/Firebase/*.json))
	$(eval FIREBASE_JSON_NAME := $(notdir $(FIREBASE_JSON)))
	@echo "=> Syncing .env.production from the local deployment source..."
	@$(SCP) -O $(SSH_OPTIONS) -i $(SSH_KEY) .env.production ubuntu@$(VM_IP):/tmp/.env
	@$(SCP) -O $(SSH_OPTIONS) -i $(SSH_KEY) $(FIREBASE_JSON) ubuntu@$(VM_IP):/tmp/$(FIREBASE_JSON_NAME)
	@$(SCP) -O $(SSH_OPTIONS) -i $(SSH_KEY) $(JWT_PRIVATE_KEY) ubuntu@$(VM_IP):/tmp/atlashub-jwt-private.pem
	@$(SCP) -O $(SSH_OPTIONS) -i $(SSH_KEY) $(JWT_PUBLIC_KEY) ubuntu@$(VM_IP):/tmp/atlashub-jwt-public.pem
	@echo "=> Ensuring Docker Image exists in K3s..."
	@$(SSH) $(SSH_OPTIONS) -i $(SSH_KEY) ubuntu@$(VM_IP) "sudo k3s ctr images ls | grep -q atlashub/app:latest" || $(MAKE) build-image
	@echo "=> Applying Kubernetes Secrets..."
	$(SSH) $(SSH_OPTIONS) -i $(SSH_KEY) ubuntu@$(VM_IP) "export KUBECONFIG=/home/ubuntu/.kube/config && kubectl delete secret atlashub-secrets firebase-secrets jwt-signing-keys --ignore-not-found && kubectl create secret generic atlashub-secrets --from-env-file=/tmp/.env && kubectl create secret generic firebase-secrets --from-file=/tmp/$(FIREBASE_JSON_NAME) && kubectl create secret generic jwt-signing-keys --from-file=private.pem=/tmp/atlashub-jwt-private.pem --from-file=public.pem=/tmp/atlashub-jwt-public.pem"
	@echo "=> Applying Kubernetes Manifests..."
	$(SSH) $(SSH_OPTIONS) -i $(SSH_KEY) ubuntu@$(VM_IP) "if [ ! -d 'atlashub' ]; then git clone https://github.com/OlamidotunIY/atlashub_backend.git atlashub; fi && cd atlashub && git checkout master && git pull origin master"
	$(SSH) $(SSH_OPTIONS) -i $(SSH_KEY) ubuntu@$(VM_IP) "export KUBECONFIG=/home/ubuntu/.kube/config && kubectl apply -k atlashub/infrastructure/k8s/base"
	@echo "=> Waiting for MySQL to boot and accept connections..."
	@$(SSH) $(SSH_OPTIONS) -i $(SSH_KEY) ubuntu@$(VM_IP) "export KUBECONFIG=/home/ubuntu/.kube/config && while true; do POD=$$(kubectl get pod -l app=mysql -o jsonpath='{.items[0].metadata.name}' 2>/dev/null); if [ -n \"$$POD\" ]; then if kubectl exec $$POD -- mysqladmin ping -u root -proot --silent 2>/dev/null; then break; fi; fi; echo 'Waiting for MySQL container...'; sleep 5; done; echo 'MySQL is fully ready!'"
	@echo "=> Restoring Database..."
	$(MAKE) restore-db
	@echo "=> Rolling out the AtlasHub application..."
	$(SSH) $(SSH_OPTIONS) -i $(SSH_KEY) ubuntu@$(VM_IP) "export KUBECONFIG=/home/ubuntu/.kube/config && kubectl rollout restart deployment/atlashub-app && kubectl rollout status deployment/atlashub-app --timeout=180s"
	$(MAKE) prune-runtime-images
	@echo "=> Environment is completely up and running!"

build-image:
	@echo "=> Building Docker Image and Loading into K3s..."
	$(SSH) $(SSH_OPTIONS) -i $(SSH_KEY) ubuntu@$(VM_IP) "if [ ! -d 'atlashub' ]; then git clone https://github.com/OlamidotunIY/atlashub_backend.git atlashub; fi && cd atlashub && git checkout master && git pull origin master && sudo docker build -t atlashub/app:latest . && sudo docker save atlashub/app:latest | sudo k3s ctr images import -"

prune-runtime-images:
	@echo "=> Pruning unused Docker build cache and K3s images..."
	$(SSH) $(SSH_OPTIONS) -i $(SSH_KEY) ubuntu@$(VM_IP) "sudo docker builder prune -af && sudo docker image prune -af && sudo k3s crictl rmi --prune"

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

# Stream API logs from every AtlasHub application pod. Press Ctrl+C to stop.
# Override the namespace or initial history when needed:
# make api-logs API_NAMESPACE=production API_LOG_TAIL=500
api-logs:
	@echo "=> Streaming AtlasHub API logs from namespace $(API_NAMESPACE)..."
	$(SSH) $(SSH_OPTIONS) -i $(SSH_KEY) ubuntu@$(VM_IP) "export KUBECONFIG=/home/ubuntu/.kube/config && kubectl logs -n $(API_NAMESPACE) -f -l app=atlashub-app --all-containers=true --tail=$(API_LOG_TAIL) --prefix=true --max-log-requests=10"

# Open a local MySQL tunnel directly to the Kubernetes service ClusterIP.
# Override the local port when needed: make mysql-tunnel MYSQL_TUNNEL_PORT=3308
mysql-tunnel:
	$(if $(strip $(MYSQL_SERVICE_IP)),,$(error Unable to resolve the Kubernetes MySQL service IP))
	@echo "=> Forwarding local port $(MYSQL_TUNNEL_PORT) to $(MYSQL_SERVICE):$(MYSQL_SERVICE_PORT) in namespace $(MYSQL_NAMESPACE)..."
	@echo "=> Keep this terminal open; press Ctrl+C to close the tunnel."
	$(SSH) $(SSH_OPTIONS) -o ExitOnForwardFailure=yes -N -i $(SSH_KEY) -L $(MYSQL_TUNNEL_PORT):$(MYSQL_SERVICE_IP):$(MYSQL_SERVICE_PORT) ubuntu@$(VM_IP)

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
