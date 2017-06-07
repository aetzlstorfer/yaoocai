package org.mufuku.yaoocai.v1.compiler.ast;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class ASTObjectTests {
    @Test
    public void test_equals_hashCode() {
        EqualsVerifier.forClass(ASTType.class).suppress(Warning.STRICT_INHERITANCE).withNonnullFields("typeName").verify();
    }
}