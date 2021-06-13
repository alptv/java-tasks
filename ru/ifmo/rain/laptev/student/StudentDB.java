package ru.ifmo.rain.laptev.student;

import info.kgeorgiy.java.advanced.student.AdvancedStudentGroupQuery;
import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.Student;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

public class StudentDB implements AdvancedStudentGroupQuery {
    private static final String ZERO_STRING = "";
    private static final Comparator<Student> COMPARATOR_BY_NAME = Comparator
            .comparing(Student::getLastName)
            .thenComparing(Student::getFirstName)
            .thenComparing(Student::getId);

    private String getFullName(final Student student) {
        return student.getFirstName() + " " + student.getLastName();
    }

    private <E> Map<E, List<Student>> groupStudentByAttribute(final Collection<Student> students , final Function<Student, E> function) {
        return students.stream().collect(Collectors.groupingBy(function));
    }

    private List<Group> getSortedGroups(final Collection<Student> students, final Comparator<Student> comparator) {
        return groupStudentByAttribute(students, Student::getGroup).entrySet().stream()
                .map(entry -> {
                    entry.getValue().sort(comparator);
                    return new Group(entry.getKey(), entry.getValue());
                })
                .sorted(Comparator.comparing(Group::getName))
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> getGroupsByName(final Collection<Student> students) {
        return getSortedGroups(students, COMPARATOR_BY_NAME);
    }

    @Override
    public List<Group> getGroupsById(final Collection<Student> students) {
      return getSortedGroups(students, Student::compareTo);
    }

    private<T> String getMaxGroupByComparator(final Collection<Student> students, final Comparator<AbstractMap.SimpleEntry<Group, T>> comparator, Function<Group, T> mapper) {
        return getGroupsByName(students).stream().map(group -> new AbstractMap.SimpleEntry<>(group, mapper.apply(group)))
                .max(comparator.thenComparing((AbstractMap.SimpleEntry<Group, T> entry) -> entry.getKey().getName(), Comparator.reverseOrder()))
                .map(entry -> entry.getKey().getName()).orElse(ZERO_STRING);
    }

    @Override
    public String getLargestGroup(final Collection<Student> students) {
       return getMaxGroupByComparator(students, Comparator.comparingInt(AbstractMap.SimpleEntry::getValue), (group -> group.getStudents().size()));
    }

    @Override
    public String getLargestGroupFirstName(final Collection<Student> students) {
        return getMaxGroupByComparator(students, Comparator.comparingLong(AbstractMap.SimpleEntry::getValue),
                group -> group.getStudents()
                        .stream()
                        .map(Student::getFirstName)
                        .distinct()
                        .count());
    }


    private List<String> getAttribute(final List<Student> students, final Function<Student, String> function) {
        return students.stream().map(function).collect(Collectors.toList());
    }

    @Override
    public List<String> getFirstNames(final List<Student> students) {
        return getAttribute(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(final List<Student> students) {
        return getAttribute(students, Student::getLastName);
    }

    @Override
    public List<String> getGroups(final List<Student> students) {
        return getAttribute(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(final List<Student> students) {
        return getAttribute(students, this::getFullName);
    }

    @Override
    public Set<String> getDistinctFirstNames(final List<Student> students) {
        return new TreeSet<>(getFirstNames(students));
    }

    @Override
    public String getMinStudentFirstName(final List<Student> students) {
        return students.stream().min(Student::compareTo).map(Student::getFirstName).orElse(ZERO_STRING);
    }


    private List<Student> sortByComparator(final Collection<Student> students, final Comparator<? super Student> comparator) {
        return students.stream().sorted(comparator).collect(Collectors.toList());
    }

    @Override
    public List<Student> sortStudentsById(final Collection<Student> students) {
        return sortByComparator(students, Student::compareTo);
    }

    @Override
    public List<Student> sortStudentsByName(final Collection<Student> students) {
        return sortByComparator(students, COMPARATOR_BY_NAME);
    }

    private List<Student> findStudentsByPredicate(final Collection<Student> students, final Predicate<Student>  predicate) {
        return sortStudentsByName(students).stream().filter(predicate).collect(Collectors.toList());
    }

    @Override
    public List<Student> findStudentsByFirstName(final Collection<Student> students, final String name) {
        return findStudentsByPredicate(students, student -> student.getFirstName().equals(name));
    }

    @Override
    public List<Student> findStudentsByLastName(final Collection<Student> students, final String name) {
        return findStudentsByPredicate(students, student -> student.getLastName().equals(name));
    }

    @Override
    public List<Student> findStudentsByGroup(final Collection<Student> students, final String group) {
        return findStudentsByPredicate(students, student -> student.getGroup().equals(group));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(final Collection<Student> students, final String group) {
        return findStudentsByGroup(students, group)
                .stream()
                .collect(Collectors
                .toMap(Student::getLastName, Student::getFirstName , (x, y) -> x.compareTo(y) < 0 ? x : y));
    }


    private long getDistinctGroupCount(List<Student> students) {
        return students.stream().map(Student::getGroup).distinct().count();
    }

    public String getMostPopularName(Collection<Student> students) {
        return groupStudentByAttribute(students, this::getFullName).entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), getDistinctGroupCount(entry.getValue())))
                .max(Comparator.comparingLong((ToLongFunction<AbstractMap.SimpleEntry<String, Long>>) AbstractMap.SimpleEntry::getValue)
                        .thenComparing(AbstractMap.SimpleEntry::getKey))
                .map(Map.Entry::getKey).orElse(ZERO_STRING);
    }


    private List<String> getWithIndices(final Student[] students, int[] indices, final Function<Student, String> function) {
        return Arrays.stream(indices).mapToObj(i -> students[i]).map(function).collect(Collectors.toList());
    }

    @Override
    public List<String> getFirstNames(final Collection<Student> students, int[] indices) {
        return getWithIndices(students.toArray(Student[]::new), indices, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(Collection<Student> students, int[] indices) {
        return getWithIndices(students.toArray(Student[]::new), indices, Student::getLastName);
    }

    @Override
    public List<String> getGroups(Collection<Student> students, int[] indices) {
        return getWithIndices(students.toArray(Student[]::new), indices, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(Collection<Student> students, int[] indices) {
        return getWithIndices(students.toArray(Student[]::new),
                indices, this::getFullName);
    }
}
