package cxp.ingest;

import org.springframework.xd.module.options.spi.ModuleOption;

public class MetadataRetrieverOptionsMetadata {

    private String metastoreApiUrl = null;

    private int metadataCacheLife = 15; // minutes

    public String getMetastoreApiUrl() {
        return metastoreApiUrl;
    }

    public int getMetadataCacheLife() {
        return metadataCacheLife;
    }

    @ModuleOption("Metastore search-by-filename API URL.")
    public void setMetastoreApiUrl(String metastoreApiUrl) {
        this.metastoreApiUrl = metastoreApiUrl;
    }

    @ModuleOption("How long (in minutes) the metadata for a file should be cached before accessing Metastore again.")
    public void setMetadataCacheLife(int metadataCacheLife) {
        this.metadataCacheLife = metadataCacheLife;
    }
}
