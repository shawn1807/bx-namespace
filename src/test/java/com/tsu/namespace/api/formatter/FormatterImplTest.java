package com.tsu.namespace.api.formatter;

import com.tsu.common.locale.EffectiveLocaleSettings;
import com.tsu.place.api.Place;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Comprehensive unit tests for FormatterImpl.
 * Tests date/time formatting, number formatting, currency formatting, and address formatting
 * with various locale settings.
 */
@DisplayName("FormatterImpl Tests")
class FormatterImplTest {

    @Nested
    @DisplayName("US Locale Tests")
    class USLocaleTests {

        private FormatterImpl formatter;
        private EffectiveLocaleSettings usSettings;

        @BeforeEach
        void setUp() {
            usSettings = EffectiveLocaleSettings.US_DEFAULT;
            formatter = new FormatterImpl(usSettings);
        }

        @Test
        @DisplayName("Should format date in US format (MM/dd/yyyy)")
        void testFormatDate() {
            LocalDate date = LocalDate.of(2025, 10, 24);
            String formatted = formatter.formatDate(date);
            assertEquals("10/24/2025", formatted);
        }

        @Test
        @DisplayName("Should format time in US format (hh:mm a)")
        void testFormatTime() {
            LocalTime time = LocalTime.of(14, 30);
            String formatted = formatter.formatTime(time);
            assertEquals("02:30 PM", formatted);
        }

        @Test
        @DisplayName("Should format datetime in US format")
        void testFormatDateTime() {
            LocalDateTime dateTime = LocalDateTime.of(2025, 10, 24, 14, 30);
            String formatted = formatter.formatDateTime(dateTime);
            assertEquals("10/24/2025 02:30 PM", formatted);
        }

        @Test
        @DisplayName("Should format datetime with custom pattern")
        void testFormatDateTimeCustomPattern() {
            LocalDateTime dateTime = LocalDateTime.of(2025, 10, 24, 14, 30);
            String formatted = formatter.formatDateTime(dateTime, "yyyy-MM-dd HH:mm");
            assertEquals("2025-10-24 14:30", formatted);
        }

        @Test
        @DisplayName("Should format number with grouping separator")
        void testFormatNumber() {
            String formatted = formatter.formatNumber(1234567.89);
            assertTrue(formatted.contains("1,234,567"));
        }

        @Test
        @DisplayName("Should format number with specified decimal places")
        void testFormatNumberWithDecimals() {
            String formatted = formatter.formatNumber(1234.5, 2);
            assertTrue(formatted.contains("1,234.50"));
        }

        @Test
        @DisplayName("Should format percentage")
        void testFormatPercentage() {
            String formatted = formatter.formatPercentage(0.85);
            assertTrue(formatted.contains("85"));
        }

        @Test
        @DisplayName("Should format percentage with decimal places")
        void testFormatPercentageWithDecimals() {
            String formatted = formatter.formatPercentage(0.8567, 2);
            assertTrue(formatted.contains("85.67"));
        }

        @Test
        @DisplayName("Should format currency in USD")
        void testFormatCurrency() {
            BigDecimal amount = new BigDecimal("1234.56");
            String formatted = formatter.formatCurrency(amount);
            assertTrue(formatted.contains("1,234.56"));
            assertTrue(formatted.contains("$"));
        }

        @Test
        @DisplayName("Should format currency with explicit currency code")
        void testFormatCurrencyWithCode() {
            BigDecimal amount = new BigDecimal("1234.56");
            String formatted = formatter.formatCurrency(amount, "EUR");
            assertTrue(formatted.contains("1,234.56"));
        }

        @Test
        @DisplayName("Should get USD currency symbol")
        void testGetCurrencySymbol() {
            String symbol = formatter.getCurrencySymbol();
            assertEquals("$", symbol);
        }

        @Test
        @DisplayName("Should get USD currency code")
        void testGetCurrencyCode() {
            String code = formatter.getCurrencyCode();
            assertEquals("USD", code);
        }

        @Test
        @DisplayName("Should format US address with pattern")
        void testFormatUSAddress() {
            Place place = createMockPlace("Suite 100", "123 Main St", "New York", "Manhattan", "10001", "USA");
            String formatted = formatter.formatAddressWithPattern(place);
            assertTrue(formatted.contains("Suite 100"));
            assertTrue(formatted.contains("123 Main St"));
            assertTrue(formatted.contains("New York"));
            assertTrue(formatted.contains("10001"));
        }

        @Test
        @DisplayName("Should get en-US language tag")
        void testGetLanguageTag() {
            assertEquals("en-US", formatter.getLanguageTag());
        }

        @Test
        @DisplayName("Should get America/New_York timezone")
        void testGetTimezoneId() {
            assertEquals("America/New_York", formatter.getTimezoneId());
        }

        @Test
        @DisplayName("Should not be RTL for US locale")
        void testIsNotRightToLeft() {
            assertFalse(formatter.isRightToLeft());
        }

        @Test
        @DisplayName("Should have period as decimal separator")
        void testDecimalSeparator() {
            assertEquals('.', formatter.getDecimalSeparator());
        }

        @Test
        @DisplayName("Should have comma as grouping separator")
        void testGroupingSeparator() {
            assertEquals(',', formatter.getGroupingSeparator());
        }
    }

    @Nested
    @DisplayName("Taiwan Locale Tests")
    class TaiwanLocaleTests {

        private FormatterImpl formatter;
        private EffectiveLocaleSettings taiwanSettings;

        @BeforeEach
        void setUp() {
            taiwanSettings = EffectiveLocaleSettings.TAIWAN_DEFAULT;
            formatter = new FormatterImpl(taiwanSettings);
        }

        @Test
        @DisplayName("Should format date in Taiwan format (yyyy/MM/dd)")
        void testFormatDate() {
            LocalDate date = LocalDate.of(2025, 10, 24);
            String formatted = formatter.formatDate(date);
            assertEquals("2025/10/24", formatted);
        }

        @Test
        @DisplayName("Should format time in 24-hour format")
        void testFormatTime() {
            LocalTime time = LocalTime.of(14, 30);
            String formatted = formatter.formatTime(time);
            assertEquals("14:30", formatted);
        }

        @Test
        @DisplayName("Should format currency in TWD")
        void testFormatCurrency() {
            BigDecimal amount = new BigDecimal("1234.56");
            String formatted = formatter.formatCurrency(amount);
            assertTrue(formatted.contains("1,234.56") || formatted.contains("1234.56"));
        }

        @Test
        @DisplayName("Should get TWD currency code")
        void testGetCurrencyCode() {
            String code = formatter.getCurrencyCode();
            assertEquals("TWD", code);
        }

        @Test
        @DisplayName("Should get zh-TW language tag")
        void testGetLanguageTag() {
            assertEquals("zh-TW", formatter.getLanguageTag());
        }

        @Test
        @DisplayName("Should get Asia/Taipei timezone")
        void testGetTimezoneId() {
            assertEquals("Asia/Taipei", formatter.getTimezoneId());
        }

        @Test
        @DisplayName("Should format Taiwan address with reverse pattern")
        void testFormatTaiwanAddress() {
            Place place = createMockPlace("大樓", "路123號", "台北市", "大安區", "106", "台灣");
            String formatted = formatter.formatAddressWithPattern(place);
            // Taiwan pattern: {country}{county}{city}{address}{building}
            assertTrue(formatted.contains("台灣"));
            assertTrue(formatted.contains("大安區"));
        }
    }

    @Nested
    @DisplayName("Japan Locale Tests")
    class JapanLocaleTests {

        private FormatterImpl formatter;
        private EffectiveLocaleSettings japanSettings;

        @BeforeEach
        void setUp() {
            japanSettings = EffectiveLocaleSettings.JAPAN_DEFAULT;
            formatter = new FormatterImpl(japanSettings);
        }

        @Test
        @DisplayName("Should format date in Japanese format with kanji")
        void testFormatDate() {
            LocalDate date = LocalDate.of(2025, 10, 24);
            String formatted = formatter.formatDate(date);
            assertTrue(formatted.contains("2025年10月24日"));
        }

        @Test
        @DisplayName("Should format currency in JPY (no decimals)")
        void testFormatCurrency() {
            BigDecimal amount = new BigDecimal("1234.56");
            String formatted = formatter.formatCurrency(amount);
            // JPY has 0 decimal places, should round to 1235
            assertTrue(formatted.contains("1,235") || formatted.contains("1235"));
        }

        @Test
        @DisplayName("Should get JPY currency code")
        void testGetCurrencyCode() {
            String code = formatter.getCurrencyCode();
            assertEquals("JPY", code);
        }

        @Test
        @DisplayName("Should get ja-JP language tag")
        void testGetLanguageTag() {
            assertEquals("ja-JP", formatter.getLanguageTag());
        }

        @Test
        @DisplayName("Should get Asia/Tokyo timezone")
        void testGetTimezoneId() {
            assertEquals("Asia/Tokyo", formatter.getTimezoneId());
        }
    }

    @Nested
    @DisplayName("UK Locale Tests")
    class UKLocaleTests {

        private FormatterImpl formatter;
        private EffectiveLocaleSettings ukSettings;

        @BeforeEach
        void setUp() {
            ukSettings = EffectiveLocaleSettings.UK_DEFAULT;
            formatter = new FormatterImpl(ukSettings);
        }

        @Test
        @DisplayName("Should format date in UK format (dd/MM/yyyy)")
        void testFormatDate() {
            LocalDate date = LocalDate.of(2025, 10, 24);
            String formatted = formatter.formatDate(date);
            assertEquals("24/10/2025", formatted);
        }

        @Test
        @DisplayName("Should format currency in GBP")
        void testFormatCurrency() {
            BigDecimal amount = new BigDecimal("1234.56");
            String formatted = formatter.formatCurrency(amount);
            assertTrue(formatted.contains("1,234.56"));
        }

        @Test
        @DisplayName("Should get GBP currency code")
        void testGetCurrencyCode() {
            String code = formatter.getCurrencyCode();
            assertEquals("GBP", code);
        }

        @Test
        @DisplayName("Should get en-GB language tag")
        void testGetLanguageTag() {
            assertEquals("en-GB", formatter.getLanguageTag());
        }

        @Test
        @DisplayName("Should get Europe/London timezone")
        void testGetTimezoneId() {
            assertEquals("Europe/London", formatter.getTimezoneId());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Null Handling")
    class EdgeCasesTests {

        private FormatterImpl formatter;

        @BeforeEach
        void setUp() {
            formatter = new FormatterImpl(EffectiveLocaleSettings.US_DEFAULT);
        }

        @Test
        @DisplayName("Should return empty string for null date")
        void testFormatNullDate() {
            String formatted = formatter.formatDate(null);
            assertEquals("", formatted);
        }

        @Test
        @DisplayName("Should return empty string for null time")
        void testFormatNullTime() {
            String formatted = formatter.formatTime(null);
            assertEquals("", formatted);
        }

        @Test
        @DisplayName("Should return empty string for null datetime")
        void testFormatNullDateTime() {
            String formatted = formatter.formatDateTime(null);
            assertEquals("", formatted);
        }

        @Test
        @DisplayName("Should return empty string for null number")
        void testFormatNullNumber() {
            String formatted = formatter.formatNumber(null);
            assertEquals("", formatted);
        }

        @Test
        @DisplayName("Should return empty string for null currency amount")
        void testFormatNullCurrency() {
            String formatted = formatter.formatCurrency(null);
            assertEquals("", formatted);
        }

        @Test
        @DisplayName("Should fallback to default formatter for invalid pattern")
        void testInvalidDateTimePattern() {
            LocalDateTime dateTime = LocalDateTime.of(2025, 10, 24, 14, 30);
            String formatted = formatter.formatDateTime(dateTime, "invalid{{pattern");
            // Should fallback to default format
            assertNotNull(formatted);
            assertFalse(formatted.isEmpty());
        }

        @Test
        @DisplayName("Should fallback to default currency for invalid code")
        void testInvalidCurrencyCode() {
            BigDecimal amount = new BigDecimal("100.00");
            String formatted = formatter.formatCurrency(amount, "XXX");
            // Should fallback to default currency
            assertNotNull(formatted);
            assertTrue(formatted.contains("100"));
        }

        @Test
        @DisplayName("Should handle address with null fields gracefully")
        void testAddressWithNullFields() {
            Place place = createMockPlace(null, "123 Main St", "New York", null, "10001", "USA");
            String formatted = formatter.formatAddressWithPattern(place);
            assertTrue(formatted.contains("123 Main St"));
            assertTrue(formatted.contains("New York"));
            assertFalse(formatted.contains("null"));
        }

        @Test
        @DisplayName("Should return empty string for null place")
        void testNullPlace() {
            String formatted = formatter.formatAddressWithPattern(null);
            assertEquals("", formatted);
        }

        @Test
        @DisplayName("Should format address stream with joining character")
        void testFormatAddressWithJoiner() {
            Place place = createMockPlace("Suite 100", "123 Main St", "New York", "Manhattan", "10001", "USA");
            String formatted = formatter.formatAddress(place, " | ");
            assertTrue(formatted.contains(" | "));
            assertTrue(formatted.contains("123 Main St"));
        }

        @Test
        @DisplayName("Should format address stream and collect")
        void testFormatAddressStream() {
            Place place = createMockPlace("Suite 100", "123 Main St", "New York", "Manhattan", "10001", "USA");
            String formatted = formatter.formatAddress(place)
                .collect(Collectors.joining(", "));
            assertTrue(formatted.contains("123 Main St"));
            assertTrue(formatted.contains("New York"));
        }
    }

    @Nested
    @DisplayName("RTL Language Tests")
    class RTLTests {

        @Test
        @DisplayName("Should detect Arabic as RTL")
        void testArabicRTL() {
            EffectiveLocaleSettings arabicSettings = new EffectiveLocaleSettings(
                Currency.getInstance("USD"),
                Locale.forLanguageTag("ar-SA"),
                ZoneId.of("Asia/Riyadh"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("HH:mm"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
                "{building}, {address}, {city}, {postCode}, {country}"
            );
            FormatterImpl formatter = new FormatterImpl(arabicSettings);
            assertTrue(formatter.isRightToLeft());
        }

        @Test
        @DisplayName("Should detect Hebrew as RTL")
        void testHebrewRTL() {
            EffectiveLocaleSettings hebrewSettings = new EffectiveLocaleSettings(
                Currency.getInstance("ILS"),
                Locale.forLanguageTag("he-IL"),
                ZoneId.of("Asia/Jerusalem"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("HH:mm"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
                "{building}, {address}, {city}, {postCode}, {country}"
            );
            FormatterImpl formatter = new FormatterImpl(hebrewSettings);
            assertTrue(formatter.isRightToLeft());
        }
    }

    @Nested
    @DisplayName("Decimal and Grouping Separator Tests")
    class SeparatorTests {

        @Test
        @DisplayName("Should use comma as decimal separator for German locale")
        void testGermanDecimalSeparator() {
            EffectiveLocaleSettings germanSettings = new EffectiveLocaleSettings(
                Currency.getInstance("EUR"),
                Locale.GERMANY,
                ZoneId.of("Europe/Berlin"),
                DateTimeFormatter.ofPattern("dd.MM.yyyy"),
                DateTimeFormatter.ofPattern("HH:mm"),
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"),
                "{address}, {postCode} {city}, {country}"
            );
            FormatterImpl formatter = new FormatterImpl(germanSettings);
            assertEquals(',', formatter.getDecimalSeparator());
        }

        @Test
        @DisplayName("Should use period as grouping separator for German locale")
        void testGermanGroupingSeparator() {
            EffectiveLocaleSettings germanSettings = new EffectiveLocaleSettings(
                Currency.getInstance("EUR"),
                Locale.GERMANY,
                ZoneId.of("Europe/Berlin"),
                DateTimeFormatter.ofPattern("dd.MM.yyyy"),
                DateTimeFormatter.ofPattern("HH:mm"),
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"),
                "{address}, {postCode} {city}, {country}"
            );
            FormatterImpl formatter = new FormatterImpl(germanSettings);
            assertEquals('.', formatter.getGroupingSeparator());
        }
    }

    // Helper method to create a mock Place
    private static Place createMockPlace(String building, String address, String city,
                                        String county, String postCode, String country) {
        Place place = Mockito.mock(Place.class);
        when(place.getBuilding()).thenReturn(building);
        when(place.getAddress()).thenReturn(address);
        when(place.getCity()).thenReturn(city);
        when(place.getCounty()).thenReturn(county);
        when(place.getPostCode()).thenReturn(postCode);
        when(place.getCountry()).thenReturn(country);
        return place;
    }
}
