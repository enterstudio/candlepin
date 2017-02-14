/**
 * Copyright (c) 2009 - 2012 Red Hat, Inc.
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
package org.candlepin.model;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import org.hibernate.Session;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;



/**
 * ContentCurator
 */
public class ContentCurator extends AbstractHibernateCurator<Content> {

    private static Logger log = LoggerFactory.getLogger(ContentCurator.class);

    private ProductCurator productCurator;

    @Inject
    public ContentCurator(ProductCurator productCurator) {
        super(Content.class);

        this.productCurator = productCurator;
    }

    // Needs an override due to the use of UUID as db identifier.
    @Override
    @Transactional
    public void delete(Content entity) {
        Content toDelete = find(entity.getUuid());
        currentSession().delete(toDelete);
    }

    /**
     * Retrieves a Content instance for the specified content UUID. If no matching content could be
     * be found, this method returns null.
     *
     * @param uuid
     *  The UUID of the content to retrieve
     *
     * @return
     *  the Content instance for the content with the specified UUID or null if no matching content
     *  was found.
     */
    @Transactional
    public Content lookupByUuid(String uuid) {
        return (Content) currentSession().createCriteria(Content.class).setCacheable(true)
            .add(Restrictions.eq("uuid", uuid)).uniqueResult();
    }

    /**
     * Fetches a collection of content used by the given products
     *
     * @param products
     *  The products for which to fetch content
     *
     * @return
     *  A collection of content used by the specified products
     */
    @SuppressWarnings("unchecked")
    public CandlepinQuery<Content> getContentByProducts(Collection<Product> products) {
        Collection<String> uuids = new HashSet<String>();

        for (Product product : products) {
            uuids.add(product.getUuid());
        }

        return this.getContentByProductUuids(uuids);
    }

    /**
     * Fetches a collection of content used by the given products
     *
     * @param productUuids
     *  A collection of UUIDs representing the products for which to fetch content
     *
     * @return
     *  A collection of content used by the specified products
     */
    @SuppressWarnings("unchecked")
    public CandlepinQuery<Content> getContentByProductUuids(Collection<String> productUuids) {
        if (productUuids != null && !productUuids.isEmpty()) {
            // We're doing this in two queries because (a) that's what Hibernate's doing already due
            // to the projection and (b) DISTINCT_ROOT_ENTITY only works when listing, not when
            // scrolling.
            Session session = this.currentSession();

            // For reasons I can only speculate, Hibernate literally refuses* to run this query
            // when built with HQL or Criteria, so we're doing it in straight SQL.
            // * By refuses, I mean silently returns an empty list without ever actually hitting
            // the database.
            Collection<String> uuids = new HashSet<String>();

            // Make sure we don't hit the parameter limit when building queries...
            Iterable<List<String>> uuidBlocks = Iterables.partition(productUuids,
                AbstractHibernateCurator.QUERY_PARAMETER_LIMIT);

            for (List<String> uuidBlock : uuidBlocks) {
                StringBuilder builder = new StringBuilder("SELECT DISTINCT content_uuid FROM ")
                    .append(ProductContent.DB_TABLE)
                    .append(" WHERE (");

                int blockCount = (int) Math.ceil(uuidBlock.size() /
                    (float) AbstractHibernateCurator.IN_OPERATOR_BLOCK_SIZE);

                for (int i = 0; i < blockCount;) {
                    if (i != 0) {
                        builder.append(" OR ");
                    }

                    builder.append("product_uuid IN (?").append(++i).append(')');
                }

                builder.append(')');

                log.debug("getContentByProductUuids query: {}", builder);
                SQLQuery query = session.createSQLQuery(builder.toString());

                int param = 0;
                Iterable<List<String>> blocks = Iterables.partition(uuidBlock,
                    AbstractHibernateCurator.IN_OPERATOR_BLOCK_SIZE);

                for (List<String> block : blocks) {
                    query.setParameterList(String.valueOf(++param), block);
                }

                // Add the uuids to our ever-growing collection
                uuids.addAll(query.list());
            }

            if (uuids != null && !uuids.isEmpty()) {
                DetachedCriteria criteria = this.createSecureDetachedCriteria()
                    .add(CPRestrictions.in("uuid", uuids));

                return this.cpQueryFactory.<Content>buildQuery(session, criteria);
            }
        }

        return this.cpQueryFactory.<Content>buildQuery();
    }
}
