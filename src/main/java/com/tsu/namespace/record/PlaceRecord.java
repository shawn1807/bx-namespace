package com.tsu.namespace.record;

import com.tsu.namespace.entities.PlaceTb;
import com.tsu.common.val.PlaceVal;
import com.tsu.common.jpa.JsonValueUtils;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Optional;
import java.util.function.Consumer;
import org.locationtech.jts.geom.Point;


@ToString(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor
public class PlaceRecord {

    @ToString.Include
    private final PlaceTb tb;
    private final Consumer<PlaceTb> persist;

    public void persist() {
        persist.accept(tb);
    }


    public PlaceVal getValue() {
        return new PlaceVal(tb.getId(), tb.getNotes(), tb.getCountry(),tb.getCounty(), tb.getCity(), tb.getBuilding(), tb.getPostCode(),
                tb.getAddress(), tb.getLat(), tb.getLng());
    }


    public Integer getId() {
        return tb.getId();
    }

    public void setNotes(String notes) {
        tb.setNotes(notes);
    }

    public <T> T getProps(Class<T> type) {
        return JsonValueUtils.getInstance().decode(tb.getProps(), type);
    }

    public void setProps(Object props) {
        tb.setProps(JsonValueUtils.getInstance().encodeAsJson(props));
    }

    public void setCountry(String country) {
        if (country != null && country.length() != 2) {
            throw new IllegalArgumentException("Country code must be 2 characters (ISO 3166-1 alpha-2)");
        }
        tb.setCountry(country != null ? country.toUpperCase() : null);
    }

    public void setCounty(String county) {
        tb.setCounty(county);
    }

    public void setCity(String city) {
        tb.setCity(city);
    }

    public void setBuilding(String building) {
        tb.setBuilding(building);
    }

    public void setAddress(String address) {
        tb.setAddress(address);
    }

    public void setPostCode(String postCode) {
        tb.setPostCode(postCode);
    }

    public void setLat(double lat) {
        tb.setLat(lat);
    }

    public void setLng(double lng) {
        tb.setLng(lng);
    }

    public Point getGeometry() {
        return tb.getGeom();
    }

    public void setGeometry(Point geometry) {
        tb.setGeom(geometry);
    }

    public boolean hasGeometry() {
        return tb.getGeom() != null;
    }

    public Optional<Double> getLongitude() {
        return tb.getGeom() != null && tb.getGeom().getCoordinate() != null
                ? Optional.of(tb.getGeom().getCoordinate().getX())
                : Optional.empty();
    }

    public Optional<Double> getLatitude() {
        return tb.getGeom() != null && tb.getGeom().getCoordinate() != null
                ? Optional.of(tb.getGeom().getCoordinate().getY())
                : Optional.empty();
    }
}
