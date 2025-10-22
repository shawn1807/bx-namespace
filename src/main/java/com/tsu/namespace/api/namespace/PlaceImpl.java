package com.tsu.namespace.api.namespace;

import com.tsu.base.api.Namespace;
import com.tsu.base.api.Place;
import com.tsu.base.enums.NamespaceAction;
import com.tsu.namespace.record.PlaceRecord;
import com.tsu.base.val.PlaceVal;
import com.tsu.common.api.ActionPack;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

@Slf4j
@ToString(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor
public class PlaceImpl implements Place {

    private final Namespace namespace;
    @ToString.Include
    private final PlaceRecord record;


    @Override
    public Integer getId() {
        return record.getId();
    }

    @Override
    public void setNotes(String notes) {
        namespace.getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.SAVE_PLACE, Map.of("notes", notes)));
        record.setNotes(notes);
        record.persist();
    }


    @Override
    public void setAddress(String address) {
        namespace.getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.SAVE_PLACE,
                Map.of("address", address)));
        record.setAddress(address);
        record.persist();
    }


    @Override
    public void setBuilding(String building) {
        namespace.getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.SAVE_PLACE,
                Map.of("building", building)));
        record.setBuilding(building);
        record.persist();
    }


    @Override
    public void setGPSLocation(double lat, double lng) {
        namespace.getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.SAVE_PLACE,
                Map.of("lat", lat, "lng", lng)));
        record.setLat(lat);
        record.setLng(lng);
        record.persist();
    }

    @Override
    public void setAddress(String address, String building, String postCode) {
        namespace.getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.SAVE_PLACE,
                Map.of("building", building, "address", address, "postCode", postCode)));
        record.setBuilding(building);
        record.setAddress(address);
        record.setPostCode(postCode);
        record.persist();
    }

    @Override
    public void setAddress(String address, String postCode) {
        namespace.getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.SAVE_PLACE,
                Map.of("address", address, "postCode", postCode)));
        record.setAddress(address);
        record.setPostCode(postCode);
        record.persist();
    }

    @Override
    public void setAddress(String country, String county, String city, String building, String address, String postCode) {
        namespace.getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.SAVE_PLACE,
                Map.of("country", country, "county", county, "city", city, "building", building, "address", address, "postCode", postCode)));
        record.setCountry(country);
        record.setCounty(county);
        record.setCity(city);
        record.setBuilding(building);
        record.setAddress(address);
        record.setPostCode(postCode);
        record.persist();
    }


    @Override
    public void setProps(Object props) {
        namespace.getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.SAVE_PLACE, props));
        record.setProps(props);
        record.persist();
    }

    @Override
    public <T> Optional<T> getProps(Class<T> type) {
        return Optional.ofNullable(record.getProps(type));

    }

    @Override
    public PlaceVal getValue() {
        return record.getValue();
    }
}
