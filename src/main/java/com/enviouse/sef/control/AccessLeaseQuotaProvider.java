package com.enviouse.sef.control;

import com.enviouse.sef.kernel.policy.QuotaService;

import java.util.Objects;
import java.util.Set;

public final class AccessLeaseQuotaProvider implements QuotaService.Provider {
    private final AccessLeaseRepository repository;

    public AccessLeaseQuotaProvider(AccessLeaseRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    @Override
    public String id() {
        return "access_lease";
    }

    @Override
    public int priority() {
        return 150;
    }

    @Override
    public QuotaService.Candidate resolve(
            QuotaService.Definition definition,
            QuotaService.Context context
    ) {
        var scope = new AccessLeaseRepository.ScopeContext(
                context.worldId(),
                context.dimensionId(),
                "",
                Set.of());
        return repository.quota(context.subjectId(), definition.id(), scope)
                .map(value -> QuotaService.Candidate.finite(
                        Math.min(value, definition.hardCeiling()),
                        id(),
                        definition.id()))
                .orElse(null);
    }
}
