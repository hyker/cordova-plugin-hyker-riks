package io.hyker.plugin;


import io.hyker.riks.box.RiksWhitelist;

/**
 * Created by joakimb on 2017-04-27.
 */

public class Whitelist implements RiksWhitelist {
    @Override
    public boolean allowedForKey(String s, String s1, String s2) {
        return true;
    }

    @Override
    public void newKey(String s) {

    }

}
