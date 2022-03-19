package calang.model.verifier;

import calang.model.bricks.Paragraphs;
import calang.model.bricks.Scope;
import calang.model.operator.OperatorMap;
import calang.model.types.TypeMap;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface VerifiedParagraphs extends Paragraphs {

    @Override
    List<String> programSpecification();

    Scope scope();

    OperatorMap operatorMap();

    TypeMap typeMap();

    @Override
    default Map<String, Paragraph> paragraphsByName() {
        var mapping = Paragraphs.super.paragraphsByName();

        for (var p : mapping.values()) {
            for (var tokens : p.tokens()) {
                checkComptInstruction(tokens);
                checkPerformInstruction(tokens, mapping.keySet());
            }
        }

        return mapping;
    }

    default void checkPerformInstruction(String[] tokens, Set<String> knownParagraphs) {
        new VerifiedPerformInstruction() {
            @Override
            public boolean isParagraphUnknown(String pargraphName) {
                return ! knownParagraphs.contains(pargraphName);
            }

            @Override
            public Scope scope() {
                return VerifiedParagraphs.this.scope();
            }

            @Override
            public Void performInstruction(String paragraphName, String alternativeParagraphName, String booleanValueSymbol, boolean isLoop, boolean isContraCondition) {
                return VerifiedPerformInstruction.super.performInstruction(paragraphName, alternativeParagraphName, booleanValueSymbol, isLoop, isContraCondition);
            }

            @Override
            public String[] tokens() {
                return tokens;
            }
        }.makeInstruction();
    }

    default void checkComptInstruction(String[] tokens) {
        new VerifiedComptInstruction() {
            @Override
            public Scope scope() {
                return VerifiedParagraphs.this.scope();
            }

            @Override
            public OperatorMap operators() {
                return VerifiedParagraphs.this.operatorMap();
            }

            @Override
            public String[] tokens() {
                return tokens;
            }
        }.makeInstruction();
    }
}
