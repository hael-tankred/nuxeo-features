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

import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.nuxeo.ecm.core.api.ClientException;
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
@WebObject(type = "users")
@Produces({ MediaType.APPLICATION_JSON,
        MediaType.APPLICATION_JSON + "+nxentity" })
public class UserRootObject extends DefaultObject {

    @Path("{username}")
    public Object getUser(@PathParam("username")
    String username) {
        UserManager um = Framework.getLocalService(UserManager.class);
        try {
            NuxeoPrincipal user = um.getPrincipal(username);
            if (user == null) {
                throw new WebResourceNotFoundException("User does not exist");
            }
            return newObject("user", user);
        } catch (ClientException e) {
            throw WebException.wrap(e);
        }
    }

    @POST
    public Response createUser(NuxeoPrincipal principal) {
        try {
            UserManager um = Framework.getLocalService(UserManager.class);
            checkCurrentUserCanCreatePrincipal(principal, um);
            checkPrincipalHasAName(principal);
            checkPrincipalDoesNotAlreadyExists(principal, um);

            um.createUser(principal.getModel());
            return Response.status(Status.CREATED).entity(
                    um.getPrincipal(principal.getName())).build();

        } catch (ClientException e) {
            throw WebException.wrap(e);
        }
    }

    private void checkCurrentUserCanCreatePrincipal(NuxeoPrincipal principal,
            UserManager um) {
        NuxeoPrincipal currentUser = (NuxeoPrincipal) getContext().getCoreSession().getPrincipal();
        if (!currentUser.isAdministrator()) {
            if (!currentUser.isMemberOf("powerusers")
                    || isNotAPowerUserEditableUser(principal, um)) {
                throw new WebSecurityException("Cannot create a user");
            }
        }
    }

    /**
     * Checks if the principal is allowed to be edited by a power user.
     * This includes admin group
     * @param principal the user to check
     * @param um2
     * @return
     *
     */
    static boolean isNotAPowerUserEditableUser(NuxeoPrincipal principal,
            UserManager um) {
        // power user can only create a non admin group
        List<String> adminGroups = um.getAdministratorsGroups();
        for (String adminGroup : adminGroups) {
            if (principal.getAllGroups().contains(adminGroup)) {
                return false;
            }
        }

        return true;
    }

    private void checkPrincipalDoesNotAlreadyExists(NuxeoPrincipal principal,
            UserManager um) throws ClientException {
        NuxeoPrincipal user = um.getPrincipal(principal.getName());
        if (user != null) {
            throw new WebException("User already exists",
                    Response.Status.PRECONDITION_FAILED.getStatusCode());
        }
    }

    private void checkPrincipalHasAName(NuxeoPrincipal principal) {
        if (principal.getName() == null) {
            throw new WebException("User MUST have a name",
                    Response.Status.PRECONDITION_FAILED.getStatusCode());
        }
    }

}