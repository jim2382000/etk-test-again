package net.micropact.aea.core.enums;

import java.util.Arrays;
import java.util.Objects;

import com.entellitrak.configuration.ThemeType;

/**
 * Enum representing the entellitrak themes.
 *
 * @author Zachary.Miller
 */
public enum Theme {

    /**
     * Hydrogen.
     */
    HYDROGEN(ThemeType.HYDROGEN, "default"),
    /**
     * Helium.
     */
    HELIUM(ThemeType.HELIUM, "helium");

    private final ThemeType themeType;
    private final String cssFolder;

    /**
     * Simple constructor.
     *
     * @param theThemeType the theme type
     * @param theCssFolder the CSS folder (within web-pub)
     */
    Theme(final ThemeType theThemeType, final String theCssFolder) {
        themeType = theThemeType;
        cssFolder = theCssFolder;
    }

    /**
     * Get the folder within web-pub that the theme CSS is stored in.
     *
     * @return the CSS folder
     */
    public String getCssFolder() {
        return cssFolder;
    }

    /**
     * Get the theme, based off of the entellitrak theme type.
     *
     * @param themeType the entellitrak theme type
     * @return the theme
     */
    public static Theme getFromThemeType(final ThemeType themeType){
        return Arrays.stream(values())
                .filter(theme -> Objects.equals(theme.themeType, themeType))
                .findAny()
                .orElse(null);
    }
}
