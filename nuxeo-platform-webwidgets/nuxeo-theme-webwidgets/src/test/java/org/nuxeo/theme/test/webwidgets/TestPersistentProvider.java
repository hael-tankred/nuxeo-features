/*
 * (C) Copyright 2006-2007 Nuxeo SAS <http://nuxeo.com> and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jean-Marc Orliaguet, Chalmers
 *
 * $Id$
 */

package org.nuxeo.theme.test.webwidgets;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

import org.nuxeo.runtime.test.NXRuntimeTestCase;
import org.nuxeo.theme.webwidgets.ProviderException;
import org.nuxeo.theme.webwidgets.Widget;
import org.nuxeo.theme.webwidgets.WidgetData;
import org.nuxeo.theme.webwidgets.WidgetState;
import org.nuxeo.theme.webwidgets.providers.PersistentProvider;
import org.nuxeo.theme.webwidgets.providers.WidgetEntity;

public class TestPersistentProvider extends NXRuntimeTestCase {

    protected PersistentProvider provider;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        deployContrib("org.nuxeo.ecm.core.persistence",
                "OSGI-INF/persistence-service.xml");
        deployContrib("org.nuxeo.theme.test.webwidgets",
                "webwidgets-contrib.xml");

        provider = new PersistentProvider();
    }

    @After
    public void tearDown() throws Exception {
        provider.destroy();
        provider.deactivate();
        provider = null;
        super.tearDown();
    }

    @Test
    public void testCreateWidget() throws ProviderException {
        Widget widget1 = provider.createWidget("test widget");
        Widget widget2 = provider.createWidget("test widget 2");

        assertEquals("test widget", widget1.getName());
        assertEquals("test widget 2", widget2.getName());
    }

    @Test
    public void testGetWidgetByUid() throws ProviderException {
        String name1 = "test widget";
        String name2 = "test widget 2";

        Widget widget1 = provider.createWidget(name1);
        Widget widget2 = provider.createWidget(name2);
        String uid1 = widget1.getUid();
        String uid2 = widget2.getUid();

        assertEquals(widget1, provider.getWidgetByUid(uid1));
        assertEquals(widget2, provider.getWidgetByUid(uid2));
    }

    @Test
    public void testAddAndGetWidgets() throws ProviderException {
        Widget widget1 = provider.createWidget("test widget");
        Widget widget2 = provider.createWidget("test widget 2");
        provider.addWidget(widget1, "region A", 0);
        provider.addWidget(widget2, "region B", 0);
        assertEquals("region A", provider.getRegionOfWidget(widget1));
        assertEquals("region B", provider.getRegionOfWidget(widget2));
        assertTrue(provider.getWidgets("region A").contains(widget1));
        assertTrue(provider.getWidgets("region B").contains(widget2));
        assertFalse(provider.getWidgets("region A").contains(widget2));
        assertFalse(provider.getWidgets("region B").contains(widget1));

        assertEquals(0, provider.getWidgets("region A").indexOf(widget1));
        assertEquals(0, provider.getWidgets("region B").indexOf(widget2));

        Widget widget3 = provider.createWidget("test widget 2");
        provider.addWidget(widget3, "region A", 1);
        assertEquals(1, provider.getWidgets("region A").indexOf(widget3));
    }

    @Test
    public void testReorderWidget() throws ProviderException {
        Widget widget1 = provider.createWidget("test widget");
        Widget widget2 = provider.createWidget("test widget");
        Widget widget3 = provider.createWidget("test widget");
        provider.addWidget(widget1, "region A", 0);
        provider.addWidget(widget2, "region A", 1);
        provider.addWidget(widget3, "region A", 2);

        assertEquals(0, provider.getWidgets("region A").indexOf(widget1));
        assertEquals(1, provider.getWidgets("region A").indexOf(widget2));
        assertEquals(2, provider.getWidgets("region A").indexOf(widget3));

        provider.reorderWidget(widget2, 0);
        assertEquals(0, provider.getWidgets("region A").indexOf(widget2));
        assertEquals(1, provider.getWidgets("region A").indexOf(widget1));
        assertEquals(2, provider.getWidgets("region A").indexOf(widget3));

        provider.reorderWidget(widget3, 1);
        assertEquals(0, provider.getWidgets("region A").indexOf(widget2));
        assertEquals(1, provider.getWidgets("region A").indexOf(widget3));
        assertEquals(2, provider.getWidgets("region A").indexOf(widget1));

        provider.reorderWidget(widget2, 2);
        assertEquals(0, provider.getWidgets("region A").indexOf(widget3));
        assertEquals(1, provider.getWidgets("region A").indexOf(widget1));
        assertEquals(2, provider.getWidgets("region A").indexOf(widget2));

        provider.reorderWidget(widget3, 2);
        assertEquals(0, provider.getWidgets("region A").indexOf(widget1));
        assertEquals(1, provider.getWidgets("region A").indexOf(widget2));
        assertEquals(2, provider.getWidgets("region A").indexOf(widget3));
    }

    @Test
    public void testRemoveWidget() throws ProviderException {
        Widget widget1 = provider.createWidget("remove test widget");
        Widget widget2 = provider.createWidget("remove test widget");
        Widget widget3 = provider.createWidget("remove test widget");
        provider.addWidget(widget1, "remove region A", 0);
        provider.addWidget(widget2, "remove region A", 1);
        provider.addWidget(widget3, "remove region A", 2);

        assertEquals(0, provider.getWidgets("remove region A").indexOf(widget1));
        assertEquals(1, provider.getWidgets("remove region A").indexOf(widget2));
        assertEquals(2, provider.getWidgets("remove region A").indexOf(widget3));

        provider.removeWidget(widget2);
        assertEquals(0, provider.getWidgets("remove region A").indexOf(widget1));
        assertEquals(1, provider.getWidgets("remove region A").indexOf(widget3));

        provider.removeWidget(widget1);
        assertEquals(0, provider.getWidgets("remove region A").indexOf(widget3));

        provider.removeWidget(widget3);
        assertTrue(provider.getWidgets("remove region A").isEmpty());
    }

    @Test
    public void testMoveWidget() throws ProviderException {
        Widget widget1 = provider.createWidget("test widget");
        Widget widget2 = provider.createWidget("test widget");
        Widget widget3 = provider.createWidget("test widget");
        provider.addWidget(widget1, "region A", 0);
        provider.addWidget(widget2, "region A", 1);
        provider.addWidget(widget3, "region A", 2);

        assertEquals(0, provider.getWidgets("region A").indexOf(widget1));
        assertEquals(1, provider.getWidgets("region A").indexOf(widget2));
        assertEquals(2, provider.getWidgets("region A").indexOf(widget3));

        provider.moveWidget(widget1, "region A", 1);
        assertEquals(0, provider.getWidgets("region A").indexOf(widget2));
        assertEquals(1, provider.getWidgets("region A").indexOf(widget1));
        assertEquals(2, provider.getWidgets("region A").indexOf(widget3));

        provider.moveWidget(widget3, "region A", 0);
        assertEquals(0, provider.getWidgets("region A").indexOf(widget3));
        assertEquals(1, provider.getWidgets("region A").indexOf(widget2));
        assertEquals(2, provider.getWidgets("region A").indexOf(widget1));

        provider.moveWidget(widget3, "region A", 0);
        assertEquals(0, provider.getWidgets("region A").indexOf(widget3));
        assertEquals(1, provider.getWidgets("region A").indexOf(widget2));
        assertEquals(2, provider.getWidgets("region A").indexOf(widget1));

        provider.moveWidget(widget2, "region A", 2);
        assertEquals(0, provider.getWidgets("region A").indexOf(widget3));
        assertEquals(1, provider.getWidgets("region A").indexOf(widget1));
        assertEquals(2, provider.getWidgets("region A").indexOf(widget2));

        provider.moveWidget(widget1, "region B", 0);
        assertEquals(0, provider.getWidgets("region A").indexOf(widget3));
        assertEquals(1, provider.getWidgets("region A").indexOf(widget2));
        assertEquals(0, provider.getWidgets("region B").indexOf(widget1));

        provider.moveWidget(widget2, "region B", 0);
        assertEquals(0, provider.getWidgets("region A").indexOf(widget3));
        assertEquals(0, provider.getWidgets("region B").indexOf(widget2));
        assertEquals(1, provider.getWidgets("region B").indexOf(widget1));
    }

    @Test
    public void testState() throws ProviderException {
        Widget widget = provider.createWidget("test widget");
        provider.setWidgetState(widget, WidgetState.DEFAULT);
        assertEquals(WidgetState.DEFAULT, provider.getWidgetState(widget));
        provider.setWidgetState(widget, WidgetState.SHADED);
        assertEquals(WidgetState.SHADED, provider.getWidgetState(widget));
    }

    @Test
    public void testPreferences() throws ProviderException {
        Widget widget = provider.createWidget("test widget");
        Map<String, String> preferences = new HashMap<String, String>();
        preferences.put("key1", "value 1");
        preferences.put("key2", "value 2");
        preferences.put("key3", "value 3");
        provider.setWidgetPreferences(widget, preferences);
        Map<String, String> retrievedPreferences = provider.getWidgetPreferences(widget);
        assertEquals("value 1", retrievedPreferences.get("key1"));
        assertEquals("value 2", retrievedPreferences.get("key2"));
        assertEquals("value 3", retrievedPreferences.get("key3"));
    }

    @Test
    public void testWidgetData() throws ProviderException {
        Widget widget = provider.createWidget("test widget");

        String content = "<FILE CONTENT>";

        WidgetData data = new WidgetData("image/png", "image.png",
                content.getBytes());
        String dataName = "src";
        provider.setWidgetData(widget, dataName, data);

        WidgetData retrievedData = provider.getWidgetData(widget, dataName);
        assertEquals("image/png", retrievedData.getContentType());
        assertEquals("image.png", retrievedData.getFilename());
        assertEquals(content, new String(retrievedData.getContent()));

        assertNull(provider.getWidgetData(widget, "unknown"));

        provider.deleteWidgetData(widget);
        assertNull(provider.getWidgetData(widget, dataName));
    }

    @Test
    public void testReorderWidgets() throws ProviderException {
        WidgetEntity widget1 = (WidgetEntity) provider.createWidget("test widget 1");
        WidgetEntity widget2 = (WidgetEntity) provider.createWidget("test widget 2");
        WidgetEntity widget3 = (WidgetEntity) provider.createWidget("test widget 3");
        widget1.setOrder(10);
        widget2.setOrder(20);
        widget3.setOrder(30);
        provider.addWidget(widget1, "region A", 0);
        provider.addWidget(widget2, "region A", 1);
        provider.addWidget(widget3, "region A", 2);

        List<Widget> widgets = provider.getWidgets("region A");
        assertEquals(0, widget1.getOrder());
        assertEquals(1, widget2.getOrder());
        assertEquals(2, widget3.getOrder());
        Collections.swap(widgets, 0, 1);
        provider.reorderWidgets(widgets);
    }
}
