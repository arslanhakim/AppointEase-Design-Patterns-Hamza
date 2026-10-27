package com.appointease.infrastructure.seed;

import com.appointease.domain.model.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** Stable offline catalog. */
public record DemoCatalog(List<Customer> customers, List<ServiceProvider> providers, List<Service> services) {
    public DemoCatalog {
        customers = List.copyOf(customers);
        providers = List.copyOf(providers);
        services = List.copyOf(services);
    }
    public static DemoCatalog create() {
        var customers = List.of(new Customer(new CustomerId("customer-demo-001"), "Alex Morgan"),
                new Customer(new CustomerId("customer-demo-002"), "Sam Taylor"),
                new Customer(new CustomerId("customer-demo-003"), "Robin Lee"));
        var services = List.of(service("health", "Wellbeing consultation", 30, "40", AppointmentKind.CONSULTATION),
                service("tutor", "Language tutoring", 60, "25", AppointmentKind.CONSULTATION),
                service("consult", "Career consultation", 45, "60", AppointmentKind.CONSULTATION),
                service("salon", "Hair styling", 30, "20", AppointmentKind.SERVICE));
        var hours = new EnumMap<DayOfWeek, List<WorkingPeriod>>(DayOfWeek.class);
        for (var day : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
            hours.put(day, List.of(new WorkingPeriod(LocalTime.of(9, 0), LocalTime.of(17, 0))));
        }
        var providers = new ArrayList<ServiceProvider>();
        String[] codes = {"health", "tutor", "consult", "salon"};
        String[] names = {"Dana Green", "Jamie White", "Casey Brown", "Riley Stone"};
        ProviderCategory[] categories = {ProviderCategory.HEALTHCARE, ProviderCategory.TUTORING,
                ProviderCategory.CONSULTING, ProviderCategory.PERSONAL_SERVICES};
        for (int i = 0; i < codes.length; i++) {
            providers.add(new ServiceProvider(new ProviderId("provider-" + codes[i] + "-001"), names[i], categories[i],
                    Set.of(services.get(i).id()), i == 3 ? Set.of(AppointmentMode.IN_PERSON) : Set.of(AppointmentMode.values()),
                    hours, Duration.ofMinutes(15)));
        }
        return new DemoCatalog(customers, providers, services);
    }
    private static Service service(String code, String name, int minutes, String price, AppointmentKind kind) {
        return new Service(new ServiceId("service-" + code + "-001"), name, Duration.ofMinutes(minutes),
                new Money(new BigDecimal(price)), kind, switch (code) {
                    case "health" -> ProviderCategory.HEALTHCARE;
                    case "tutor" -> ProviderCategory.TUTORING;
                    case "salon" -> ProviderCategory.PERSONAL_SERVICES;
                    default -> ProviderCategory.CONSULTING;
                });
    }
}
