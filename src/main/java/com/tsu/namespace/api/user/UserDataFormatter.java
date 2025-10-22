package com.tsu.namespace.api.user;

import com.tsu.namespace.api.Formatter;
import com.tsu.base.api.Place;
import com.tsu.namespace.record.UserRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

public class UserDataFormatter implements Formatter {

    private final UserRecord user;

    public UserDataFormatter(UserRecord user) {
        this.user = user;
    }


    @Override
    public String formatDate(LocalDate date) {
        return "";
    }

    @Override
    public String formatDateTime(LocalDateTime date) {
        return "";
    }

    @Override
    public Stream<String> formatAddress(Place place) {
        return Stream.empty();
    }

    @Override
    public String formatAddress(Place place, String joiningChar) {
        return "";
    }
}
