/**
 * Copyright (c) 2009 - 2017 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package org.candlepin.dto.api.v1;

import io.swagger.annotations.ApiModel;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.candlepin.dto.CandlepinDTO;
import org.candlepin.util.MapView;

import java.util.HashMap;
import java.util.Map;



/**
 * DTO class for compliance reasons. Used almost exclusively with ComplianceStatus.
 *
 * <tt>
 *  {
 *    "key": "string",
 *    "message": "string",
 *    "attributes": {}
 *  }
 * </tt>
 */
@ApiModel(parent = CandlepinDTO.class, description = "DTO representing a compliance reason")
public class ComplianceReasonDTO extends CandlepinDTO<ComplianceReasonDTO> {
    protected String key;
    protected String message;
    protected Map<String, String> attributes;

    public ComplianceReasonDTO() {
        // Intentionally left empty
    }

    public ComplianceReasonDTO(ComplianceReasonDTO source) {
        this.populate(source);
    }

    public String getKey() {
        return this.key;
    }

    public ComplianceReasonDTO setKey(String key) {
        this.key = key;
        return this;
    }

    public String getMessage() {
        return this.message;
    }

    public ComplianceReasonDTO setMessage(String message) {
        this.message = message;
        return this;
    }

    public Map<String, String> getAttributes() {
        return this.attributes != null ? new MapView(this.attributes) : null;
    }

    public ComplianceReasonDTO setAttributes(Map<String, String> attributes) {
        if (attributes != null) {
            if (this.attributes == null) {
                this.attributes = new HashMap<>();
            }

            this.attributes.clear();
            this.attributes.putAll(attributes);
        }
        else {
            this.attributes = null;
        }

        return this;
    }
    // addAttribute
    // removeAttribute

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof ComplianceReasonDTO) {
            ComplianceReasonDTO that = (ComplianceReasonDTO) obj;

            EqualsBuilder builder = new EqualsBuilder()
                .append(this.getKey(), that.getKey())
                .append(this.getMessage(), that.getMessage())
                .append(this.getAttributes(), that.getAttributes());

            return builder.isEquals();
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return String.format("ComplianceReasonDTO [key: %s, message: %s]", this.getKey(), this.getMessage());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(7, 17)
            .append(this.getKey())
            .append(this.getMessage())
            .append(this.getAttributes());

        return builder.toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComplianceReasonDTO clone() {
        ComplianceReasonDTO copy = super.clone();

        Map<String, String> attributes = this.getAttributes();
        copy.setAttributes(null);
        if (attributes != null) {
            copy.setAttributes(attributes);
        }

        return copy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComplianceReasonDTO populate(ComplianceReasonDTO source) {
        super.populate(source);

        this.setKey(source.getKey());
        this.setMessage(source.getMessage());
        this.setAttributes(source.getAttributes());

        return this;
    }
}
