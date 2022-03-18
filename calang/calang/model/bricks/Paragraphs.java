package calang.model.bricks;

import calang.model.bricks.instructions.PerformInstructionMk;

import java.util.*;
import java.util.stream.Stream;

import static calang.rejections.Rejections.*;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.function.Predicate.not;

public interface Paragraphs {

    abstract class Paragraph {
        private final int startInc, endExc;
        Paragraph(int startInc, int endExc) {
            this.startInc = startInc;
            this.endExc = endExc;
        }

        abstract protected List<String> programSpecification();

        public List<String[]> tokens() {
            return programSpecification().stream()
                    .skip(startInc)
                    .limit(endExc - startInc)
                    .filter(not(String::isBlank))
                    .map(this::split)
                    .toList();
        }

        private String[] split(String line) {
            return line.trim().split("\s+");
        }
    }

    List<String> programSpecification();

    default Map<String, Paragraph> paragraphsByName() {
        class P extends Paragraph {
            P(int startInc, int endExc) {
                super(startInc, endExc);
            }
            @Override
            protected List<String> programSpecification() {
                return Paragraphs.this.programSpecification();
            }
        }
        var paragraphs = new HashMap<String, P>();
        int lineCounter = -1;
        consumeLine: for (var it = programSpecification().iterator(); it.hasNext();) {
            String line = it.next(); lineCounter++;
            point: for(;;)
            {
                if (line.isBlank() || line.startsWith("DECLARE")) continue consumeLine;

                if (!line.endsWith(".") || line.length() <= 1 || line.startsWith(" "))
                    throw MALFORMED_PARAGRAPH_TITLE.error();
                if (! it.hasNext())
                    throw EMPTY_PARAGRAPH.error(lineCounter);

                assert programSpecification().size() >= lineCounter + 2;
                lineCounter += 1;
                int endLine = lineCounter;

                String paragraphName = line.substring(0, line.length() - 1);

                try {
                    while(it.hasNext()) {
                        line = it.next(); endLine++;
                        if (!line.isBlank() && !line.startsWith("  ")) {
                            endLine -= 1;
                            continue point;
                        }
                    } break;
                } finally {
                    assert endLine > lineCounter;
                    assert programSpecification().size() >= endLine;

                    paragraphs.put(paragraphName, new P(lineCounter, endLine));
                    lineCounter = endLine;
                }
            }
        }
        return unmodifiableMap(paragraphs);
    }

    default String headParagraph() {
        for (var paragraph : paragraphsByName().keySet())
            if(callersOf(paragraph).isEmpty())
                return paragraph;
        throw NO_MAIN_PARAGRAPH_FOUND.error();
    }

    default Set<String> callersOf(String _paragraph) {
        var calledBy = new HashSet<String>();
        for (var p : paragraphsByName().entrySet()) {
            for (var tokens : p.getValue().tokens()) {
                class PerformInstruction implements PerformInstructionMk<Boolean> {
                    @Override
                    public Boolean performInstruction(String paragraphName, String alternativeParagraphName, String booleanValueSymbol, boolean isLoop, boolean isContraCondition) {
                        return Stream.of(paragraphName, alternativeParagraphName).filter(Objects::nonNull)
                                .anyMatch(_paragraph::equals);
                    }

                    @Override
                    public String[] tokens() {
                        return tokens;
                    }
                }
                if (Boolean.TRUE.equals(new PerformInstruction().makeInstruction()))
                    calledBy.add(p.getKey());
            }
        }
        return unmodifiableSet(calledBy);
    }
    default boolean isParagraphAsynchronous(String paragraph) {
        return asynchronousParagraphs().contains(paragraph);
    }

    default Set<String> asynchronousParagraphs() {
        var toExplore = new HashSet<String>();
        var asyncParagraphs = new HashSet<String>();

        var lookup = paragraphsByName();

        // Initialize with tails
        for (var p: lookup.entrySet()) {
            var paragraph = p.getValue();
            for(var i: paragraph.tokens())
                (i[0].equals("CALL") ? asyncParagraphs : toExplore).add(p.getKey());
        }

        for (;;) {
            assert asyncParagraphs.stream().noneMatch(toExplore::contains);
            var queue = new ArrayDeque<String>();
            for (var asyncPar : asyncParagraphs)
                queue.addAll(callersOf(asyncPar).stream()
                        .filter(toExplore::contains)
                        .filter(not(asyncPar::equals))
                        .toList()
                );
            assert toExplore.containsAll(queue);
            if (queue.isEmpty()) break;
            else {
                asyncParagraphs.addAll(queue);
                toExplore.removeAll(queue);
            }
        } assert asyncParagraphs.stream().noneMatch(toExplore::contains);

        return unmodifiableSet(asyncParagraphs);
    }

}
