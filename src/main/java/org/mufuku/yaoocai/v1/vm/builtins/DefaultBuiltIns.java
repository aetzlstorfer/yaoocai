package org.mufuku.yaoocai.v1.vm.builtins;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
@SuppressWarnings("squid:S1214")
public interface DefaultBuiltIns {
    Map<Short, BuiltInVMFunction> STANDARD_BUILT_INS = ImmutableMap
            .<Short, BuiltInVMFunction>builder()
            .put((short) 1, new PrintInteger())
            .build();
}
