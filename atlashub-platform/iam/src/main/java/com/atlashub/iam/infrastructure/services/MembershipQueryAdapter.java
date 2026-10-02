package com.atlashub.iam.infrastructure.services;

import com.atlashub.iam.domain.entities.CustomRole;
import com.atlashub.iam.domain.entities.OrganizationMember;
import com.atlashub.iam.domain.entities.Permission;
import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.iam.domain.repositories.OrganizationMemberRepository;
import com.atlashub.iam.domain.repositories.PermissionRepository;
import com.atlashub.iam.domain.valueobject.MemberStatus;
import com.atlashub.shared.application.port.MembershipQueryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MembershipQueryAdapter implements MembershipQueryPort {
    private final OrganizationMemberRepository memberRepository;
    private final CustomRoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public MembershipQueryAdapter(OrganizationMemberRepository memberRepository,
                                  CustomRoleRepository roleRepository,
                                  PermissionRepository permissionRepository) {
        this.memberRepository = memberRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public boolean isMemberOf(Long userId, Long orgId) {
        return memberRepository.findByOrganizationIdAndUserId(orgId, userId)
                .map(member -> member.getStatus() == MemberStatus.ACTIVE)
                .orElse(false);
    }

    @Override
    public boolean isActiveOwner(Long userId, Long orgId) {
        return activeMember(userId, orgId)
                .flatMap(member -> roleRepository.findById(member.getCustomRoleId()))
                .map(CustomRole::isBuiltIn)
                .orElse(false);
    }

    @Override
    public Set<String> getPermissions(Long userId, Long orgId) {
        return activeMember(userId, orgId)
                .flatMap(member -> roleRepository.findById(member.getCustomRoleId()))
                .map(role -> role.isBuiltIn()
                        ? permissionRepository.findAllByActiveTrue()
                        : permissionRepository.findAllById(role.getPermissions()))
                .orElseGet(List::of)
                .stream()
                .filter(Permission::isActive)
                .map(Permission::getCode)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public MembershipStatus getMemberStatus(Long userId, Long orgId) {
        return memberRepository.findByOrganizationIdAndUserId(orgId, userId)
                .map(member -> MembershipStatus.valueOf(member.getStatus().name()))
                .orElse(null);
    }

    @Override
    public List<Long> listOrganizationIds(Long userId) {
        return memberRepository.findAllByUserId(userId).stream()
                .filter(member -> member.getStatus() == MemberStatus.ACTIVE)
                .map(OrganizationMember::getOrganizationId)
                .distinct()
                .toList();
    }

    private Optional<OrganizationMember> activeMember(Long userId, Long orgId) {
        return memberRepository.findByOrganizationIdAndUserId(orgId, userId)
                .filter(member -> member.getStatus() == MemberStatus.ACTIVE);
    }
}
