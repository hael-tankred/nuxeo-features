/*
 * Copyright (c) 2006-2013 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     vpasquier
 */
package org.nuxeo.ecm.automation.core.test;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.impl.OperationServiceImpl;
import org.nuxeo.ecm.automation.core.operations.FetchContextDocument;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

import com.google.inject.Inject;

/**
 * @since 5.7.2 Test for parametized chain
 */
@RunWith(FeaturesRunner.class)
@Features(CoreFeature.class)
@Deploy("org.nuxeo.ecm.automation.core")
@LocalDeploy("org.nuxeo.ecm.automation.core:test-parametrization-chain.xml")
public class TestOperationChainParametrization {

    protected DocumentModel src;

    @Inject
    AutomationService service;

    @Inject
    CoreSession session;

    @Before
    public void initRepo() throws Exception {
        src = session.createDocumentModel("/", "src", "Folder");
        src.setPropertyValue("dc:title", "Source");
        src = session.createDocument(src);
        session.save();
        src = session.getDocument(src.getRef());
    }

    @After
    public void clearRepo() throws Exception {
        Framework.getLocalService(EventService.class).waitForAsyncCompletion();
        session.removeChildren(session.getRootDocument().getRef());
    }

    /**
     * Check if using chain without parameters is working
     */
    @Test
    public void testSimpleChain() throws Exception {
        OperationContext ctx = new OperationContext(session);
        ctx.setInput(src);
        OperationChain chain = new OperationChain("notRegisteredChain");
        chain.add(FetchContextDocument.ID);
        chain.add("o1").set("message", "Hello 1!");
        chain.add("o2").set("message", "Hello 2!");
        chain.add("o3").set("message", "Hello 3!");
        DocumentModel doc = (DocumentModel) service.run(ctx, chain);
        Assert.assertNotNull(doc);
    }

    /**
     * Check if using chain with parameters is working
     */
    @Test
    public void testParametizedChain() throws Exception {
        OperationContext ctx = new OperationContext(session);
        ctx.setInput(src);
        OperationChain chain = new OperationChain("notRegisteredChain");
        chain.add(FetchContextDocument.ID);
        chain.add("o1").set("message", "Hello 1!");
        chain.add("o2").set("message", "Hello 2!");
        chain.add("o3").set("message",
                "Message from chain: @{ChainParameters['messageChain']}");
        // Setting parameters of the chain
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("messageChain", "Hello i'm a chain!");
        chain.addChainParameters(params);
        DocumentModel doc = (DocumentModel) ((OperationServiceImpl) service).run(
                ctx, chain);
        Assert.assertNotNull(doc);
    }

    /**
     * Check if using registered chain with its operations is working
     */
    @Test
    public void testContributedParametizedChain() throws Exception {
        OperationContext ctx = new OperationContext(session);
        ctx.setInput(src);
        // No need to put a message parameter, fallback is going to be done
        DocumentModel doc = (DocumentModel) service.run(ctx, "contributedchain");
        Assert.assertNotNull(doc);
    }
}