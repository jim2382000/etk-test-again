package net.micropact.aea.core.enums.cf;

public enum DefaultCfElementType {

    DATA_ELEMENT("code.usr.cf.elementType.dataElement", "Data Element", 1),
    CUSTOM_SQL("code.usr.cf.elementType.custom.sql", "Custom - SQL", 2),
    CUSTOM_SCRIPT_OBJECT("code.usr.cf.elementType.custom.scriptObject", "Custom - Script Object", 3),
    STATIC_TEXT("code.usr.cf.elementType.custom.staticText", "Static Text", 4);

    private String code;
    private String name;
    private Integer order;

    private DefaultCfElementType(final String code, final String name, final Integer order) {
        this.code = code;
        this.name = name;
        this.order = order;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Integer getOrder() {
        return order;
    }

}
