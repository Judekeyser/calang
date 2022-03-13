package calang.model.algebra;

import calang.Calang;
import calang.model.Paragraph;
import calang.model.Program;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public interface GraphLinks {

    interface CallPreInstruction extends GraphLinks {}

    interface PerformPreInstruction extends GraphLinks {

        List<String> dependentParagraphs();

    }

    static Set<Paragraph<Calang.PreInstruction>> asynchronousParagraphs(Program<Calang.PreInstruction> self) {
        class Helper {
            boolean hasCallInstruction(Paragraph<Calang.PreInstruction> p) {
                return p.instructions().stream().anyMatch(GraphLinks.CallPreInstruction.class::isInstance);
            }

            Predicate<Paragraph<Calang.PreInstruction>> makesCallTo(Paragraph<Calang.PreInstruction> p) {
                return __ -> __.instructions().stream()
                        .filter(GraphLinks.PerformPreInstruction.class::isInstance)
                        .map(GraphLinks.PerformPreInstruction.class::cast)
                        .map(GraphLinks.PerformPreInstruction::dependentParagraphs)
                        .flatMap(List::stream)
                        .map(self::paragraph)
                        .anyMatch(p::equals);
            }

            List<Paragraph<Calang.PreInstruction>> allParagraphs() {
                return self.paragraphNames().stream().map(self::paragraph).toList();
            }
        }
        var helper = new Helper();

        var toExplore = new HashSet<Paragraph<Calang.PreInstruction>>();
        var asyncParagraphs = new HashSet<Paragraph<Calang.PreInstruction>>();

        // Initialize with tails
        for (var p: helper.allParagraphs())
            (helper.hasCallInstruction(p) ? asyncParagraphs : toExplore).add(p);

        for (;;) {
            assert asyncParagraphs.stream().noneMatch(toExplore::contains);
            var queue = new ArrayDeque<Paragraph<Calang.PreInstruction>>();
            for (var asyncPar : asyncParagraphs)
                toExplore.stream()
                        .filter(helper.makesCallTo(asyncPar))
                        .forEach(queue::add);
            if (queue.isEmpty()) break;
            else {
                asyncParagraphs.addAll(queue);
                toExplore.removeAll(queue);
            }
        } assert asyncParagraphs.stream().noneMatch(toExplore::contains);

        return asyncParagraphs;
    }

}
