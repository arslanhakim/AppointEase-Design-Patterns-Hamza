package com.appointease.domain.addon;
import com.appointease.domain.model.*;
import com.appointease.domain.exception.DomainValidationException;
import java.util.*;
/** Immutable quote with ordered instructions for a future event consumer. */
public record BookingEnhancements(Money baseFee, BookingOptions options, List<String> instructions) {
    public BookingEnhancements {
        Validation.required(baseFee, "base fee");
        Validation.required(options, "options");
        Validation.required(instructions, "instructions");
        instructions = instructions.stream().map(s -> Validation.text(s, "instruction")).toList();
    }
    public Money totalFee() { return baseFee.plus(options.totalAdditionalFee()); }
    public BookingEnhancements with(String code, Money fee, String instruction) {
        var fees = new LinkedHashMap<>(options.additionalFees());
        String normalized = Validation.text(code, "add-on code");
        if (fees.putIfAbsent(normalized, Validation.required(fee, "fee")) != null) {
            throw new DomainValidationException("Duplicate add-on: " + normalized);
        }
        var messages = new ArrayList<>(instructions);
        messages.add(Validation.text(instruction, "instruction"));
        return new BookingEnhancements(baseFee, new BookingOptions(fees), messages);
    }
}
