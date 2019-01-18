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
 *     Thibaud Arguillere
 */
package nuxeo.csv.zip.importer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;

/**
 * 
 * @since 9.10
 */
public class TestCommons {
    
    public static final String RESULT_OK = "OK";
    
    public static final String RESULT_INVALID_DOC_TO_TEST = "Doc to test is null or not Folderish";
    
    public static final String RESULT_WRONG_CHILDREN_COUNT = "Wrong children count";
    
    public static final String RESULT_MISSING_BLOB = "Missing blob";
    
    public static final String RESULT_MISSING_DOC = "One doc not found: ";

    /*
     * See content of test-1.zip, and the meta-data.csv
     */
    public static String checkWithTest1Zip(CoreSession coreSession, DocumentModel doc) {
        
        if(doc == null || !doc.hasFacet("Folderish")) {
            return RESULT_INVALID_DOC_TO_TEST;
        }
        
        DocumentModelList children = coreSession.getChildren(doc.getRef());
        if(children.size() !=  3) {
            return RESULT_WRONG_CHILDREN_COUNT;
        }
        
        ArrayList<String> childrenInfo = new ArrayList<String>();
        for(DocumentModel oneDoc : children) {
            Blob blob = (Blob) oneDoc.getPropertyValue("file:content");
            if(blob == null) {
                return RESULT_MISSING_BLOB;
            }
            
            childrenInfo.add(blob.getFilename() + "-" + oneDoc.getPropertyValue("dc:description") + "-" + oneDoc.getType());
        }
        String [] expectedValues = {
          "file-1.pdf-abc-File",
          "image-1.jpg-def-File",
          "video-1.mp4-ghi-File",      
        };
        for(String testValue : expectedValues) {
            if(!childrenInfo.contains(testValue)) {
                return RESULT_MISSING_DOC + testValue;
            }
        }
        
        return RESULT_OK;
    }
}
