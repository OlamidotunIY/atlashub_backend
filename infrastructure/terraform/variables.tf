variable "tenancy_ocid" {
  description = "OCI Tenancy OCID"
  type        = string
}

variable "user_ocid" {
  description = "OCI User OCID"
  type        = string
}

variable "fingerprint" {
  description = "OCI API Key Fingerprint"
  type        = string
}

variable "private_key_path" {
  description = "Path to OCI API Private Key"
  type        = string
}

variable "region" {
  description = "OCI Region (e.g., us-ashburn-1)"
  type        = string
}

variable "compartment_ocid" {
  description = "OCI Compartment OCID where resources will be created"
  type        = string
}

variable "ssh_public_key" {
  description = "SSH Public Key string for accessing the VM"
  type        = string
}

variable "instance_shape" {
  description = "Compute Instance Shape (Always Free ARM)"
  type        = string
  default     = "VM.Standard.A1.Flex"
}

variable "instance_ocpus" {
  description = "Number of OCPUs (Max 4 for free tier)"
  type        = number
  default     = 2
}

variable "instance_memory_in_gbs" {
  description = "Memory in GBs (Max 24 for free tier)"
  type        = number
  default     = 12
}
