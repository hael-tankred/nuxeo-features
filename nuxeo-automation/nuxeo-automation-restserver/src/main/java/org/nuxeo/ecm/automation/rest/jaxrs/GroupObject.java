/*
 * (C) Copyright 2013 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     dmetzler
 */
package org.nuxeo.ecm.automation.rest.jaxrs;

import java.io.Serializable;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoGroup;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.webengine.WebException;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.exceptions.WebResourceNotFoundException;
import org.nuxeo.ecm.webengine.model.exceptions.WebSecurityException;
import org.nuxeo.ecm.webengine.model.impl.DefaultObject;
import org.nuxeo.runtime.api.Framework;

/**
 *
 *
 * @since 5.7.3
 */
@WebObject(type = "group")
public class GroupObject extends DefaultObject {

    private NuxeoGroup group;

    @Override
    protected void initialize(Object... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException(
                    "Group object takes at least one argument");
        }

        group = (NuxeoGroup) args[0];
    }

    @GET
    public NuxeoGroup doGetGroup() {
        return group;
    }

    @PUT
    public NuxeoGroup doUpdateGroup(NuxeoGroup updateGroup) {
        checkCurrentUserCanAdministerGroup();
        UserManager um = Framework.getLocalService(UserManager.class);
        try {

            DocumentModel groupModel = um.getGroupModel(group.getName());
            groupModel.setPropertyValue(um.getGroupLabelField(),
                    updateGroup.getLabel());
            groupModel.setPropertyValue(um.getGroupMembersField(),
                    (Serializable) updateGroup.getMemberUsers());
            groupModel.setPropertyValue(um.getGroupSubGroupsField(),
                    (Serializable) updateGroup.getMemberGroups());

            um.updateGroup(groupModel);
            return um.getGroup(group.getName());
        } catch (ClientException e) {
            throw WebException.wrap(e);
        }
    }

    @DELETE
    public Response doDeleteGroup() {
        try {
            checkCurrentUserCanAdministerGroup();
            UserManager um = Framework.getLocalService(UserManager.class);
            um.deleteGroup(um.getGroupModel(group.getName()));
            return Response.status(Status.NO_CONTENT).build();
        } catch (ClientException e) {
            throw WebException.wrap(e);
        }
    }

    @Path("user/{username}")
    public Object doGetUserToGroup(@PathParam("username")
    String username) {
        try {
            UserManager um = Framework.getLocalService(UserManager.class);
            NuxeoPrincipal principal = um.getPrincipal(username);
            if (principal == null) {
                throw new WebResourceNotFoundException("User not found");
            }
            return newObject("userToGroup", principal, group);

        } catch (ClientException e) {
            throw WebException.wrap(e);
        }

    }

    private void checkCurrentUserCanAdministerGroup() {
        UserManager um = Framework.getLocalService(UserManager.class);
        NuxeoPrincipal principal = (NuxeoPrincipal) getContext().getCoreSession().getPrincipal();
        if (!principal.isAdministrator()) {
            if ((!principal.isMemberOf("powerusers"))
                    || GroupRootObject.isNotAPowerUserEditableObject(group, um)) {

                throw new WebSecurityException(
                        "User is not allowed to edit this group");
            }
        }
    }
}