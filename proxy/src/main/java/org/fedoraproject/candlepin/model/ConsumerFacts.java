/**
 * Copyright (c) 2009 Red Hat, Inc.
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
package org.fedoraproject.candlepin.model;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.MapKeyManyToMany;

/**
 * ConsumerFacts contains the metadata about a given Consumer (parent). It is 
 * a series of (name,value) pairs which allows for a more flexible model of
 * defining attributes about a Consumer.
 * 
 * For example, for a system we might capture CPU type and architecture.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
//@Entity
//@Table(name = "cp_consumer_facts")
//@SequenceGenerator(name = "seq_consumer_facts",
//        sequenceName = "seq_consumer_facts", allocationSize = 1)
public class ConsumerFacts implements Persisted {
    
    // TODO: Don't know if this is a good idea, technically the consumer +
    // metadata data key should be the identifier.
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_consumer_facts")
    private Long id;
    
    //@OneToOne(mappedBy = "facts")
    private Consumer consumer;
    
    // NOTE: Had to deviate from default EJB3 annotations here, doesn't seem
    // possible to map strings without an unplesant hack:
    // http://bit.ly/liststringjpa
    @MapKeyManyToMany(targetEntity = String.class)
    @CollectionOfElements(targetElement = String.class)
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    private Map<String, String> metadata;
    
    /**
     * default ctor
     */
    public ConsumerFacts() {
        metadata = new HashMap<String, String>();
    }
    
    /**
     * creates a new fact associated with the given Consumer.
     * @param consumerIn Consumer to be associated.
     */
    public ConsumerFacts(Consumer consumerIn) {
        metadata = new HashMap<String, String>();
        consumer = consumerIn;
    }

    /** {@inheritDoc} */
    public Long getId() {
        return id;
    }

    /**
     * @param id fact id.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the associated Consumer.
     */
    @XmlTransient
    public Consumer getConsumer() {
        return consumer;
    }

    /**
     * @param consumerIn consumer to associate.
     */
    public void setConsumer(Consumer consumerIn) {
        consumer = consumerIn;
    }
    
    /**
     * @return the metadata
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    
    /**
     * @param metadataIn replaces the metadata.
     */
    public void setMetadata(Map<String, String> metadataIn) {
        metadata = metadataIn;
    }
    
    /**
     * Sets the value of the fact with the given name.
     * @param name fact name to be modified.
     * @param value new value for the fact.
     */
    public void setFact(String name, String value) {
        if (this.metadata ==  null) {
            metadata = new HashMap<String, String>();
        }
        metadata.put(name, value);
        
    }
    
    /**
     * Returns the fact whose name matches the given name.
     * @param name fact name sought.
     * @return sthe fact whose name matches the given name.
     */
    public String getFact(String name) {
        if (this.metadata != null) {
            return metadata.get(name);
        }
        return null;
    }
}
