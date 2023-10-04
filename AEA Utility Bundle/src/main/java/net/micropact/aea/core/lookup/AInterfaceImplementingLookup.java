package net.micropact.aea.core.lookup;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.lookup.ListBasedLookupHandler;
import com.entellitrak.lookup.LookupExecutionContext;
import com.entellitrak.lookup.LookupHandler;
import com.entellitrak.lookup.LookupResult;

import net.entellitrak.aea.lookup.IAeaLookupHandler;
import net.micropact.aea.core.reflection.InterfaceImplementationUtility;

/**
 * <p>
 *  This abstract class is for helping to implement {@link LookupHandler}s which are simply a list of java Script Objects
 *  which must implement a particular interface. An example is the lookup of IScripts on the the RF Script object.
 * </p>
 * <p>
 *  This handler returns Script Objects from the system repository.
 *  Both the Value and Display for this lookup is the fully qualified name of the script object.
 *  For instance: "net.entellitrak.aea.rf.RulesFramework"
 *  The reason for choosing this to be the value is that despite the fact that it is a calculated field,
 *  there is no other good choice. The script_id changes when you apply changes,
 *  the business key may be different in a different system,
 *  the name is not unique, and code which needs to use this data element is likely to be
 *  interested in the fully qualified name anyway.
 * </p>
 * <p>
 *  In order to use this class, you only need to extend it, and call its constructor from the subclass's default
 *  constructor.
 * </p>
 *
 * @author zachary.miller
 */
public class AInterfaceImplementingLookup implements ListBasedLookupHandler<String>, IAeaLookupHandler {

    private final Class<?> theInterfaceClass;

    /**
     * Constructor which must be called from the subclass's.
     *
     * @param theInterface the interface which Script Objects must implement
     */
    protected AInterfaceImplementingLookup(final Class<?> theInterface) {
        theInterfaceClass = theInterface;
    }

    @Override
    public List<LookupResult> getLookupResults(final LookupExecutionContext etk) throws ApplicationException {
        return formatResults(
                InterfaceImplementationUtility.getInterfaceImplementations(etk, theInterfaceClass)
                .stream());
    }

    @Override
    public List<LookupResult> getSelectedLookupResults(final LookupExecutionContext etk, final List<String> selectedValues)
            throws ApplicationException {
        return formatResults(selectedValues.stream()
                .map(className -> {
                    try {
                        return Class.forName(className);
                    } catch (final ClassNotFoundException e) {
                    	etk.getLogger().error(
                                String.format("Error getting selected values for scripts implementing interface %s. A selected value was not found. This probably means you have old data which has not been cleaned up, or that you need to check out the system repository.",
                                        theInterfaceClass.getName()),
                                e);
                        return null;
                    }
                })
                .filter(Objects::nonNull));
    }

    /**
     * Converts a stream of classes to a formatted (including sorted) list of lookup results.
     *
     * @param theClasses the classes
     * @return the formatted lookup results
     */
    private static List<LookupResult> formatResults(final Stream<? extends Class<?>> theClasses){
        return theClasses
                .sorted(Comparator.comparing(Class::getName))
                .map(theClass -> new LookupResult(theClass.getName(), theClass.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getValueTableName(final ExecutionContext theExecutionContext) {
        return "AEA_SCRIPT_PKG_VIEW_SYS_ONLY";
    }

    @Override
    public String getValueColumnName(final ExecutionContext theExecutionContext) {
        return "FULLY_QUALIFIED_SCRIPT_NAME";
    }
}
