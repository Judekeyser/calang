package calang.rejections;

public enum Rejections {
    NO_PARAGRAPH_FOUND("There is no paragraph in the program.. That's unfortunate"),
    UNDEFINED_PARAGRAPH("Unresolved paragrah named %s"),
    UNMAPPABLE_INPUT("Provided input field named %s cannot be mapped on program inputs"),
    UNMAPPED_INPUT("Unable to run the program as not all inputs are given; missing at least %s"),
    UNKNOWN_VARIABLE("The requested scope does not contain any reference to %s symbol"),
    UNSUPPORTED_OPERATOR("Unsupported operator %s on %s"),
    UNAPPLICABLE_OPERATOR("Operator %s on %s cannot be applied on types %s"),
    UNSTORABLE_OPERATOR_RESULT("Result of operator %s on %s cannot be stored in variable of type %s"),
    UNRECOGNIZED_INSTRUCTION_TOKEN("Unrecognized instruction token %s"),
    UNRECOGNIZED_PERFORM_DECORATOR("Unrecognized <PERFORM> instruction decorator %s"),
    MALFORMED_PERFORM_INSTRUCTION("Malformed expression PERFORM |%s|"),
    MALFORMED_PRINT_INSTRUCTION("Malformed expression PRINT |%s|"),
    MALFORMED_STORE_INSTRUCTION("Malformed expression STORE |%s|"),
    MALFORMED_COMPT_INSTRUCTION("Malformed expression COMPT |%s|"),
    MALFORMED_CALL_INSTRUCTION("Malformed expression CALL: %s"),
    UNSUPPORTED_FROM_BYTES_CONVERSION("Unsupported from-bytes conversion on %s"),
    UNSUPPORTED_FROM_OBJECT_CONVERSION("Unsupported from-object conversion on %s for source |%s|"),
    BOOLEAN_FLAG_IS_NOT_BOOLEAN("Boolean flag %s is not fed with boolean typed, got %s instead"),
    NON_TRANSPILED_INSTRUCTION("Unable to find a way to transpile instruction %s"),
    NON_TRANSPILED_PROGRAM("Unable to find a way to transpile program"),
    NON_TRANSPILED_TYPE("Unable to find a way to transpile type %s"),
    CALANG_BASEPATH_IS_MALFORMED("Malformed Calang base path %s")
    ;
    private final String messageTemplate;

    Rejections(String tpl) {
        messageTemplate = tpl;
    }

    public AssertionError error(Object... args) {
        return new AssertionError(messageTemplate.formatted(args));
    }
}
