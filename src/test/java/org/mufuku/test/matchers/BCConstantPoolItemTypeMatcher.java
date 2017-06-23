package org.mufuku.test.matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.mufuku.yaoocai.v1.bytecode.data.BCConstantPoolItem;
import org.mufuku.yaoocai.v1.bytecode.data.BCConstantPoolItemType;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class BCConstantPoolItemTypeMatcher extends TypeSafeMatcher<BCConstantPoolItem> {

    private final BCConstantPoolItemType expectedType;
    private final Object expectedValue;

    BCConstantPoolItemTypeMatcher(BCConstantPoolItemType expectedType, Object expectedValue) {
        super(BCConstantPoolItem.class);
        this.expectedType = expectedType;
        this.expectedValue = expectedValue;
    }

    @Override
    protected boolean matchesSafely(BCConstantPoolItem constantPoolItem) {
        return expectedType.equals(constantPoolItem.getType()) &&
                expectedValue.equals(constantPoolItem.getValue());
    }

    @Override
    public void describeTo(Description description) {
        description
                .appendValue(expectedType)
                .appendText(", ")
                .appendValue(expectedValue);
    }
}
