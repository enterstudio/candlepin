/**
 * Copyright (c) 2009 - 2018 Red Hat, Inc.
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
package org.candlepin.dto.manifest.v1;

import org.candlepin.dto.AbstractTranslatorTest;
import org.candlepin.dto.ModelTranslator;
import org.candlepin.model.ConsumerType;

import static org.junit.Assert.*;

import junitparams.JUnitParamsRunner;

import org.junit.runner.RunWith;



/**
 * Test suite for the ConsumerTypeTranslator (manifest import/export) class
 */
@RunWith(JUnitParamsRunner.class)
public class ConsumerTypeTranslatorTest extends
    AbstractTranslatorTest<ConsumerType, ConsumerTypeDTO, ConsumerTypeTranslator> {

    @Override
    protected ConsumerTypeTranslator initObjectTranslator() {
        return new ConsumerTypeTranslator();
    }

    @Override
    protected void initModelTranslator(ModelTranslator modelTranslator) {
        modelTranslator.registerTranslator(this.translator, ConsumerType.class, ConsumerTypeDTO.class);
    }

    @Override
    protected ConsumerType initSourceObject() {
        ConsumerType type = new ConsumerType();

        type.setId("type_id");
        type.setLabel("type_label");
        type.setManifest(true);

        return type;
    }

    @Override
    protected ConsumerTypeDTO initDestinationObject() {
        // Nothing fancy to do here.
        return new ConsumerTypeDTO();
    }

    @Override
    protected void verifyOutput(ConsumerType source, ConsumerTypeDTO dest, boolean childrenGenerated) {

        if (source != null) {
            // This DTO does not have any nested objects, so we don't need to worry about the
            // childrenGenerated flag

            assertEquals(source.getId(), dest.getId());
            assertEquals(source.getLabel(), dest.getLabel());
            assertEquals(source.isManifest(), dest.isManifest());
        }
        else {
            assertNull(dest);
        }
    }
}
