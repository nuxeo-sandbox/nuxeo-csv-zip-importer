/*
/*
 * (C) Copyright 2018 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Thibaud Arguillere
 */
package nuxeo.csv.zip.importer;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;

/**
 *
 */
@Operation(id = CSVZipImporterOp.ID, category = Constants.CAT_DOCUMENT, label = "CSV Zip Importer", description = "Expands the zip in parent (must be Folderish) and set metadata for the files. The zip must have a meta-data.csv at first level. All files must also be at first levele (no folders) in the .zip. If input is a Document, xpath is used to get its blob")
public class CSVZipImporterOp {

    public static final String ID = "Document.CSVZipImporterOp";

    @Context
    protected CoreSession session;

    @Param(name = "parent", required = true)
    protected DocumentModel parent;

    @Param(name = "xpath", required = false, values = { "file:content" })
    protected String xpath = "file:content";

    @OperationMethod

    public DocumentModel run(DocumentModel doc) throws IOException {
        Blob blob = (Blob) doc.getPropertyValue(xpath);

        blob = run(blob);

        return doc;
    }

    @OperationMethod
    public Blob run(Blob blob) throws IOException {

        CSVZipImporter importer = new CSVZipImporter();

        importer.create(session, blob, parent.getPathAsString(), true, blob.getFilename(), null);

        return blob;
    }
}
