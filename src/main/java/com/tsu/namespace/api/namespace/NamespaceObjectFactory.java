package com.tsu.namespace.api.namespace;

import com.tsu.auth.security.AppSecurityContext;
import com.tsu.auth.security.NamespaceContext;
import com.tsu.entry.service.BucketService;
import com.tsu.namespace.api.*;
import com.tsu.namespace.api.entity.EntityImpl;
import com.tsu.namespace.api.entity.EntityTypeImpl;
import com.tsu.namespace.api.subscription.NamespaceSubscription;
import com.tsu.namespace.api.user.NamespaceRoleImpl;
import com.tsu.namespace.api.user.NamespaceUserImpl;
import com.tsu.namespace.api.user.UserBaseImpl;
import com.tsu.namespace.helper.*;
import com.tsu.namespace.record.*;
import com.tsu.namespace.service.IDGeneratorService;
import com.tsu.place.api.Place;
import org.springframework.beans.factory.annotation.Autowired;

public class NamespaceObjectFactory {


    @Autowired
    private BucketService bucketService;


    @Autowired
    private BasePrincipalHelper basePrincipalHelper;

    @Autowired
    private NamespaceDbHelper namespaceDbHelper;
    @Autowired
    private SubscriptionDbHelper subscriptionDbHelper;

    @Autowired
    private EntityDbHelper entityDbHelper;

    @Autowired
    private UserDbHelper userDbHelper;

    @Autowired
    private NumberDbHelper numberDbHelper;
    @Autowired
    private PlaceDbHelper placeDbHelper;

    @Autowired
    private ResourceDbHelper resourceDbHelper;

    @Autowired
    private BookingDbHelper bookingDbHelper;

    @Autowired
    private AppDbHelper appDbHelper;


    @Autowired
    private IDGeneratorService idGeneratorService;

    public Namespace build(NamespaceRecord record, NamespaceUser user, AppSecurityContext context) {
        return new NamespaceImpl(context, user, record, namespaceDbHelper,
                entityDbHelper, numberDbHelper, bucketService,
                placeDbHelper, resourceDbHelper, bookingDbHelper,
                subscriptionDbHelper, appDbHelper, idGeneratorService, this);
    }


    public Subscription build(SubscriptionRecord record, AppSecurityContext context) {
        return new NamespaceSubscription(record, context, subscriptionDbHelper, namespaceDbHelper);
    }

    public NamespaceUser build(NamespaceRecord namespace, NamespaceUserRecord record, AppSecurityContext context) {
        return new NamespaceUserImpl(namespace, record, context, userDbHelper, this);
    }

    public NamespaceUser build(Namespace namespace, NamespaceUserRecord record, AppSecurityContext context) {
        return new NamespaceUserImpl(namespace, record, context, userDbHelper, this);
    }


    public NamespaceRole build(NamespaceRoleRecord record, NamespaceContext context) {
        return new NamespaceRoleImpl(record, context);
    }


    public UserBase build(UserRecord tb, AppSecurityContext context) {
        return new UserBaseImpl(tb, namespaceDbHelper, context);
    }


    public EntityType build(Namespace namespace, EntityTypeRecord record) {
        return new EntityTypeImpl(record, namespace);
    }

    public NumberSeq build(NumberRecord number, NamespaceContext context) {
        return new NumberSeqImpl(number, numberDbHelper, context);
    }

    public Place build(Namespace namespace, PlaceRecord record) {
        return new PlaceImpl(namespace, record);
    }


    public Entity build(Namespace namespace, EntityRecord e, NamespaceContext context) {
        return new EntityImpl(namespace, e, entityDbHelper, context, this);
    }


}
