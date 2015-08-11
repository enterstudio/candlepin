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
package org.candlepin.sync;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import org.candlepin.audit.EventSink;
import org.candlepin.model.Rules;
import org.candlepin.model.RulesCurator;
import org.candlepin.policy.js.JsRunnerProvider;
import org.candlepin.test.DatabaseTestFixture;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.StringReader;

import javax.inject.Inject;

/**
 * RulesImporterTest
 */
@RunWith(MockitoJUnitRunner.class)
public class RulesImporterTest extends DatabaseTestFixture {
    @Inject private EventSink sink;

    @Mock private RulesCurator curator;
    @Mock private JsRunnerProvider jsProvider;
    private RulesImporter importer;

    @Before
    public void setUp() {
        importer = new RulesImporter(curator, sink, jsProvider);
    }

    @Test
    public void importNewerRulesSameApi() throws IOException {
        Rules currentRules = new Rules("//Version: 2.0");
        when(curator.getRules()).thenReturn(currentRules);

        importer.importObject(new StringReader("//Version: 2.1"));
        verify(curator).update(any(Rules.class));
    }

    @Test
    public void importSkipsOlderRulesSameApi() throws IOException {
        Rules currentRules = new Rules("// Version: 2.1");
        when(curator.getRules()).thenReturn(currentRules);

        importer.importObject(new StringReader("// Version: 2.0"));
        verify(curator, never()).update(any(Rules.class));
    }

    @Test
    public void importSkipsOlderRulesDifferentApi() throws IOException {
        Rules currentRules = new Rules("// Version: 2.1");
        when(curator.getRules()).thenReturn(currentRules);

        importer.importObject(new StringReader("//Version: 1.0\n//rules"));
        verify(curator, never()).update(any(Rules.class));
    }

    @Test
    public void importSkipsNewerRulesDifferentApi() throws IOException {
        Rules currentRules = new Rules("// Version: 2.1");
        when(curator.getRules()).thenReturn(currentRules);

        importer.importObject(new StringReader("//Version: 3.0"));
        verify(curator, never()).update(any(Rules.class));
    }

    static class RulesMatcher extends ArgumentMatcher<Rules> {
        private String rule;

        public RulesMatcher(String rule) {
            this.rule = rule;
        }

        public boolean matches(Object rules) {
            return ((Rules) rules).getRules().equals(rule);
        }
    }

}
