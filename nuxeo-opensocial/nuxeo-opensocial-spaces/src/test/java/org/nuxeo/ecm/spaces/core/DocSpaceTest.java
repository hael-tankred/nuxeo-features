/*
 * (C) Copyright 2011 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 * Nuxeo - initial API and implementation
 */

package org.nuxeo.ecm.spaces.core;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.nuxeo.ecm.core.storage.sql.SQLRepositoryTestCase;
import org.nuxeo.ecm.spaces.api.Space;
import org.nuxeo.ecm.spaces.api.SpaceManager;
import org.nuxeo.opensocial.container.server.layout.YUILayoutAdapter;
import org.nuxeo.opensocial.container.shared.layout.api.LayoutHelper;
import org.nuxeo.opensocial.container.shared.layout.api.YUIComponent;
import org.nuxeo.opensocial.container.shared.layout.api.YUILayout;
import org.nuxeo.runtime.api.Framework;

@RunWith(JUnit4.class)
public class DocSpaceTest extends SQLRepositoryTestCase {

    private SpaceManager spaceManager;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        deployBundle("org.nuxeo.ecm.opensocial.spaces");
        deployBundle("org.nuxeo.ecm.platform.picture.core");
        deployContrib("org.nuxeo.ecm.opensocial.spaces.test",
                "OSGI-INF/test1-spaces-contrib.xml");
        spaceManager = Framework.getService(SpaceManager.class);
        session = openSessionAs("Administrator");
    }

    @Test
    public void iCanGetSpaceManager() throws Exception {
        assertNotNull(spaceManager);
    }

    @Test
    public void iGetTheHomeSpaceProvider() throws Exception {
        Space space = spaceManager.getSpace("homeSpace", session);
        assertNotNull(space);
        assertEquals("Home", space.getTitle());
    }

    @Test
    public void layoutIsInitialized() throws Exception {
        Space space = spaceManager.getSpace("homeSpace", session);
        YUILayoutAdapter layoutAdapter = space.getLayout();
        assertNotNull(layoutAdapter);
        space.initLayout(LayoutHelper.buildLayout(LayoutHelper.Preset.X_2_33_66));

        YUILayout layout = layoutAdapter.getLayout();
        assertEquals(1, layout.getContent().getComponents().size());
        YUIComponent yuiComponent = layout.getContent().getComponents().get(0);
        assertNotNull(yuiComponent);
        assertEquals(2, yuiComponent.getComponents().size());
        YUIComponent unit = yuiComponent.getComponents().get(0);
        assertNotNull(unit.getId());
    }

}