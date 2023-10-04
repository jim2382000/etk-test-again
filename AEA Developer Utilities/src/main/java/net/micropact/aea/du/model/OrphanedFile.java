/**
 *
 * OrphanedFile model class.
 *
 * derek.perriero 07/07/2020
 **/

package net.micropact.aea.du.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class OrphanedFile {

    private Long fileId;
    private String objectType;

    public OrphanedFile(final Long fileId, final String objectType) {
        super();
        this.fileId = fileId;
        this.objectType = objectType;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(final Long fileId) {
        this.fileId = fileId;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(final String objectType) {
        this.objectType = objectType;
    }

    @Override
    public String toString() {
    	return ToStringBuilder.reflectionToString(this);
    }
}
