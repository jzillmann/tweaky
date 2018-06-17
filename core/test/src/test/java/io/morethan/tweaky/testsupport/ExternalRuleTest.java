package io.morethan.tweaky.testsupport;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;

class ExternalRuleTest {

    private static int _classRuleBeforeCalled;
    private static int _classRuleAfterCalled;
    private static int _testRuleBeforeCalled;
    private static int _testRuleAfterCalled;

    @RegisterExtension
    static ExternalResource _classRule = new ExternalResource() {

        @Override
        protected void before(ExtensionContext context) throws Exception {
            System.out.println("Before: " + context.getDisplayName());
            _classRuleBeforeCalled++;
        }

        @Override
        protected void after(ExtensionContext context) throws Exception {
            System.out.println("After: " + context.getDisplayName());
            _classRuleAfterCalled++;
        }
    };

    @RegisterExtension
    ExternalResource _testRule = new ExternalResource() {

        @Override
        protected void before(ExtensionContext context) throws Exception {
            System.out.println("Before: " + context.getDisplayName());
            _testRuleBeforeCalled++;
        }

        @Override
        protected void after(ExtensionContext context) throws Exception {
            System.out.println("After: " + context.getDisplayName());
            _testRuleAfterCalled++;
        }
    };

    @Test
    void test1() {
        check();

    }

    @Test
    void test2() {
        check();
    }

    private void check() {
        boolean isFirstTest = _testRuleBeforeCalled == 1;
        if (isFirstTest) {
            assertThat(_classRuleBeforeCalled).isEqualTo(1);
            assertThat(_testRuleBeforeCalled).isEqualTo(1);
            assertThat(_classRuleAfterCalled).isEqualTo(0);
            assertThat(_testRuleAfterCalled).isEqualTo(0);
        } else {
            assertThat(_classRuleBeforeCalled).isEqualTo(1);
            assertThat(_testRuleBeforeCalled).isEqualTo(2);
            assertThat(_classRuleAfterCalled).isEqualTo(0);
            assertThat(_testRuleAfterCalled).isEqualTo(1);
        }
    }

}
