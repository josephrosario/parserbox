package com.parserbox.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class SpecialAsyncTaskObservable extends Observable{
    List<SpecialAsyncTaskObserverAdapter> asyncObjects = new ArrayList<>();
    public void setChanged() {
        super.setChanged();
    }

    @Override
    public synchronized void addObserver(Observer o) {
        if (o instanceof SpecialAsyncTaskObserverAdapter ) {
            if (!asyncObjects.contains(o)) {
                asyncObjects.add((SpecialAsyncTaskObserverAdapter)o);
            }
        }
        super.addObserver(o);
    }

    public List<SpecialAsyncTaskObserverAdapter> getAsyncObjects() {
        return asyncObjects;
    }
}
