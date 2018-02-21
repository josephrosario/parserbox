package com.parserbox.model;


import com.parserbox.utils.NumberHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

public class SpecialAsyncTask extends Thread{
    private Log log = LogFactory.getLog(this.getClass());
    String taskId;
    String transaction;

    Date started;
    Date completed;
    boolean okToPurge;

    public enum statusType {
        PROCESSING,
        CANCELLED,
        FINAL,
        ERROR,
        PENDING
    }
    statusType status = statusType.PENDING;

    Exception exception;
    String buffer;

    public enum bufferType {
        HTML,
        JSON,
        PATH,
        STRING,
        URL
    }
    bufferType bufferType;

    SpecialAsyncTaskObservable observable = new SpecialAsyncTaskObservable();

    public SpecialAsyncTask(String transaction, String taskId){
        this.transaction = transaction;
        this.taskId = taskId;
    }

    public Date getCompleted() {
        return completed;
    }

    public void setCompleted(Date completed) {
        this.completed = completed;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    public Date getStarted() {
        return started;
    }

    public void setStarted(Date started) {
        this.started = started;
    }

    public statusType getStatus() {
        return status;
    }



    public void setStatus(statusType status) {
        this.status = status;
        observable.setChanged();
        observable.notifyObservers(this.status);
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public String getBuffer() {
        return buffer;
    }

    public void setBuffer(String buffer) {
        this.buffer = buffer;
    }

    public SpecialAsyncTask.bufferType getBufferType() {
        return bufferType;
    }

    public void setBufferType(SpecialAsyncTask.bufferType bufferType) {
        this.bufferType = bufferType;
    }

    public boolean isOkToPurge() {
        return okToPurge;
    }

    public void setOkToPurge(boolean okToPurge) {
        this.okToPurge = okToPurge;
    }

    public void addObserver(Observer observer) {
        observable.addObserver(observer);
    }

    public double getPercentageComplete() {
        List<SpecialAsyncTaskObserverAdapter> list = observable.getAsyncObjects();
        double percentageComplete = 0.0;
        if (list.size() > 0) {
            for (SpecialAsyncTaskObserverAdapter o : list) {
                percentageComplete += o.getPercentageComplete();
            }
            percentageComplete /= list.size();
        }

        return NumberHelper.roundTo2Decimals(percentageComplete);
    }

    public void start() {
        this.started = new Date();
        log.info("Special processing task: " + transaction + " started: " +
                this.started + (StringUtils.isNotBlank(taskId) ? " Task ID: " + taskId : ""));
        super.start();

    }
}
