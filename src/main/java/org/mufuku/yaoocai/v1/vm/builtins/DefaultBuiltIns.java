package org.mufuku.yaoocai.v1.vm.builtins;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
@SuppressWarnings("squid:S1214")
public interface DefaultBuiltIns {
    Map<String, BuiltInVMFunction> STANDARD_BUILT_INS = ImmutableMap
            .<String, BuiltInVMFunction>builder()
            .put("printlnInteger", new PrintLnInteger())
            .build();
}
