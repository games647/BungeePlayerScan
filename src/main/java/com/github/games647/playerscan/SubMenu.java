package com.github.games647.playerscan;

import java.util.function.Supplier;

public class SubMenu {

    private final String title;
    private final Supplier<String> value;

    public SubMenu(String title, Supplier<String> value) {
        this.title = title;
        this.value = value;
    }

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value.get();
    }
}
