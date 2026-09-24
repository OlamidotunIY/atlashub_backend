variable "location" {
  description = "Azure Region (Sweden Central has excellent capacity and prices)"
  type        = string
  default     = "swedencentral"
}

variable "resource_group_name" {
  description = "Name of the resource group"
  type        = string
  default     = "atlashub-k3s-rg"
}

variable "ssh_public_key" {
  description = "SSH Public Key string for accessing the VM"
  type        = string
}

variable "vm_size" {
  description = "Azure VM Size (AMD Burstable tier - excellent for multiple backends on a budget)"
  type        = string
  default     = "Standard_B4as_v2" # 4 vCPUs, 16 GB RAM ($113/mo)
}
