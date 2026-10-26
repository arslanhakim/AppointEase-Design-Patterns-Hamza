package com.appointease.domain.model;

import com.appointease.domain.exception.DomainValidationException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record ServiceProvider(ProviderId id, String name, ProviderCategory category,
        Set<ServiceId> serviceIds, Set<AppointmentMode> modes,
        Map<DayOfWeek, List<WorkingPeriod>> workingHours, Duration preparationBuffer) {
    public ServiceProvider {
        Validation.required(id, "provider ID");
        name = Validation.text(name, "provider name");
        Validation.required(category, "category");
        Validation.required(serviceIds, "supported services");
        Validation.required(modes, "supported modes");
        if (serviceIds.isEmpty() || serviceIds.stream().anyMatch(java.util.Objects::isNull)
                || modes.isEmpty() || modes.stream().anyMatch(java.util.Objects::isNull)) {
            throw new DomainValidationException("Provider requires nonnull supported services and modes");
        }
        serviceIds = Set.copyOf(serviceIds);
        modes = Set.copyOf(modes);
        Validation.required(workingHours, "working hours");
        Map<DayOfWeek, List<WorkingPeriod>> copy = new EnumMap<>(DayOfWeek.class);
        workingHours.forEach((day, periods) -> {
            Validation.required(day, "working day");
            Validation.required(periods, "working periods");
            periods.forEach(period -> Validation.required(period, "working period"));
            List<WorkingPeriod> ordered = periods.stream()
                    .sorted(java.util.Comparator.comparing(WorkingPeriod::start)).toList();
            for (int i = 1; i < ordered.size(); i++) {
                if (ordered.get(i).start().isBefore(ordered.get(i - 1).end())) {
                    throw new DomainValidationException("Provider working periods must not overlap");
                }
            }
            copy.put(day, ordered);
        });
        if (copy.values().stream().allMatch(List::isEmpty)) {
            throw new DomainValidationException("Provider requires working hours");
        }
        workingHours = Map.copyOf(copy);
        if (Validation.required(preparationBuffer, "preparation buffer").isNegative()) {
            throw new DomainValidationException("Preparation buffer must not be negative");
        }
    }

    public boolean supports(Service service, AppointmentMode mode) {
        return serviceIds.contains(Validation.required(service, "service").id())
                && modes.contains(Validation.required(mode, "mode"));
    }
}
