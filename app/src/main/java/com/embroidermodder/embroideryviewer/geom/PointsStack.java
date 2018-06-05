package com.embroidermodder.embroideryviewer.geom;

import java.util.ArrayList;

/**
 * Created by Tat on 5/25/2015.
 *
 * Points Stack is a stack structure for points.
 * @param <E>
 */

public abstract class PointsStack<E extends Points> {
    protected ArrayList<E> stack = new ArrayList<>();
    public E current;

    public PointsStack() {
        newPath();
    }

    public PointsStack(PointsStack<E> path) {
        stack = new ArrayList<>(path.stack.size());
        for (E p : path.stack) {
            add(newItem(p));
        }
    }

    abstract protected E newItem(E v);
    abstract protected E getBlank();

    protected boolean isEmpty(E v) {
        return (current.size() == 0);
    }

    protected void validate() {
    }

    public ArrayList<E> asStack() {
        validate();
        return stack;
    }

    public void newPath() {
        if ((current == null) || (!isEmpty(current))) {
            current = getBlank();
            stack.add(current);
        }
    }

    public void add(E add) {
        if (current.size() == 0) {
            stack.remove(current);
        }
        current = add;
        stack.add(add);
    }

    public void clear() {
        stack.clear();
        stack.add(current);
    }
}
