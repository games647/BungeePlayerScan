package com.github.games647.playerscan;

import java.util.function.Supplier;

class SubMenu {

    private final String title;
    private final Supplier<String> value;

    public SubMenu(String title, Supplier<String> value) {
        this.title = title;
        this.value = value;
    }

    String getTitle() {
        return title;
    }

    String getValue() {
        return value.get();
    }
}
