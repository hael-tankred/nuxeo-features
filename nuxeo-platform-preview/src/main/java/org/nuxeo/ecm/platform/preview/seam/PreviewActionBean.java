/*
 * (C) Copyright 2006-2007 Nuxeo SAS (http://nuxeo.com/) and contributors.
 * (C) Copyright 2006-2007 YSEngineers Soc. Coop. And. (http://www.yerbabuena.es/) and contributors.

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
 *     Yerbabuena - initial implementation
 *     Nuxeo

 */

package org.nuxeo.ecm.platform.preview.seam;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.remoting.WebRemote;
import org.jboss.seam.annotations.web.RequestParameter;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentLocation;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.impl.DocumentLocationImpl;
import org.nuxeo.ecm.platform.preview.api.PreviewException;
import org.nuxeo.ecm.platform.preview.helper.PreviewHelper;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.ui.web.api.WebActions;
import org.nuxeo.ecm.platform.ui.web.rest.RestHelper;
import org.nuxeo.ecm.platform.ui.web.rest.api.URLPolicyService;
import org.nuxeo.ecm.platform.url.DocumentViewImpl;
import org.nuxeo.ecm.platform.url.api.DocumentView;
import org.nuxeo.ecm.platform.web.common.vh.VirtualHostHelper;
import org.nuxeo.ecm.webapp.helpers.EventNames;
import org.nuxeo.runtime.api.Framework;

/**
 * Seam Action bean to handle the preview tabs and associated actions.
 *
 * @author <a href="mailto:enriqueperez@yerbabuena.es">Enrique Perez</a>
 * @author tiry
 */
@Name("previewActions")
@Scope(ScopeType.CONVERSATION)
public class PreviewActionBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(PreviewActionBean.class);

    public static final String PREVIEW_POPUP_VIEW = "preview_popup";

    @In(create = true, required = false)
    transient NavigationContext navigationContext;

    @In(create = true, required = false)
    protected transient CoreSession documentManager;

    @In(create = true, required = false)
    protected WebActions webActions;

    @RequestParameter
    private String fieldXPath;

    @RequestParameter
    private String previewTabId;

    private String fieldXPathValue;

    public boolean getHasPreview() {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        return documentHasPreview(currentDocument);
    }

    public boolean documentHasPreview(DocumentModel document) {
        if (document == null) {
            return false;
        }
        if(PreviewHelper.typeSupportsPreview(document)) {
            try {
                return PreviewHelper.docHasBlobToPreview(document);
            } catch (PreviewException e) {
                return false;
            }
        } else {
            return false;
        }


    }

    public String getPreviewURL() {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        if (currentDocument == null) {
            return null;
        }
        return getPreviewURL(currentDocument);
    }

    public String getPreviewURL(DocumentModel doc) {
        return PreviewHelper.getPreviewURL(doc, fieldXPathValue);
    }

    public String getPreviewURL(DocumentModel doc, String field) {
        return PreviewHelper.getPreviewURL(doc, protectField(field));
    }

    public String getPreviewWithBlobPostProcessingURL() {
        String url = getPreviewURL();
        url += "?blobPostProcessing=true";
        return url;
    }

    public String getPreviewWithBlobPostProcessingURL(DocumentModel doc) {
        String url = getPreviewURL(doc);
        url += "?blobPostProcessing=true";
        return url;
    }

    public String getCurrentDocumentPreviewPopupURL() {
        return getPreviewPopupURL(navigationContext.getCurrentDocument());
    }

    public String getPreviewPopupURL(DocumentModel doc) {
        return getPreviewPopupURL(doc, false);
    }

    /**
     * @since 5.7
     */
    public String getPreviewPopupURL(DocumentModel doc, boolean newConversation) {
        DocumentLocation docLocation = new DocumentLocationImpl(
                doc.getRepositoryName(), doc.getRef());
        DocumentView docView = new DocumentViewImpl(docLocation,
                PREVIEW_POPUP_VIEW);
        docView.setPatternName("id");
        URLPolicyService urlPolicyService = Framework.getLocalService(URLPolicyService.class);
        String url = urlPolicyService.getUrlFromDocumentView(docView, null);
        if (!newConversation) {
            url = RestHelper.addCurrentConversationParameters(url);
        }
        return VirtualHostHelper.getContextPathProperty() + "/" + url;
    }

    @WebRemote
    public String getPreviewPopupURL(String docId) {
        try {
            DocumentModel doc = documentManager.getDocument(new IdRef(docId));
            return getPreviewPopupURL(doc, true);
        } catch (ClientException e) {
            log.error(e, e);
            return "";
        }
    }

    @Observer(value = { EventNames.DOCUMENT_SELECTION_CHANGED,
            EventNames.DOCUMENT_CHANGED }, create = false)
    @BypassInterceptors
    public void resetFields() {
        fieldXPathValue = null;
    }

    public String doSetFieldXPath() throws ClientException {
        if (fieldXPath != null) {
            fieldXPathValue = protectField(fieldXPath);
        }
        return webActions.setCurrentTabAndNavigate(previewTabId);
    }

    protected String protectField(String field) {
        return field.replace("/", "-");
    }

}
