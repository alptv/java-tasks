package ru.ifmo.rain.laptev.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;


public class ParallelMapperImpl implements ParallelMapper {
    private final List<Thread> threads;
    private final Queue<Runnable> tasksQueue;

    public ParallelMapperImpl(final int threadCount) {
        threads = new ArrayList<>();
        tasksQueue = new LinkedList<>();
        IntStream.range(0, threadCount).forEach(i -> threads.add(new Thread(getTask())));
        threads.forEach(Thread::start);
    }

    private Runnable getTask() {
        return () -> {
            try {
                while (!Thread.interrupted()) {
                    Runnable task;
                    synchronized (tasksQueue) {
                        while (tasksQueue.isEmpty()) {
                            tasksQueue.wait();
                        }
                        task = tasksQueue.poll();
                        tasksQueue.notifyAll();
                    }
                    task.run();
                }
            } catch (InterruptedException ignored) {}
        };
    }

    @Override
    public <T, R> List<R> map(final Function<? super T, ? extends R> mapper, final List<? extends T> args) throws InterruptedException {
        TasksGroup<T, R> tasksGroup = new TasksGroup<>(args, mapper);
        return tasksGroup.solve();
    }

    @Override
    public void close() {
        threads.forEach(Thread::interrupt);
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException ignored) {
        }
    }

    private class TasksGroup<T, R> {
        private final Function<? super T, ? extends R> mapper;
        private final List<R> solutions;
        private int taskCount;

        private TasksGroup(final List<? extends T> args, final Function<? super T, ? extends R> mapper) {
            this.taskCount = args.size();
            this.mapper = mapper;
            this.solutions = new ArrayList<>(Collections.nCopies(taskCount, null));
            addTasks(args);
        }

        private List<R> solve() throws InterruptedException {
            synchronized (this) {
                while (taskCount > 0) {
                    wait();
                }
                return solutions;
            }
        }

        private void addTasks(final List<? extends T> args) {
            synchronized (tasksQueue) {
                IntStream.range(0, args.size()).forEach(i -> tasksQueue.add(() -> {
                    R solution = mapper.apply(args.get(i));
                    synchronized (this) {
                        solutions.set(i, solution);
                        taskCount--;
                        if (taskCount == 0) {
                            notify();
                        }
                    }
                }));
                tasksQueue.notifyAll();
            }
        }
    }
}


