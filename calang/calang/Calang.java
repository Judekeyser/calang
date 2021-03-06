package calang;

import calang.model.bricks.Paragraphs;
import calang.model.bricks.Scope;
import calang.model.operator.OperatorMap;
import calang.model.operator.WritableOperatorMap;
import calang.model.types.TypeMap;
import calang.model.types.TypedValue;
import calang.model.types.WritableTypeMap;
import calang.model.verifier.VerifiedParagraphs;
import calang.transpilation.Transpiler;
import calang.transpilation.js.JsTranspiler;

import java.util.*;

import static java.util.function.Predicate.not;

public class Calang {
    interface EphemereTranspiler extends JsTranspiler, AutoCloseable {
        @Override
        void close();
    }

    class CalangRun {
        private List<String> programLines;
        private Set<Scope.Declaration<?>> declarations;
        private Set<String> asynchronousParagraphs;
        private Map<String, Paragraphs.Paragraph> paragraphsMap;
        private String programName;

        EphemereTranspiler bindWith(String programName, List<String> programLines) {
            this.programName = programName;
            this.programLines = programLines;

            return new EphemereTranspiler() {
                @Override
                public List<String> transpile() {
                    return transpiler.transpile();
                }

                @Override
                public void close() {
                    reset();
                }

                @Override
                public String programName() {
                    return CalangRun.this.programName;
                }

                @Override
                public Paragraphs paragraphs() {
                    return CalangRun.this.paragraphs;
                }

                @Override
                public Scope scope() {
                    return CalangRun.this.scope;
                }
            };
        }

        private final Paragraphs paragraphs = new VerifiedParagraphs() {
            @Override
            public Scope scope() {
                return scope;
            }

            @Override
            public OperatorMap operatorMap() {
                return operatorsMap;
            }

            @Override
            public TypeMap typeMap() {
                return typesMap;
            }

            @Override
            public Map<String, Paragraph> paragraphsByName() {
                return paragraphsMap == null ? (paragraphsMap = VerifiedParagraphs.super.paragraphsByName()) : paragraphsMap;
            }

            @Override
            public Set<String> asynchronousParagraphs() {
                return asynchronousParagraphs == null ? (asynchronousParagraphs = VerifiedParagraphs.super.asynchronousParagraphs()): asynchronousParagraphs;
            }

            @Override
            public List<String> programSpecification() {
                assert programLines != null;
                return programLines;
            }
        };
        private final Scope scope = new Scope() {
            @Override
            public Set<Scope.Declaration<?>> declarations() {
                return declarations == null ? (declarations = Scope.super.declarations()) : declarations;
            }

            @Override
            public TypeMap typeMap() {
                return typesMap;
            }

            @Override
            public List<String> programSpecification() {
                assert programLines != null;
                return programLines;
            }
        };
        private final Transpiler<List<String>> transpiler = new JsTranspiler() {
            @Override
            public String programName() {
                assert programName != null;
                return programName;
            }

            @Override
            public Paragraphs paragraphs() {
                return paragraphs;
            }

            @Override
            public Scope scope() {
                return scope;
            }
        };

        void reset() {
            this.programName = null;
            this.programLines = null;
            this.declarations = null;
            this.asynchronousParagraphs = null;
            this.paragraphsMap = null;
        }
    }

    /************************************************ */
    final WritableTypeMap typesMap;
    final WritableOperatorMap operatorsMap;
    final CalangRun run = new CalangRun();

    protected Calang() {
        typesMap = WritableTypeMap.getMutableDefaultTypeMap();
        operatorsMap = WritableOperatorMap.getMutableDefaultOperatorMap();
    }

    public final <T extends TypedValue<T>> void addType(String typeIdentifier, Class<T> type) {
        typesMap.registerType(typeIdentifier, type);
    }

    public final <T extends TypedValue<T>, R extends TypedValue<R>> void addOperator(Class<T> clz, String operatorName,
                                                               Class<R> returnType,
                                                               List<Class<? extends TypedValue<?>>> typeChecker) {
        operatorsMap.registerOperator(clz, operatorName, returnType, typeChecker);
    }
    public final <T extends TypedValue<T>, R extends TypedValue<R>> void addOperator(Class<T> clz, String operatorName,
                                                               Class<R> returnType, Class<? extends TypedValue<?>> typeChecker) {
        operatorsMap.registerOperator(clz, operatorName, returnType, typeChecker);
    }

    /******************************************************************** */

    protected List<String> transpile(String programName, List<String> lines) {
        lines = lines.stream()
                .filter(not(String::isBlank))
                .map(str -> str.replaceAll("[\n\r]+", ""))
                .toList();
        try(var epht = run.bindWith(programName, lines)) {
            return epht.transpile();
        }
    }

}
