# nuxeo-csv-zip-importer

QA status<br/>
_(TBD)_

This plug-in allows users to drop a zip file containing files to import and a `data.csv` file. The zip will be expanded and Documents will be created accordingly to the csv: Type of document and metadata.

Syntax for the CSV file is the same as the default [CSV Importer](https://doc.nuxeo.com/nxdoc/nuxeo-csv/), in the file:content column, set the exact key of the file stored in the zip.

So, for example, say we have a zip file containing the following...

* data.csv
* file.pdf
* image.jpg
* video.mp4

...and the `data.csv` file is:

```
type,name,dc:title,file:content
File,file,A File,file.pdf
File,image,An Image,image.jpg
File,video,A Video,video.mp4
```

Dropping this file in a container will create 3 documents:

* A `File`, whose title is `A File` and whose blob is the `file.pdf` found in the .zip
* A `Picture`, whose title is `An Image` and whose blob is the `image.jpg` found in the .zip
* A `Video`, whose title is `A Video` and whose blob is the `video.mp4` found in the .zip

You will have the same result if files to import ar in subfolders: The relative path to in `data.csv` must match the key. For texample, the following will get the same results as above:

```
data?csv
files
  file.pdf
images
  image.jpg
videos
  video.mp4
```

With `data.csv`:

```
type,name,dc:title,file:content
File,file,A File,files/file.pdf
File,image,An Image,images/image.jpg
File,video,A Video,videos/video.mp4
```


## WARNINGS - Please, Read

### Format of the .zip

As explained above, the `data.csv` file must be at very first level of the zip, not inside a container (`myfolder/data.csv` => data.csv is not found and the .zip will be imported as is, with no extraction). The plug-in will not work if you zip a folder for example. So, usually, on your Desktop, put everything in the same folder. Files to import can be in subfolders, just set the `file:content` value accordingly (see example above). then select the `data.csv` file and the files to add, then compress as .zip.

### Field Storage for the Blob and Tests

As of today the plugin has only be tested with `file:content`, and contains code to handle this field. If you face issues when storing files in another schema, please, upgrade the plug-in or let us know :-)

Also, it has not been tested against every possible field types and scenario (like setting up a complex, multivalued fields with JSON, etc.)

### Transaction
Every 100 documents created, the transaction is commited to avoid database issue if the zip contains a lot of files.

Notice the plugin has not been tested when the zip contains a lot of files.

## Event and Context
* During the import, every document created/modified will have an event context data `DocumentHandledByCsvZipImporter` set to `true`. This allows developers to possibly adapt their listeners (`empty document created`, `about to create`, ...) if needed.

* At the end of the import, the global `CsvZipImportDone` event is fired, with the container as document input.

## Operation
An operation is provided to perform the extraction when the document was not created with drag and drop in the UI (or using the `FileManager` from a REST call, ...)

### Document.CSVZipImporterOp (CSV Zip Importer)

The operation creates the documents and sets their fields.

* **Input**:
  * `Document` or `Blob`
  * If `Document`, `xpath` parameter can be set to get the blob (default "file:content")
* **Parameters**
  * `parent`:
    * Required
    * The id or path of the container where the documents will be created.
    * Make sure current user has the right to create children in this destination.
  * `xpath`
    * Optional
    * When the input is a Document, you can specify another field than the default `file:content`
* Return the input, unchanged (Document or Blob)

## Disabling the Automatic Handling
When a .zip file is dropped, if it does not contain te `data.csv` file at first level, it is ignored and the next plug-in in the FileManager system will look at it. This means that not every .zip will be extracted, only the ones with the `data.csv` file.

If you prefer to always handle the files manually via the `Document.CSVZipImporterOp` operation, you can contribute the following XML extension to your Studio project to disable the automatic behavior:

```
<extension
    target="org.nuxeo.ecm.platform.filemanager.service.FileManagerService"
    point="plugins">
  <plugin name="CSVZipImporter" enabled="false"></plugin>
</extension>
```


## Build

    git clone https://github.com/nuxeo-csv-zip-importer.git
    cd nuxeo-csv-zip-importer
    
    mvn clean install


## Support

**These features are not part of the Nuxeo Production platform, they are not supported**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.


# Licensing

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)


# About Nuxeo

Nuxeo dramatically improves how content-based applications are built, managed and deployed, making customers more agile, innovative and successful. Nuxeo provides a next generation, enterprise ready platform for building traditional and cutting-edge content oriented applications. Combining a powerful application development environment with SaaS-based tools and a modular architecture, the Nuxeo Platform and Products provide clear business value to some of the most recognizable brands including Verizon, Electronic Arts, Sharp, FICO, the U.S. Navy, and Boeing. Nuxeo is headquartered in New York and Paris.

More information is available at [www.nuxeo.com](http://www.nuxeo.com).  
