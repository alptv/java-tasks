package ru.ifmo.rain.laptev.arrayset;

import java.util.*;


public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {
    private final ReversibleList<E> elements;
    private final Comparator<? super E> comparator;

    public ArraySet() {
        this.comparator = null;
        this.elements = new ReversibleList<>();
    }

    public ArraySet(final Collection<E> elements, Comparator<? super E> comparator) {
        this.comparator = comparator;
        if (!isSorted(elements)) {
            Set<E> sortedElements = new TreeSet<>(comparator);
            sortedElements.addAll(elements);
            this.elements = new ReversibleList<>(sortedElements);
        } else {
            this.elements = new ReversibleList<>(elements);
        }

    }

    public ArraySet(final Collection<E> elements) {
        this(elements, null);
    }

    public ArraySet(final Comparator<? super E> comparator) {
        this.comparator = comparator;
        this.elements = new ReversibleList<>();
    }

    private ArraySet(final ReversibleList<E> elements, Comparator<? super E> comparator) {
        this.comparator = comparator;
        this.elements = elements;
    }

    private boolean isSorted(final Collection<E> elements) {
        E previous = null;
        for (E current : elements) {
            if (previous != null && compare(previous, current) >= 0) {
                return false;
            }
            previous = current;
        }
        return true;
    }

    private int compare(E first, E second) {
        return comparator == null ? ((Comparable) first).compareTo(second) : comparator.compare(first, second);
    }

    private int binarySearch(E element) {
        return Collections.binarySearch(elements, element, comparator);
    }

    private int getIndex(E elementToFind, int shiftIfFound, int shiftIfNotFound) {
        int index = binarySearch(elementToFind);
        if (index >= 0) {
            index += shiftIfFound;
        } else {
            index = Math.abs(index + 1) + shiftIfNotFound;
        }
        return index;
    }

    private E findWithShifts(E element, int shiftIfFound, int shiftIfNotFound) {
        int index = getIndex(element, shiftIfFound, shiftIfNotFound);
        if (index >= size() || index < 0) {
            return null;
        }
        return elements.get(index);
    }

    @Override
    public boolean contains(Object element) {
        if (element == null) {
            return false;
        }
        try {
            return binarySearch((E) element) >= 0;
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public E lower(E e) {
        return findWithShifts(e, -1, -1);
    }

    @Override
    public E floor(E e) {
        return findWithShifts(e, 0, -1);
    }

    @Override
    public E ceiling(E e) {
        return findWithShifts(e, 0, 0);
    }

    @Override
    public E higher(E e) {
        return findWithShifts(e, 1, 0);
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException("pollFirst is unsupported");
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException("pollLast is unsupported");
    }

    @Override
    public Iterator<E> iterator() {
        return elements.iterator();
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ArraySet<>(elements.getDescendingList(), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        int left = getIndex(fromElement, fromInclusive ? 0 : 1, 0);
        int right = getIndex(toElement, toInclusive ? 0 : -1, -1);
        if (left > right || left >= size() || right < 0) {
            return new ArraySet<>(new ReversibleList<>(), comparator);
        }
        return new ArraySet<>(elements.subList(left, right + 1), comparator);
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        if (isEmpty()) {
            return new ArraySet<>(new ReversibleList<>(), comparator);
        }
        return subSet(first(), true, toElement, inclusive);
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        if (isEmpty()) {
            return new ArraySet<>(new ReversibleList<>(), comparator);
        }
        return subSet(fromElement, inclusive, last(), true);
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        if (compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException("Subset: fromElement > toElement");
        }
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public E first() {
        if (isEmpty()) {
            throw new NoSuchElementException("No first element, set is empty");
        }
        return elements.get(0);
    }

    @Override
    public E last() {
        if (isEmpty()) {
            throw new NoSuchElementException("No last element, set is empty");
        }
        return elements.get(size() - 1);
    }

    @Override
    public int size() {
        return elements.size();
    }

    private static class ReversibleList<E> extends AbstractList<E> implements RandomAccess {
        private List<E> elements;
        private boolean isReversed;

        private ReversibleList(Collection<E> collection) {
            this.elements = new ArrayList<>(collection);
        }

        private ReversibleList() {
            this.elements = new ArrayList<>();
            this.isReversed = false;
        }


        private ReversibleList(List<E> elements, boolean isReversed) {
            this.elements = elements;
            this.isReversed = isReversed;
        }

        private ReversibleList<E> getDescendingList() {
            return new ReversibleList<>(elements, !isReversed);
        }

        @Override
        public ReversibleList<E> subList(int fromIndex, int toIndex) {
            return new ReversibleList<>(elements.subList(fromIndex, toIndex), isReversed);
        }

        @Override
        public E get(int index) {
            if (isReversed) {
                return elements.get(size() - 1 - index);
            }
            return elements.get(index);
        }

        @Override
        public int size() {
            return elements.size();
        }
    }

}