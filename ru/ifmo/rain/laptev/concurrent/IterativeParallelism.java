package ru.ifmo.rain.laptev.concurrent;

import info.kgeorgiy.java.advanced.concurrent.AdvancedIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class IterativeParallelism implements AdvancedIP {
    private final ParallelMapper parallelMapper;

    public IterativeParallelism() {
        parallelMapper = null;
    }

    public IterativeParallelism(final ParallelMapper parallelMapper) {
        this.parallelMapper = parallelMapper;
    }

    private static int calculateRight(final int left, final int taskSize, final int remainder) {
        return left + taskSize + (remainder > 0 ? 1 : 0);
    }

    private static int calculateLeft(final int index, final int taskSize, final int remainder) {
        if (index > remainder) {
            return taskSize * index + remainder;
        }
        return taskSize * index + index;
    }

    private  <R> List<R> getTasks(final int argsSize, final int tasksCount, final TaskCreator<R> creator) {
        final int taskSize = argsSize / tasksCount;
        final int remainder = argsSize % tasksCount;

        List<R> tasks = new ArrayList<>();
        for (int i = 0; i < tasksCount; i++) {
            final int left = calculateLeft(i, taskSize, remainder);
            final int right = calculateRight(left, taskSize, remainder - i);
            tasks.add(creator.create(left, right, i));
        }
        return tasks;
    }

    private <R, T> R solve(int threadsCount, final List<T> args,
                           final Function<Stream<T>, R> solver,
                           final Function<Stream<R>, R> finisher) throws InterruptedException {

        threadsCount = Math.max(1, Math.min(threadsCount, args.size()));

        if (parallelMapper != null) {
            TaskCreator<List<T>> sublistCreator = (left, right, index) -> args.subList(left, right);
            List<List<T>> tasks = getTasks(args.size(), threadsCount, sublistCreator);
            return finisher.apply(parallelMapper.map(task -> solver.apply(task.stream()), tasks).stream());
        }

        final List<R> solutions = new ArrayList<>(Collections.nCopies(threadsCount, null));

        TaskCreator<Thread> threadCreator = (left, right, index)  -> new Thread(() ->
                solutions.set(index, solver.apply(args.subList(left, right).stream())));

        final List<Thread> threads = getTasks(args.size(), threadsCount,threadCreator);

        threads.forEach(Thread::start);
        joinThreads(threads);

        return finisher.apply(solutions.stream());
    }

    private void joinThreads(List<Thread> threads) throws InterruptedException {
        InterruptedException interruptedException = null;
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                if (interruptedException == null) {
                    interruptedException = e;
                } else {
                    interruptedException.addSuppressed(e);
                }
            }
        }
        if (interruptedException != null) {
            throw interruptedException;
        }
    }

    @Override
    public <T> T maximum(final int threadsCount, final List<? extends T> args, final Comparator<? super T> comparator) throws InterruptedException {
        if (args.isEmpty()) {
            throw new NoSuchElementException();
        }
        return solve(threadsCount, args,stream -> stream.max(comparator).get(), stream -> stream.max(comparator).get());
    }

    @Override
    public <T> T minimum(final int threads, final List<? extends T> args, final Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, args, comparator.reversed());
    }

    @Override
    public <T> boolean all(final int threadsCount, final List<? extends T> args, final Predicate<? super T> predicate) throws InterruptedException {
        return solve(threadsCount, args, stream -> stream.allMatch(predicate), stream -> stream.allMatch(Boolean::booleanValue));
    }

    @Override
    public <T> boolean any(final int threads, final List<? extends T> args, final Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, args, predicate.negate());
    }

    @Override
    public String join(final int threadsCount, final List<?> args) throws InterruptedException {
        return solve(threadsCount, args, stream -> stream.map(Objects::toString).collect(Collectors.joining()),
                stream -> stream.collect(Collectors.joining()));
    }

    @Override
    public <T> List<T> filter(final int threadsCount, final List<? extends T> args, final Predicate<? super T> predicate) throws InterruptedException {
        return solve(threadsCount, args, stream -> stream.filter(predicate).collect(Collectors.toList()),
                stream -> stream.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    @Override
    public <T, U> List<U> map(final int threadsCount, final List<? extends T> args, final Function<? super T, ? extends U> function) throws InterruptedException {
        return solve(threadsCount, args, stream -> stream.map(function).collect(Collectors.toList()),
                stream -> stream.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    @Override
    public <T> T reduce(final int threadsCount, final List<T> args, final Monoid<T> monoid) throws InterruptedException {
        return solve(threadsCount, args, stream -> stream.reduce(monoid.getIdentity(), monoid.getOperator()),
                stream -> stream.reduce(monoid.getIdentity(), monoid.getOperator()));
    }

    @Override
    public <T, R> R mapReduce(final int threadsCount, final List<T> args, final Function<T, R> lift, final Monoid<R> monoid) throws InterruptedException {
        return solve(threadsCount, args, stream -> stream.map(lift).reduce(monoid.getIdentity(), monoid.getOperator()),
                stream -> stream.reduce(monoid.getIdentity(), monoid.getOperator()));
    }


    private interface TaskCreator<R> {
        R create(int left, int right, int index);
    }



 
}
