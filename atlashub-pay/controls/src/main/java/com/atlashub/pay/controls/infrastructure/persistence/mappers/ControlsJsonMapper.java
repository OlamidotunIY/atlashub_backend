package com.atlashub.pay.controls.infrastructure.persistence.mappers;

import com.atlashub.pay.controls.domain.valueobject.AmountFrequencyRuleSet;
import com.atlashub.pay.controls.domain.valueobject.BeneficiaryRuleSet;
import com.atlashub.pay.controls.domain.valueobject.ControlReason;
import com.atlashub.pay.controls.domain.valueobject.EvidenceRequirement;
import com.atlashub.pay.controls.domain.valueobject.PermittedCategory;
import com.atlashub.pay.controls.domain.valueobject.ProviderControlCapability;
import com.atlashub.pay.controls.domain.valueobject.RestrictedCategory;
import com.atlashub.pay.controls.domain.valueobject.WithdrawalRuleSet;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class ControlsJsonMapper {

    private final ObjectMapper objectMapper;

    public ControlsJsonMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String permittedCategoriesToJson(Set<PermittedCategory> categories) {
        if (categories == null || categories.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(categories);
        } catch (Exception e) {
            return null;
        }
    }

    public Set<PermittedCategory> jsonToPermittedCategories(String json) {
        if (json == null || json.isBlank()) return Collections.emptySet();
        try {
            return objectMapper.readValue(json, new TypeReference<Set<PermittedCategory>>() {});
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }

    public String restrictedCategoriesToJson(Set<RestrictedCategory> categories) {
        if (categories == null || categories.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(categories);
        } catch (Exception e) {
            return null;
        }
    }

    public Set<RestrictedCategory> jsonToRestrictedCategories(String json) {
        if (json == null || json.isBlank()) return Collections.emptySet();
        try {
            return objectMapper.readValue(json, new TypeReference<Set<RestrictedCategory>>() {});
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }

    public String beneficiaryRulesToJson(BeneficiaryRuleSet rules) {
        if (rules == null) return null;
        try {
            return objectMapper.writeValueAsString(rules);
        } catch (Exception e) {
            return null;
        }
    }

    public BeneficiaryRuleSet jsonToBeneficiaryRules(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, BeneficiaryRuleSet.class);
        } catch (Exception e) {
            return null;
        }
    }

    public String withdrawalRulesToJson(WithdrawalRuleSet rules) {
        if (rules == null) return null;
        try {
            return objectMapper.writeValueAsString(rules);
        } catch (Exception e) {
            return null;
        }
    }

    public WithdrawalRuleSet jsonToWithdrawalRules(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, WithdrawalRuleSet.class);
        } catch (Exception e) {
            return null;
        }
    }

    public String amountFrequencyRulesToJson(AmountFrequencyRuleSet rules) {
        if (rules == null) return null;
        try {
            return objectMapper.writeValueAsString(rules);
        } catch (Exception e) {
            return null;
        }
    }

    public AmountFrequencyRuleSet jsonToAmountFrequencyRules(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, AmountFrequencyRuleSet.class);
        } catch (Exception e) {
            return null;
        }
    }

    public String providerCapabilitiesToJson(Set<ProviderControlCapability> capabilities) {
        if (capabilities == null || capabilities.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(capabilities);
        } catch (Exception e) {
            return null;
        }
    }

    public Set<ProviderControlCapability> jsonToProviderCapabilities(String json) {
        if (json == null || json.isBlank()) return Collections.emptySet();
        try {
            return objectMapper.readValue(json, new TypeReference<Set<ProviderControlCapability>>() {});
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }

    public String reasonCodesToJson(Set<ControlReason> codes) {
        if (codes == null || codes.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(codes);
        } catch (Exception e) {
            return null;
        }
    }

    public Set<ControlReason> jsonToReasonCodes(String json) {
        if (json == null || json.isBlank()) return Collections.emptySet();
        try {
            return objectMapper.readValue(json, new TypeReference<Set<ControlReason>>() {});
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }

    public String evidenceRequirementsToJson(List<EvidenceRequirement> requirements) {
        if (requirements == null || requirements.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(requirements);
        } catch (Exception e) {
            return null;
        }
    }

    public List<EvidenceRequirement> jsonToEvidenceRequirements(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<EvidenceRequirement>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public String stringListToJson(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> jsonToStringList(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
