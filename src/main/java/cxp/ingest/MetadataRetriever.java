package cxp.ingest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.LocalDateTime;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

//REFERENCE
//https://github.com/spring-projects/spring-framework/blob/master/spring-messaging/src/main/java/org/springframework/messaging/MessageHeaders.java

/**
 * @author Aung Htet
 */
public class MetadataRetriever {

    private static final Log log = LogFactory.getLog(MetadataRetriever.class);

    MetadataProvider metadataProvider;

    FileDataset fileDataset;
    ObjectMapper mapper;

    // Options
    // The following provided as command line arguments or
    // using defaults set by StreamProcessorOptionsMetadata

    String metastoreApiUrl;

    String outputFormat = "LINE";   // LINE OR JSON

    String columnDelimiter = "\t";  // Does not apply if output is JSON

    int metadataCacheLife = 15;     // minutes to cache metadata


    Map<String, String> metadataCache;
    Map<String, LocalDateTime> metadataCacheExpiry;

    public MetadataRetriever(String metastoreApiUrl, int metadataCacheLife) {
        this.metastoreApiUrl = metastoreApiUrl;
        mapper = new ObjectMapper();
        metadataCache = new HashMap<String, String>();
        metadataCacheExpiry = new HashMap<String, LocalDateTime>();

        if (metadataCacheLife >= 0)
            this.metadataCacheLife = metadataCacheLife;
    }

    public Object process(final Message<?> message) throws Exception {
        Object payload = message.getPayload();
        MessageHeaders headers = message.getHeaders();
        String filename = (String) headers.get("file_name");
        LocalDateTime now = LocalDateTime.now();

        if (metadataCache.containsKey(filename) && now.isBefore(metadataCacheExpiry.get(filename))) {
            if (log.isDebugEnabled()) {
                log.debug("Metadata found in cache");
            }
            String metadata = metadataCache.get(filename);
            fileDataset = mapper.readValue(metadata, FileDataset.class);

            Assert.notNull(fileDataset);

        } else {
            if (log.isDebugEnabled()) {
                log.debug("Metadata received from metastore");
            }

            // Call metadata service and set cache
            metadataProvider.setFilename(filename);
            fileDataset = metadataProvider.getFileDataset();

            Assert.notNull(fileDataset);

            metadataCache.put(filename, mapper.writeValueAsString(fileDataset));
            metadataCacheExpiry.put(filename, now.plusMinutes(metadataCacheLife));
        }

        // Create output message - simply pass the same payload and generate new headers
        Map<String, Object> newHeaders = new HashMap<String, Object>();
        newHeaders.put("file_name", filename);
        newHeaders.put("row_delimiter", fileDataset.getRowDelimiter());

        return new GenericMessage<>(payload, newHeaders);
    }

    public void setMetadataProvider(MetadataProvider metadataProvider) {
        if (metastoreApiUrl != null && !metastoreApiUrl.isEmpty()) {
            metadataProvider.setDatasetUrl(metastoreApiUrl);
        }
        this.metadataProvider = metadataProvider;
    }
}
