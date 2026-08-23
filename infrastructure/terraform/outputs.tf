output "k3s_vm_public_ip" {
  description = "Public IP of the K3s VM"
  value       = oci_core_instance.k3s_node.public_ip
}

output "k3s_vm_private_ip" {
  description = "Private IP of the K3s VM"
  value       = oci_core_instance.k3s_node.private_ip
}
