package net.atos.RtcJenkinsLinker;

import org.eclipse.core.runtime.IProgressMonitor;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerProgressMonitor implements IProgressMonitor {

    private static transient Logger logger = Logger.getLogger("rjl");

    public void beginTask(String name, int totalWork) {
        print(name);
    }

    public void done() {
    }

    public void internalWorked(double work) {
    }

    public boolean isCanceled() {
        return false;
    }

    public void setCanceled(boolean value) {
    }

    public void setTaskName(String name) {
        print(name);
    }

    public void subTask(String name) {
        print(name);
    }

    public void worked(int work) {
    }

    private void print(String name) {
        if(name != null && ! "".equals(name))
            logger.log(Level.FINE, name);
    }
}