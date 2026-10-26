package com.appointease;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;

/** JDK bytecode analysis includes wildcard/qualified references and nested classes. */
class ArchitectureTest {
    private static final String ROOT = "com.appointease.";
    private record Dependency(String from, String to) { }

    @Test void compiledDependenciesRespectLayersAndPresentationBoundary() throws Exception {
        for (Dependency dependency : compiledDependencies()) requireAllowed(dependency.from(), dependency.to());
    }

    @Test void productionPackagesHaveNoCycles() throws Exception {
        Map<String, Set<String>> graph = packageGraph(compiledDependencies());
        StringBuilder report = new StringBuilder();
        graph.forEach((from, targets) -> targets.forEach(to -> report.append(from).append(" -> ").append(to).append('\n')));
        Files.writeString(Path.of("target", "architecture-packages.txt"), report);
        assertTrue(cycle(graph).isEmpty(), () -> "Package cycle: " + cycle(graph) + "\n" + report);
    }

    @Test void cycleCheckDetectsDirectIndirectAndDisconnectedCycles() {
        // A diamond, same-package reference and isolated node are legal.
        var edges = new ArrayList<>(List.of(new Dependency("a.One", "b.Two"),
                new Dependency("a.One", "c.Three"), new Dependency("b.Two", "d.Four"),
                new Dependency("c.Three", "d.Four"), new Dependency("d.Four", "d.Other"),
                new Dependency("isolated.One", "java.lang.Object")));
        assertTrue(cycle(packageGraph(edges)).isEmpty());
        edges.add(new Dependency("d.Four", "a.One"));
        assertFalse(cycle(packageGraph(edges)).isEmpty(), "Indirect cycle must fail");
        assertEquals(List.of("x", "y", "x"), cycle(packageGraph(List.of(
                new Dependency("a.One", "b.Two"), new Dependency("x.One", "y.Two"),
                new Dependency("y.Two", "x.One")))), "Disconnected reciprocal cycle must fail");
    }

    @Test void declaredImportsRespectLayersEvenWhenUnused() throws Exception {
        Pattern declaration = Pattern.compile("(?m)^package\\s+([\\w.]+);");
        Pattern imports = Pattern.compile("(?m)^import\\s+(?:static\\s+)?([\\w.*]+);");
        try (var files = Files.walk(Path.of("src/main/java"))) {
            for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                String source = Files.readString(file);
                var declared = declaration.matcher(source);
                assertTrue(declared.find(), "Missing package: " + file);
                String owner = declared.group(1) + "." + file.getFileName().toString().replace(".java", "");
                var imported = imports.matcher(source);
                while (imported.find()) {
                    String target = imported.group(1);
                    requireAllowed(owner, target);
                    if (layer(owner).equals("presentation")) {
                        assertNotEquals(ROOT + "application.port.*", target, file.toString());
                        assertNotEquals(ROOT + "application.*", target, file.toString());
                    }
                }
            }
        }
    }

    private static List<Dependency> compiledDependencies() throws Exception {
        Path classes = Path.of(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        String executable = System.getProperty("os.name").startsWith("Windows") ? "jdeps.exe" : "jdeps";
        Path report = Path.of("target", "architecture-jdeps.txt");
        Process process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", executable).toString(),
                "-verbose:class", "-filter:none", classes.toString())
                .redirectErrorStream(true).redirectOutput(report.toFile()).start();
        try {
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "jdeps timed out");
            String output = Files.readString(report);
            assertEquals(0, process.exitValue(), output);
            List<Dependency> dependencies = new ArrayList<>();
            Set<String> inspected = new HashSet<>();
            for (String line : output.lines().toList()) {
                String[] parts = line.strip().split("\\s+");
                if (parts.length < 3 || !parts[1].equals("->") || !parts[0].startsWith(ROOT)) continue;
                dependencies.add(new Dependency(parts[0], parts[2]));
                inspected.add(parts[0]);
            }
            assertFalse(dependencies.isEmpty(), "No project dependencies inspected");
            try (var files = Files.walk(classes)) {
                for (Path file : files.filter(p -> p.toString().endsWith(".class")).toList()) {
                    String name = classes.relativize(file).toString().replace('\\', '.').replace('/', '.');
                    assertTrue(inspected.contains(name.substring(0, name.length() - 6)),
                            "Class omitted by dependency analysis: " + name);
                }
            }
            return dependencies;
        } finally {
            if (process.isAlive()) process.destroyForcibly();
        }
    }

    private static void requireAllowed(String owner, String target) {
        String evidence = owner + " -> " + target;
        if (!target.startsWith(ROOT)) {
            assertTrue(target.startsWith("java."), evidence);
            return;
        }
        String from = layer(owner);
        Set<String> allowed = switch (from) {
            case "domain" -> Set.of("domain");
            case "application" -> Set.of("application", "domain");
            case "infrastructure" -> Set.of("infrastructure", "application", "domain");
            case "presentation" -> Set.of("presentation", "application", "domain");
            case "Main" -> Set.of("Main", "domain", "application", "infrastructure", "presentation");
            default -> throw new AssertionError("Unknown production layer: " + from);
        };
        assertTrue(allowed.contains(layer(target)), evidence);
        if (from.equals("presentation")) {
            assertFalse(target.startsWith(ROOT + "application.port.AppointmentRepository"), evidence);
            assertFalse(target.startsWith(ROOT + "application.CommittedEventDispatcher"), evidence);
        }
    }

    private static Map<String, Set<String>> packageGraph(List<Dependency> dependencies) {
        Map<String, Set<String>> graph = new TreeMap<>();
        for (Dependency dependency : dependencies) {
            String from = packageName(dependency.from());
            graph.computeIfAbsent(from, ignored -> new TreeSet<>());
            // JDK packages are external leaves; no production package is excluded.
            if (dependency.to().startsWith("java.")) continue;
            String to = packageName(dependency.to());
            graph.computeIfAbsent(to, ignored -> new TreeSet<>());
            if (!from.equals(to)) graph.get(from).add(to);
        }
        return graph;
    }

    private static String packageName(String className) {
        return className.substring(0, className.lastIndexOf('.'));
    }

    private static List<String> cycle(Map<String, Set<String>> graph) {
        Set<String> done = new HashSet<>();
        for (String node : graph.keySet()) {
            List<String> found = visit(node, graph, done, new ArrayList<>());
            if (!found.isEmpty()) return found;
        }
        return List.of();
    }

    private static List<String> visit(String node, Map<String, Set<String>> graph,
            Set<String> done, List<String> active) {
        int index = active.indexOf(node);
        if (index >= 0) {
            List<String> found = new ArrayList<>(active.subList(index, active.size()));
            found.add(node);
            return found;
        }
        if (done.contains(node)) return List.of();
        active.add(node);
        for (String next : graph.get(node)) {
            List<String> found = visit(next, graph, done, active);
            if (!found.isEmpty()) return found;
        }
        active.removeLast();
        done.add(node);
        return List.of();
    }

    @Test void productionClassesHaveNoMutableStaticFields() throws Exception {
        Path classes = Path.of(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        try (var files = Files.walk(classes.resolve("com/appointease"))) {
            for (Path file : files.filter(p -> p.toString().endsWith(".class")).toList()) {
                String name = classes.relativize(file).toString().replace('\\', '.').replace('/', '.');
                Class<?> type = Class.forName(name.substring(0, name.length() - 6), false, Main.class.getClassLoader());
                for (var field : type.getDeclaredFields()) {
                    if (!field.isSynthetic() && Modifier.isStatic(field.getModifiers())) {
                        assertTrue(Modifier.isFinal(field.getModifiers()), type.getName() + "." + field.getName());
                        assertTrue(field.getType().isPrimitive() || field.getType() == String.class || field.isEnumConstant(),
                                "Review potentially mutable static object: " + field);
                    }
                }
            }
        }
    }

    private static String layer(String name) {
        return name.substring(ROOT.length()).split("[.$]", 2)[0];
    }
}
