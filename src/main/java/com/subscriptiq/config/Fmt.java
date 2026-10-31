package com.subscriptiq.config;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Component("fmt")
public class Fmt {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public String money(BigDecimal amount) {
        if (amount == null) {
            return "—";
        }
        BigDecimal value = amount.setScale(2, RoundingMode.HALF_UP);
        String plain = value.abs().toPlainString();
        int decimalAt = plain.indexOf('.');
        String whole = decimalAt < 0 ? plain : plain.substring(0, decimalAt);
        String decimals = decimalAt < 0 ? "00" : plain.substring(decimalAt + 1);
        if (whole.length() > 3) {
            int leadingLength = whole.length() - 3;
            int firstGroupLength = leadingLength % 2;
            if (firstGroupLength == 0) {
                firstGroupLength = 2;
            }
            StringBuilder grouped = new StringBuilder(whole.substring(0, firstGroupLength));
            for (int start = firstGroupLength; start < leadingLength; start += 2) {
                grouped.append(',').append(whole, start, start + 2);
            }
            whole = grouped.append(',').append(whole.substring(leadingLength)).toString();
        }
        return "₹" + (value.signum() < 0 ? "-" : "") + whole + "." + decimals;
    }

    public String date(LocalDate date) {
        return Objects.isNull(date) ? "—" : DATE_FORMAT.format(date);
    }
}
