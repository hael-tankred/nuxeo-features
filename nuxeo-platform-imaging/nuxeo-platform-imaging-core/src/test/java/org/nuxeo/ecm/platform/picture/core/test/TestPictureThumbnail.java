/*
 * (C) Copyright 2007-2013 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Vladimir Pasquier <vpasquier@nuxeo.com>
 */

package org.nuxeo.ecm.platform.picture.core.test;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.impl.DocumentModelImpl;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.picture.api.PictureView;
import org.nuxeo.ecm.platform.picture.api.adapters.MultiviewPicture;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

/**
 * @since 5.7
 */
@RunWith(FeaturesRunner.class)
@Features(CoreFeature.class)
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.core.convert", "org.nuxeo.ecm.core.convert.api",
        "org.nuxeo.ecm.platform.commandline.executor",
        "org.nuxeo.ecm.platform.picture.api",
        "org.nuxeo.ecm.platform.picture.core",
        "org.nuxeo.ecm.platform.picture.convert" })
public class TestPictureThumbnail {

    @Inject
    CoreSession session;

    private static File getFileFromPath(String path) {
        return FileUtils.getResourceFileFromContext(path);
    }

    private List<Map<String, Serializable>> createViews() {
        List<Map<String, Serializable>> views = new ArrayList<Map<String, Serializable>>();
        Map<String, Serializable> map = new HashMap<String, Serializable>();
        map.put("title", "Original");
        map.put("content", new FileBlob(getFileFromPath("images/statue.jpg"),
                "image/jpeg", null, "statue.jpg", null));
        map.put("filename", "statue.jpg");
        views.add(map);
        return views;
    }

    @Test
    public void testPictureThumbnail() throws Exception {
        // Init test
        DocumentModel root = session.getRootDocument();
        DocumentModel picture = new DocumentModelImpl(root.getPathAsString(),
                "pic", "Picture");
        picture.setPropertyValue("picture:views", (Serializable) createViews());
        picture = session.createDocument(picture);
        session.save();
        // Create 4 views
        BlobHolder bh = picture.getAdapter(BlobHolder.class);
        Blob blob = new FileBlob(getFileFromPath("images/statue.jpg"),
                "image/jpeg", null, "statue.jpg", null);
        bh.setBlob(blob);
        session.saveDocument(picture);
        session.save();
        // Get picture thumbnail view
        MultiviewPicture mViewPicture = picture.getAdapter(MultiviewPicture.class);
        PictureView thumbnailView = mViewPicture.getView("Thumbnail");
        Blob pictureThumbnail = thumbnailView.getBlob();
        // Thumbnail service should return the default picture thumbnail
        Assert.assertEquals(pictureThumbnail.getDigest(),
                bh.getThumbnail().getDigest());
    }
}