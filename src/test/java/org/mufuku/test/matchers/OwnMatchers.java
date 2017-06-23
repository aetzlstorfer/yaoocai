package org.mufuku.test.matchers;

import org.hamcrest.Matcher;
import org.mufuku.yaoocai.v1.bytecode.data.BCConstantPoolItem;
import org.mufuku.yaoocai.v1.bytecode.data.BCConstantPoolItemType;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
public class OwnMatchers {

    public static Matcher<BCConstantPoolItem> constantPoolItem(
            BCConstantPoolItemType expectedType,
            Object expectedValue
    ) {
        return new BCConstantPoolItemTypeMatcher(expectedType, expectedValue);
    }
}
