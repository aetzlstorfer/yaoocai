package org.mufuku.yaoocai.v1.vm.builtins;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andreas Etzlstorfer (a.etzlstorfer@gmail.com)
 */
// TODO replace with dependency manager
public class DefaultBuiltIns {

    private static final Map<Short, BuiltInVMFunction> builtIns = new HashMap<>();

    static {
        builtIns.put((short) 1, new PrintInteger());
    }

    public static Map<Short, BuiltInVMFunction> getBuiltIns() {
        return builtIns;
    }
}
