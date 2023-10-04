package net.micropact.aea.utility;

import java.util.Objects;
import java.util.stream.Stream;

import com.entellitrak.configuration.LanguageType;

/**
 * Enum containing the Script Object Language Types that entellitrak supports.
 *
 * @author zmiller
 */
public enum ScriptObjectLanguageType {
    /**
     * Beanshell.
     */
    BEANSHELL(1, LanguageType.BEANSHELL, "Beanshell"),
    /**
     * Javascript.
     */
    JAVASCRIPT(2, LanguageType.JAVASCRIPT, "Javascript"),
    /**
     * Java.
     */
    JAVA(3, LanguageType.JAVA, "Java"),
    /**
     * HTML.
     */
    HTML(5, LanguageType.HTML, "HTML"),
    /**
     * SQL.
     */
    SQL(6, LanguageType.SQL, "SQL"),
    /**
     * CSS.
     * */
    CSS(8, LanguageType.CSS, "CSS");

    /**
     * The id which entellitrak internally uses to represent the Script Object Language.
     */
    private final long id;

    /**
     * The core language type.
     */
    private final LanguageType languageType;

    /**
     * A user-readable name used to identify the language.
     */
    private final String name;

    /**
     * Constructor.
     *
     * @param scriptObjectLanguageTypeId The id which entellitrak internally uses to represent
     *      the Script Object Language
     * @param theLanguageType the core language type
     * @param scriptObjectLanguageName A user-readable name used to identify the language
     */
    ScriptObjectLanguageType(final long scriptObjectLanguageTypeId, final LanguageType theLanguageType, final String scriptObjectLanguageName){
        id = scriptObjectLanguageTypeId;
        languageType = theLanguageType;
        name = scriptObjectLanguageName;
    }

    /**
     * Get the id entellitrak internally uses to represent the Script Object language.
     *
     * @return The id entellitrak internally uses to represent the Script Object Language
     */
    public long getId(){
        return id;
    }

    /**
     * Get the name entellitrak uses to represent the Scirpt Object Language Type.
     *
     * @return The name entellitrak uses to represent the Script Object Language Type
     */
    public String getName(){
        return name;
    }

    /**
     * Get the core language type used by entellitrak.
     *
     * @return the core language type
     */
    public LanguageType getLanguageType() {
        return languageType;
    }

    /**
     * Returns the ScriptObjectLanguageType given the id that entellitrak uses to reference it.
     *
     * @param id The id entellitrak internally uses to represent the Script Object Language
     * @return The ScriptObjectLanguageType corresponding to the given entellitrak id
     */
    public static ScriptObjectLanguageType getById(final long id){
        return Stream.of(values())
                .filter(scriptObjectLanguageType -> Objects.equals(id, scriptObjectLanguageType.getId()))
                .findAny()
                .orElseThrow(()
                    -> new IllegalArgumentException(String.format("Could not find ScriptObjectLanguageType for id: %s", id)));
    }

    /**
     * Returns the ScriptObjectLanguageType given the core entellitrak language type.
     *
     * @param theLanguageType the entellitrak language type
     * @return the script object language type.
     */
    public static ScriptObjectLanguageType getByLanguageType(final LanguageType theLanguageType){
        return Stream.of(values())
                .filter(scriptObjectLanguageType -> Objects.equals(theLanguageType, scriptObjectLanguageType.getLanguageType()))
                .findAny()
                .orElseThrow(()
                    -> new IllegalArgumentException(String.format("Could not find ScriptObjectLanguageType for language type: %s", theLanguageType)));
    }
}
