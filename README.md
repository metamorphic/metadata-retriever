Metadata Retriever
==================

This processor module reads the "file_name" field from the header of an incoming message and attempts to retrieve
metadata for this file from the Metastore. Once found, metadata is cached in the Java heap to prevent making
redundant calls.

### Install Metadata Retriever Module

    xd:>module delete processor:metadata-retriever
    Successfully destroyed module 'metadata-retriever' with type processor

    xd:>module upload --file /home/cxp/big-data-cxp/metadata-retriever/build/libs/metadata-retriever-1.0.0.BUILD-SNAPSHOT.jar  --name metadata-retriever --type processor
    Successfully uploaded module 'processor:metadata-retriever'

    xd:>module info processor:metadata-retriever
    Information about processor module 'metadata-retriever':

    Option Name      Description                                            Default                                                               Type
    ---------------  -----------------------------------------------------  --------------------------------------------------------------------  --------
    metastoreApiUrl  metastore search-by-filename API URL                   http://localhost:8080/api/file-datasets/search/by-filename?filename=  String
    outputType       how this module should emit messages it produces       <none>                                                                MimeType
    inputType        how this module should interpret messages it consumes  <none>                                                                MimeType

### Test Metadata Retriever Module Only

    xd:> stream create --name myevents --definition "file --dir=/home/cxp/Data/inbox --pattern=*.* --preventDuplicates=false --ref=false --fixedDelay=10 --outputType=text/plain | adata-retriever |  log" --deploy
    Created and deployed new stream 'myevents'

    xd:>stream destroy --name myevents
    Destroyed stream 'myevents'

### Using Metadata Retriever together with Splitter

    xd:>stream create --name myevents --definition "file --dir=/home/cxp/Data/inbox --pattern=*.* --preventDuplicates=false --ref=false --fixedDelay=10 --outputType=text/plain | metadata-retriever | splitter --expression=payload.split(headers.get('row_delimiter')) | log" --deploy
    Created and deployed new stream 'myevents'

    xd:>stream destroy --name myevents
    Destroyed stream 'myevents'

### Setting the Metastore API URL Parameter

    xd:>stream create --name myevents --definition "file --dir=/home/cxp/Data/inbox --pattern=*.* --preventDuplicates=false --ref=false --fixedDelay=10 --outputType=text/plain | metadata-retriever --metastoreApiUrl=http://www.example.com:8080/api/file-datasets/search/by-filename?filename=  | splitter --expression=payload.split(headers.get('row_delimiter')) | log" --deploy
