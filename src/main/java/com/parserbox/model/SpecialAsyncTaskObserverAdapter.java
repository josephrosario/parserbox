package com.parserbox.model;

import java.util.Observable;
import java.util.Observer;

public abstract class SpecialAsyncTaskObserverAdapter implements Observer {

    public SpecialAsyncTaskObserverAdapter() {}

    /**
     * Called by parent observable
     *
     * @param observable
     * @param o
     */
    @Override
    public void update(Observable observable, Object o) {
        if (o != null && o instanceof SpecialAsyncTask.statusType) {
            this.setStatus((SpecialAsyncTask.statusType)o);
            if (o.equals(SpecialAsyncTask.statusType.CANCELLED)) {
                this.setCancelled();
            }
        }
    }

    public abstract double getPercentageComplete();
    public abstract void setCancelled();
    public abstract void setStatus(SpecialAsyncTask.statusType statusType);

}
