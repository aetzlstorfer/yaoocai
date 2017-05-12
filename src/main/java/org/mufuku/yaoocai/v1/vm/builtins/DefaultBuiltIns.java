package org.mufuku.yaoocai.v1.vm.builtins;

import org.mufuku.yaoocai.v1.vm.BuiltInVMFunction;

import java.util.HashMap;
import java.util.Map;

/**
 * @author andreas.etzlstorfer@ecx.io
 */
// TODO replace with dependency manager
public class DefaultBuiltIns {

    private static final Map<Short, BuiltInVMFunction> builtIns = new HashMap<>();

    static {
        builtIns.put((short) 0, new PrintInteger());
    }

    public static Map<Short, BuiltInVMFunction> getBuiltIns() {
        return builtIns;
    }
}
