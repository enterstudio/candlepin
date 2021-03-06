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
package org.candlepin.dto.api.v1;

import org.candlepin.dto.AbstractTranslatorTest;
import org.candlepin.dto.ModelTranslator;
import org.candlepin.model.User;

import static org.junit.Assert.*;

import junitparams.JUnitParamsRunner;

import org.junit.runner.RunWith;



/**
 * Test suite for the UserTranslator class
 */
@RunWith(JUnitParamsRunner.class)
public class UserTranslatorTest extends AbstractTranslatorTest<User, UserDTO, UserTranslator> {

    protected UserTranslator translator = new UserTranslator();

    @Override
    protected void initModelTranslator(ModelTranslator modelTranslator) {
        modelTranslator.registerTranslator(this.translator, User.class, UserDTO.class);
    }

    @Override
    protected UserTranslator initObjectTranslator() {
        return this.translator;
    }

    @Override
    protected User initSourceObject() {
        User user = new User();

        user.setId("user_id");
        user.setUsername("user_username");
        user.setPassword("user_password");
        user.setSuperAdmin(true);

        return user;
    }

    @Override
    protected UserDTO initDestinationObject() {
        // Nothing fancy to do here.
        return new UserDTO();
    }

    @Override
    protected void verifyOutput(User source, UserDTO dest, boolean childrenGenerated) {

        if (source != null) {
            // This DTO does not have any nested objects, so we don't need to worry about the
            // childrenGenerated flag

            assertEquals(source.getId(), dest.getId());
            assertEquals(source.getUsername(), dest.getUsername());
            assertEquals(source.isSuperAdmin(), dest.isSuperAdmin());

            // Under no circumstance should we be copying over the password field on translation.
            // This should always be null on the DTO.
            assertNotNull(source.getPassword());
            assertNull(dest.getHashedPassword());
        }
        else {
            assertNull(dest);
        }
    }
}
