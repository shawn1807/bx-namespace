package com.tsu.namespace.api.namespace;

import com.tsu.namespace.api.*;
import com.tsu.namespace.api.entity.EntityImpl;
import com.tsu.namespace.api.entity.EntityTypeImpl;
import com.tsu.base.api.subscription.NamespaceSubscription;
import com.tsu.namespace.api.user.NamespaceRoleImpl;
import com.tsu.namespace.api.user.NamespaceUserImpl;
import com.tsu.namespace.api.user.UserBaseImpl;
import com.tsu.namespace.api.workspace.*;
import com.tsu.namespace.helper.*;
import com.tsu.namespace.record.*;
import com.tsu.base.service.IDGeneratorService;
import com.tsu.entry.api.EntryLink;
import com.tsu.entry.api.EntryObj;
import com.tsu.entry.service.BucketService;
import com.tsu.security.AppSecurityContext;
import com.tsu.security.NamespaceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class DomainObjectBuilder {


    @Autowired
    private BucketService bucketService;


    @Autowired
    private DocumentDbHelper documentDbHelper;


    @Autowired
    private BasePrincipalHelper basePrincipalHelper;

    @Autowired
    private WorkspaceDbHelper workspaceDatabaseHelper;

    @Autowired
    private NamespaceDbHelper namespaceDbHelper;
    @Autowired
    private SubscriptionDbHelper subscriptionDbHelper;

    @Autowired
    private EntityDbHelper entityDbHelper;

    @Autowired
    private WorkDbHelper workDbHelper;

    @Autowired
    private UserDbHelper userDbHelper;

    @Autowired
    private NumberDbHelper numberDbHelper;
    @Autowired
    private PlaceDbHelper placeDbHelper;

    @Autowired
    private AppDbHelper appDbHelper;

    @Autowired
    private BaseEventManager eventManager;

    @Autowired
    private IDGeneratorService idGeneratorService;

    public Namespace build(NamespaceRecord record, NamespaceUser user, AppSecurityContext context) {
        return new NamespaceImpl(context, user, record, namespaceDbHelper, workspaceDatabaseHelper,
                entityDbHelper, workDbHelper, numberDbHelper, bucketService,
                placeDbHelper, documentDbHelper, subscriptionDbHelper, appDbHelper, idGeneratorService, this);
    }

    public WorkspaceUser build(Workspace workspace, WorkspaceUserRecord record) {
        return new WorkspaceUserImpl(workspace, record, namespaceDbHelper, workspace.getPermissionManager(), this);
    }

    public MeetingParticipant build(EntryLink link) {
        return new MeetingParticipantImpl(link);
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

    public WorkspaceGroup build(Namespace namespace, WorkspaceGroupRecord groupRecord, NamespaceContext context) {
        return new WorkspaceGroupImpl(namespace, groupRecord, workspaceDatabaseHelper, context, this);
    }


    public UserBase build(UserRecord tb, AppSecurityContext context) {
        return new UserBaseImpl(tb, bucketService, namespaceDbHelper, context);
    }

    public WorkspaceType build(NamespaceContext context, WorkspaceTypeRecord record) {
        return new WorkspaceTypeImpl(context, record);
    }

    public Workspace build(WorkspaceRecord workspace, NamespaceContext context) {
        return new WorkspaceImpl(workspace, eventManager, workspaceDatabaseHelper, documentDbHelper,
                workDbHelper, userDbHelper, appDbHelper, context, this);
    }

    public EntityType build(Namespace namespace, EntityTypeRecord record) {
        return new EntityTypeImpl(record, namespace);
    }

    public DocumentType build(Namespace namespace, DocumentTypeRecord record) {
        return new DocumentTypeImpl(namespace, record);
    }

    public Document build(Workspace workspace, DocumentRecord document, NamespaceContext context) {
        return new DocumentImpl(workspace, document, documentDbHelper, context);
    }

    public NumberSeq build(NumberRecord number, NamespaceContext context) {
        return new NumberSeqImpl(number, numberDbHelper, context);
    }

    public Place build(Namespace namespace, PlaceRecord record) {
        return new PlaceImpl(namespace, record);
    }

    public WorkItem build(Workspace workspace, WorkRecord record, NamespaceContext context) {
        return switch (record.getCategory()) {
            case T -> new WorkspaceTask(record, workspace, workDbHelper, documentDbHelper, context, this);
            case J -> new WorkspaceJob(record, workspace, workDbHelper, documentDbHelper, context, this);
            case M -> new WorkspaceMeeting(record, workspace, workDbHelper, documentDbHelper, context, this);
            case G -> new WorkspaceGoal(record, workspace, workDbHelper, documentDbHelper, context, this);
            case E -> new WorkspaceEvent(record, workspace, workDbHelper, documentDbHelper, context, this);
        };
    }


    public Form build(Workspace workspaces, WorkItem workItem, EntryObj entryObj) {
        return new FormImpl(workspaces, workItem, entryObj);
    }

    public WorkHour build(WorkItem workItem, WorkHourRecord hour, PermissionManager permissionManager, NamespaceContext context) {
        return new WorkHourImpl(workItem, hour, permissionManager, context);
    }


    public Assignment build(AssignmentRecord record, PermissionManager permissionManager, NamespaceContext context) {
        return new EntryAssignmentImpl(context, record, permissionManager, namespaceDbHelper, this);
    }

    public Entity build(Namespace namespace, EntityRecord e, NamespaceContext context) {
        return new EntityImpl(namespace, e, entityDbHelper, workspaceDatabaseHelper, context, this);
    }

    public WorkDependency build(WorkItem workItem, EntryLink link, NamespaceContext context) {
        return new WorkDependencyImpl(workItem, link, workDbHelper, context, this);
    }

    public WorkType build(WorkTypeRecord record, NamespaceContext context) {
        return switch (record.getCategory()) {
            case T -> new TaskTypeImpl(record, context);
            case J -> new JobTypeImpl(record, context);
            case M -> new MeetingTypeImpl(record, context);
            case G -> new GoalTypeImpl(record, context);
            case E -> new EventTypeImpl(record, context);
        };
    }


}
