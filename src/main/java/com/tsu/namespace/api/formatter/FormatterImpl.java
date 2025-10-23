package com.tsu.namespace.api.formatter;

import com.tsu.common.locale.EffectiveLocaleSettings;
import com.tsu.place.api.Place;
import com.tsu.workspace.api.Formatter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of Formatter with user/namespace fallback logic.
 * Uses EffectiveLocaleSettings for all formatting operations.
 */
@RequiredArgsConstructor
public class FormatterImpl implements Formatter {

    private final EffectiveLocaleSettings localeSettings;

    @Override
    public EffectiveLocaleSettings getLocaleSettings() {
        return localeSettings;
    }

    // ========== Date/Time Formatting ==========

    @Override
    public String formatDate(LocalDate date) {
        if (date == null) return "";
        return localeSettings.dateFormatter().format(date);
    }

    @Override
    public String formatTime(LocalTime time) {
        if (time == null) return "";
        return localeSettings.timeFormatter().format(time);
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

    // ========== Currency Formatting ==========

    @Override
    public String formatCurrency(BigDecimal amount) {
        if (amount == null) return "";

        Currency currency = localeSettings.currency();
        NumberFormat formatter = NumberFormat.getCurrencyInstance(localeSettings.locale());
        formatter.setCurrency(currency);

        // Round to currency's default fraction digits
        int decimalPlaces = currency.getDefaultFractionDigits();
        BigDecimal rounded = amount.setScale(decimalPlaces, RoundingMode.HALF_UP);

        return formatter.format(rounded);
    }

    @Override
    public String formatCurrency(BigDecimal amount, String currencyCode) {
        if (amount == null) return "";
        if (currencyCode == null || currencyCode.isEmpty()) {
            return formatCurrency(amount);
        }

        try {
            Currency currency = Currency.getInstance(currencyCode);
            NumberFormat formatter = NumberFormat.getCurrencyInstance(localeSettings.locale());
            formatter.setCurrency(currency);

            int decimalPlaces = currency.getDefaultFractionDigits();
            BigDecimal rounded = amount.setScale(decimalPlaces, RoundingMode.HALF_UP);

            return formatter.format(rounded);
        } catch (IllegalArgumentException e) {
            // Invalid currency code, fallback to default
            return formatCurrency(amount);
        }
    }

    @Override
    public String getCurrencySymbol() {
        return localeSettings.getCurrencySymbol();
    }

    @Override
    public String getCurrencyCode() {
        return localeSettings.getCurrencyCode();
    }

    // ========== Address Formatting ==========

    @Override
    public Stream<String> formatAddress(Place place) {
        if (place == null) return Stream.empty();

        return Stream.of(
            place.getBuilding(),
            place.getAddress(),
            place.getCity(),
            place.getCounty(),
            place.getPostCode(),
            place.getCountry()
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

        String result = pattern;
        result = replacePlaceholder(result, "{building}", place.getBuilding());
        result = replacePlaceholder(result, "{address}", place.getAddress());
        result = replacePlaceholder(result, "{city}", place.getCity());
        result = replacePlaceholder(result, "{county}", place.getCounty());
        result = replacePlaceholder(result, "{postCode}", place.getPostCode());
        result = replacePlaceholder(result, "{country}", place.getCountry());

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
