package org.broadinstitute.gpinformatics.automation.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

/**
 * Execute a command with a timeout
 */
public class RuntimeExecutor
    {
        private static final Logger gLog = LoggerFactory.getLogger(ZiathRackScanner.class);

        private long timeout;

        public RuntimeExecutor(long timeout)
        {
            this.timeout = timeout;
        }

        /**
         * Execute specified string command in a separate process with a timeout
         * @param command a specified system command
         * @return a string of the command processes input stream
         * @throws IOException
         *         If an I/O error occurs
         * @throws TimeoutException
         *         If process does not finish within timeout
         */
        public String execute(String command) throws IOException, TimeoutException
        {
            gLog.info("RuntimeExecutor: execute() {}", command);
            Process p = Runtime.getRuntime().exec(command);

            Timer timer = new Timer();
            timer.schedule(new InterruptScheduler(Thread.currentThread()), this.timeout);

            try
            {
                p.waitFor();
            } catch (InterruptedException e)
            {
                gLog.error("RuntimeExecutor: process interrupted", e);
                p.destroy();
                throw new TimeoutException(
                        command + "did not return after " + this.timeout + " milliseconds");
            }
            finally
            {
                timer.cancel();
            }

            StringBuilder buffer = new StringBuilder();
            BufferedInputStream br = new BufferedInputStream(p.getInputStream());
            while (br.available() != 0)
            {
                buffer.append((char) br.read());
            }
            return buffer.toString().trim();

        }

        private class InterruptScheduler extends TimerTask
        {
            Thread target = null;

            public InterruptScheduler(Thread target)
            {
                this.target = target;
            }

            @Override
            public void run()
            {
                target.interrupt();
            }

        }
    }
