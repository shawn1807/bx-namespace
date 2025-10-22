package com.tsu.namespace.api.manager;

import com.tsu.enums.BaseParamName;
import com.tsu.namespace.api.Namespace;
import com.tsu.namespace.api.namespace.NamespaceObjectFactory;
import com.tsu.place.api.Place;
import com.tsu.place.api.PlaceManager;
import com.tsu.namespace.api.namespace.PlaceImpl;
import com.tsu.auth.permissions.NamespaceAction;
import com.tsu.namespace.helper.PlaceDbHelper;
import com.tsu.namespace.record.PlaceRecord;
import com.tsu.workspace.request.AddPlace;
import com.tsu.common.api.ActionPack;
import com.tsu.common.utils.ParamValidator;
import com.tsu.auth.security.AppSecurityContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

@Slf4j
public class NamespacePlaceManager implements PlaceManager {

    private final Namespace namespace;
    private final AppSecurityContext context;
    private final PlaceDbHelper dbHelper;
    private final NamespaceObjectFactory factory;


    public NamespacePlaceManager(Namespace namespace, AppSecurityContext context, PlaceDbHelper dbHelper, NamespaceObjectFactory factory) {
        this.namespace = namespace;
        this.context = context;
        this.factory = factory;
        this.dbHelper = dbHelper;
    }

    @Override
    public Optional<Place> findPlaceById(Integer id) {
        return dbHelper.findPlaceById(id, context)
                .map(r -> new PlaceImpl(namespace, r));
    }

    @Override
    public Place addPlace(AddPlace add, Object props) {
        ParamValidator.builder()
                .withNonNullOrEmpty(add.getCountry(), BaseParamName.COUNTRY)
                .withNonNullOrEmpty(add.getCity(), BaseParamName.CITY)
                .withNonNullOrEmpty(add.getAddress(), BaseParamName.ADDRESS)
                .throwIfErrors();
        namespace.getPermissionManager().auditAndCheckPermission(new ActionPack(NamespaceAction.SAVE_PLACE,
                Map.of("place", add, "props", props)));
        PlaceRecord placeRecord = dbHelper.addPlace(add.getCountry().strip(), add.getCounty(), add.getCity().strip(), add.getBuilding(),
                add.getAddress().strip(), add.getPostCode(), add.getLat(), add.getLng(),
                add.getNotes(), props, context);
        return factory.build(namespace, placeRecord);
    }
}
