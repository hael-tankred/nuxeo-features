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

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.nuxeo.ecm.core.api.ClientException;
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
@WebObject(type = "user")
@Produces({ MediaType.APPLICATION_JSON,
        MediaType.APPLICATION_JSON + "+nxentity" })
public class UserObject extends DefaultObject {

    protected NuxeoPrincipal currentPrincipal;

    @Override
    protected void initialize(Object... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException(
                    "UserObject takes at least one parameter");
        }

        currentPrincipal = (NuxeoPrincipal) args[0];
    }

    @GET
    public NuxeoPrincipal doGetUser() {
        return currentPrincipal;
    }

    @PUT
    public NuxeoPrincipal doUpdateUser(NuxeoPrincipal principal) {
        UserManager um = Framework.getLocalService(UserManager.class);
        checkCurrentUserCanAdministerUser(um);
        try {
            um.updateUser(principal.getModel());
            return um.getPrincipal(principal.getName());
        } catch (ClientException e) {
            throw WebException.wrap(e);
        }
    }

    private void checkCurrentUserCanAdministerUser(UserManager um) {
        NuxeoPrincipal principal = (NuxeoPrincipal) getContext().getCoreSession().getPrincipal();
        if (!principal.isAdministrator()) {
            if ((!principal.isMemberOf("powerusers"))
                    || UserRootObject.isNotAPowerUserEditableUser(principal, um)) {

                throw new WebSecurityException(
                        "User is not allowed to edit users");
            }
        }
    }

    @Path("group/{groupName}")
    public Object doGetUserToGroup(@PathParam("groupName")
    String groupName) {
        try {
            UserManager um = Framework.getLocalService(UserManager.class);
            NuxeoGroup group = um.getGroup(groupName);
            if (group == null) {
                throw new WebResourceNotFoundException("Group not found");
            }

            return newObject("userToGroup", currentPrincipal, group);
        } catch (ClientException e) {
            throw WebException.wrap(e);
        }
    }

    @DELETE
    public Response doDeleteUser() {
        UserManager um = Framework.getLocalService(UserManager.class);
        checkCurrentUserCanAdministerUser(um);
        try {
            um.deleteUser(currentPrincipal.getModel());
            return Response.status(Status.NO_CONTENT).build();
        } catch (ClientException e) {
            throw WebException.wrap(e);
        }
    }

}