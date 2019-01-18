/*
 * (C) Copyright 2019 Nuxeo (http://nuxeo.com/) and others.
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
 *     Jackie Aldama <jaldama@nuxeo.com>
 *     Thibaud Arguillere
 */
package nuxeo.csv.zip.importer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.filemanager.api.FileManager;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.transaction.TransactionHelper;

/**
 * 
 * @since 9.10
 */
@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy({"org.nuxeo.ecm.csv.core",
    "org.nuxeo.ecm.platform.types.api",
    "org.nuxeo.ecm.platform.types.core",
    "org.nuxeo.ecm.platform.filemanager.api",
    "org.nuxeo.ecm.platform.filemanager.core",
    "nuxeo.csv.zip.importer.nuxeo-csv-zip-importer-core"})
public class TestCSVZipImporter {

    @Inject
    protected CoreSession coreSession;
    
    protected DocumentModel testFolder;
    
    @Before
    public void create_data() {
        // Create ContentLibrary
        testFolder = coreSession.createDocumentModel("/", "testFolder", "Folder");
        testFolder.setPropertyValue("dc:title", "testFolder");
        testFolder = coreSession.createDocument(testFolder);

        coreSession.save();
        TransactionHelper.commitOrRollbackTransaction();
        TransactionHelper.startTransaction();
    }
    
    @After
    public void cleanup() {
        coreSession.removeDocument(testFolder.getRef());
    }
    
    @Test
    public void shouldImportCsvZip() throws Exception {
        
        File zipFile = FileUtils.getResourceFileFromContext("test-1.zip");
        Blob zipBlob = new FileBlob(zipFile);
        
        FileManager fm = Framework.getService(FileManager.class);
        DocumentModel doc = fm.createDocumentFromBlob(coreSession, zipBlob, testFolder.getPathAsString(), true, "test-1.zip");
                
        assertNotNull(doc);
        // Should return the container
        assertEquals(doc.getId(), testFolder.getId());
        
        String testResult = TestCommons.checkWithTest1Zip(coreSession, doc);
        assertEquals("Wrong result: " + testResult, TestCommons.RESULT_OK, testResult);
        
    }
    
    @Test
    public void shouldNotImportNonCsvZip() throws Exception {
        
        File zipFile = FileUtils.getResourceFileFromContext("not-a-csv-zip.zip");
        Blob zipBlob = new FileBlob(zipFile);
        
        FileManager fm = Framework.getService(FileManager.class);
        DocumentModel doc = fm.createDocumentFromBlob(coreSession, zipBlob, testFolder.getPathAsString(), true, "not-a-csv-zip.zip");
        
        assertNotNull(doc);
        assertFalse(doc.hasFacet("Folderish"));
        Blob blob = (Blob) doc.getPropertyValue("file:content");
        assertNotNull(blob);
        assertEquals("not-a-csv-zip.zip", blob.getFilename());
        assertEquals("not-a-csv-zip.zip", doc.getTitle());
    }

}
