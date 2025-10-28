package com.tsu.namespace.api.formatter;

import com.tsu.common.locale.EffectiveLocaleSettings;
import com.tsu.common.val.PlaceVal;
import com.tsu.place.api.Place;
import com.tsu.workspace.api.Formatter;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of Formatter with user/namespace fallback logic.
 * Uses EffectiveLocaleSettings for all formatting operations.
 */
public record FormatterImpl(EffectiveLocaleSettings localeSettings) implements Formatter {

    // ========== Date/Time Formatting ==========

    @Override
    public String formatDate(LocalDate date) {
        if (date == null) return "";
        return localeSettings.dateFormatter().format(date);
    }


    @Override
    public String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return localeSettings.dateTimeFormatter().format(dateTime);
    }



    @Override
    public String formatDateTime(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) return "";
        if (pattern == null || pattern.isEmpty()) {
            return formatDateTime(dateTime);
        }
        try {
            return java.time.format.DateTimeFormatter.ofPattern(pattern, localeSettings.locale()).format(dateTime);
        } catch (Exception e) {
            // Fallback to default formatter if custom pattern fails
            return formatDateTime(dateTime);
        }
    }

    // ========== Number Formatting ==========

    @Override
    public String formatNumber(Number number) {
        if (number == null) return "";
        NumberFormat formatter = NumberFormat.getNumberInstance(localeSettings.locale());
        return formatter.format(number);
    }

    @Override
    public String formatNumber(Number number, int decimalPlaces) {
        if (number == null) return "";
        NumberFormat formatter = NumberFormat.getNumberInstance(localeSettings.locale());
        formatter.setMinimumFractionDigits(decimalPlaces);
        formatter.setMaximumFractionDigits(decimalPlaces);
        return formatter.format(number);
    }

    @Override
    public String formatPercentage(double value) {
        return formatPercentage(value, 0);
    }

    @Override
    public String formatPercentage(double value, int decimalPlaces) {
        NumberFormat formatter = NumberFormat.getPercentInstance(localeSettings.locale());
        formatter.setMinimumFractionDigits(decimalPlaces);
        formatter.setMaximumFractionDigits(decimalPlaces);
        return formatter.format(value);
    }


    // ========== Address Formatting ==========

    @Override
    public Stream<String> formatAddress(Place place) {
        if (place == null) return Stream.empty();
        PlaceVal val = place.getValue();
        return Stream.of(
                val.building(),
                val.address(),
                val.city(),
                val.county(),
                val.postCode(),
                val.country()
        ).filter(s -> s != null && !s.isEmpty());
    }

    @Override
    public String formatAddress(Place place, String joiningChar) {
        if (place == null) return "";
        String separator = joiningChar != null ? joiningChar : ", ";
        return formatAddress(place).collect(Collectors.joining(separator));
    }

    @Override
    public String formatAddressWithPattern(Place place) {
        if (place == null) return "";

        String pattern = localeSettings.locationPattern();
        if (pattern == null || pattern.isEmpty()) {
            return formatAddress(place, ", ");
        }
        PlaceVal val = place.getValue();
        String result = pattern;
        result = replacePlaceholder(result, "{building}", val.building());
        result = replacePlaceholder(result, "{address}", val.address());
        result = replacePlaceholder(result, "{city}", val.city());
        result = replacePlaceholder(result, "{county}", val.county());
        result = replacePlaceholder(result, "{postCode}", val.postCode());
        result = replacePlaceholder(result, "{country}", val.country());

        // Clean up any double separators or leading/trailing separators
        result = result.replaceAll(",\\s*,", ",");
        result = result.replaceAll("^[,\\s]+|[,\\s]+$", "");
        result = result.replaceAll("\\s+", " ");

        return result;
    }

    /**
     * Replace placeholder with value, handling nulls gracefully.
     */
    private String replacePlaceholder(String pattern, String placeholder, String value) {
        if (value == null || value.isEmpty()) {
            // Remove placeholder and any adjacent separator
            return pattern
                    .replace(placeholder + ", ", "")
                    .replace(", " + placeholder, "")
                    .replace(placeholder, "");
        }
        return pattern.replace(placeholder, value);
    }

    // ========== Utility Methods ==========

    @Override
    public String getLanguageTag() {
        return localeSettings.getLanguageTag();
    }

    @Override
    public String getTimezoneId() {
        return localeSettings.getTimezoneId();
    }

    @Override
    public boolean isRightToLeft() {
        return localeSettings.isRightToLeft();
    }

    @Override
    public char getDecimalSeparator() {
        return localeSettings.getDecimalSeparator();
    }

    @Override
    public char getGroupingSeparator() {
        return localeSettings.getGroupingSeparator();
    }
}
