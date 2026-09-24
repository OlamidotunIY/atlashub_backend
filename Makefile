TF_DIR = infrastructure/terraform

ifeq ($(OS),Windows_NT)
    SSH = C:\Windows\System32\OpenSSH\ssh.exe
    CAT = type
else
    SSH = ssh
    CAT = cat
endif

.PHONY: start stop backup-db restore-db ssh

# 1. Build the infrastructure from scratch and restore the database
start:
	@echo "=> Building Azure infrastructure..."
	cd $(TF_DIR) && terraform init -upgrade && terraform apply -auto-approve
	@echo "=> Waiting for VM and Docker to initialize (sleeping for 90s)..."
	timeout /t 90 /nobreak
	@echo "=> Deploying Backend Stack (Cloning & Building via SSH)..."
	$(SSH) -i ~/.ssh/id_rsa_azure -o StrictHostKeyChecking=no ubuntu@$$(cd $(TF_DIR) && terraform output -raw k3s_vm_public_ip) \
		"if [ ! -d 'atlashub' ]; then git clone https://github.com/OlamidotunIY/atlashub_backend.git atlashub; fi && \
		 cd atlashub && git pull && \
		 cd infrastructure/docker && docker compose -f docker-compose.prod.yml up -d --build"
	@echo "=> Waiting 30s for MySQL to boot..."
	timeout /t 30 /nobreak
	@echo "=> Restoring Database..."
	$(MAKE) restore-db
	@echo "=> Environment is completely up and running!"

# 2. Backup the database and completely destroy the infrastructure
stop:
	@echo "=> Backing up the database to local machine..."
	$(MAKE) backup-db
	@echo "=> Destroying all Azure infrastructure (including disks)..."
	cd $(TF_DIR) && terraform destroy -auto-approve
	@echo "=> Environment destroyed. Billing is now $$0."

# 3. SSH directly into the VM
ssh:
	@echo "=> Connecting to VM..."
	$(SSH) -i ~/.ssh/id_rsa_azure -o StrictHostKeyChecking=no ubuntu@$$(cd $(TF_DIR) && terraform output -raw k3s_vm_public_ip)

# -----------------------------------------------------------------------------
# DATABASE BACKUP & RESTORE COMMANDS
# You will need to replace the commands below with the exact commands for your 
# database (Postgres, MySQL, etc.) and how you expose it in Kubernetes.
# -----------------------------------------------------------------------------

backup-db:
	@echo "=> Running MySQL Backup via SSH..."
	@echo "=> Connecting to the VM, running mysqldump inside the docker container, and saving it locally."
	$(SSH) -i ~/.ssh/id_rsa_azure -o StrictHostKeyChecking=no ubuntu@$$(cd $(TF_DIR) && terraform output -raw k3s_vm_public_ip) \
		"docker exec atlashub-mysql mysqldump -u root -proot atlashub" > atlashub_backup.sql

restore-db:
	@echo "=> Restoring MySQL Backup via SSH..."
	@echo "=> Sending the local SQL file to the VM and piping it into the MySQL docker container."
	$(CAT) atlashub_backup.sql | $(SSH) -i ~/.ssh/id_rsa_azure -o StrictHostKeyChecking=no ubuntu@$$(cd $(TF_DIR) && terraform output -raw k3s_vm_public_ip) \
		"docker exec -i atlashub-mysql mysql -u root -proot atlashub"
