package nuxeo.csv.zip.importer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.transaction.TransactionHelper;

@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.csv.core", "org.nuxeo.ecm.platform.types.api", "org.nuxeo.ecm.platform.types.core",
        "org.nuxeo.ecm.platform.filemanager.api", "org.nuxeo.ecm.platform.filemanager.core",
        "nuxeo.csv.zip.importer.nuxeo-csv-zip-importer-core" })
public class TestCSVZipImporterOp {

    @Inject
    protected CoreSession coreSession;

    @Inject
    protected AutomationService automationService;

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
        coreSession.removeDocument(new PathRef("/"));
    }

    @Test
    public void shouldUnzipWithDocumentInput() throws OperationException {

        File zipFile = FileUtils.getResourceFileFromContext("test-1.zip");
        Blob zipBlob = new FileBlob(zipFile);

        // Create at root level so TestCommons.checkWithTest1Zip is OK when getting children of testFolder
        DocumentModel doc = coreSession.createDocumentModel("/", "test-1", "File");
        doc.setPropertyValue("file:content", (Serializable) zipBlob);
        doc.setPropertyValue("dc:title", "test-1.zip");

        OperationContext ctx = new OperationContext(coreSession);
        ctx.setInput(doc);
        Map<String, Object> params = new HashMap<>();
        params.put("parent", testFolder.getId());
        DocumentModel result = (DocumentModel) automationService.run(ctx, CSVZipImporterOp.ID, params);

        assertNotNull(result);

        // Must test against the container, testFolder
        String testResult = TestCommons.checkWithTest1Zip(coreSession, testFolder);
        assertEquals("Wrong result: " + testResult, TestCommons.RESULT_OK, testResult);

    }
}
