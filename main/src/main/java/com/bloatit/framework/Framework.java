//
// Copyright (c) 2011 Linkeos.
//
// This file is part of Elveos.org.
// Elveos.org is free software: you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the
// Free Software Foundation, either version 3 of the License, or (at your
// option) any later version.
//
// Elveos.org is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
// more details.
// You should have received a copy of the GNU General Public License along
// with Elveos.org. If not, see http://www.gnu.org/licenses/.
//
package com.bloatit.framework;

import java.net.BindException;

import com.bloatit.common.CommonConfiguration;
import com.bloatit.common.Log;
import com.bloatit.framework.exceptions.highlevel.ExternalErrorException;
import com.bloatit.framework.feedbackworker.FeedBackWorker;
import com.bloatit.framework.feedbackworker.FeedbackServer;
import com.bloatit.framework.mailsender.MailServer;
import com.bloatit.framework.model.Model;
import com.bloatit.framework.model.ModelAccessor;
import com.bloatit.framework.utils.IpLocator;
import com.bloatit.framework.webprocessor.context.SessionCleanerTask;
import com.bloatit.framework.webprocessor.context.SessionManager;
import com.bloatit.framework.xcgiserver.XcgiProcessor;
import com.bloatit.framework.xcgiserver.XcgiServer;
import com.bloatit.model.Reporting;

/**
 * This class represent the whole framework.
 */
public class Framework {

    private final Model model;
    private final XcgiServer scgiServer;
    private final MailServer mailServer;
    private final FeedbackServer feedbackServer;

    public Framework(final Model model) {
        this.model = model;
        this.scgiServer = new XcgiServer();
        this.mailServer = MailServer.getInstance();
        this.feedbackServer = FeedbackServer.getInstance();
    }

    public void addProcessor(final XcgiProcessor processor) {
        scgiServer.addProcessor(processor);
    }

    public void addWorker(final FeedBackWorker<?> worker) {
        feedbackServer.addWorker(worker);
    }

    public boolean initialize() {
        try {
            CommonConfiguration.load();
            FrameworkConfiguration.load();
            LocalesConfiguration.load();
            IpLocator.initialize();

            mailServer.initialize();
            scgiServer.initialize();
            feedbackServer.initialize();

            ModelAccessor.initialize(model);
            new SessionCleanerTask();
            Reporting.reporter.reportServerStart();
        } catch (final ExternalErrorException e) {
            Log.framework().fatal("Error loading configuration file", e);
            return false;
        } catch (final BindException e) {
            Log.framework().fatal("Are you sure you have killed previous instance? ", e);
            return false;
        } catch (final RuntimeException e) {
            Log.framework().fatal("Unknown RuntimeException", e);
            return false;
        } catch (final Exception e) {
            Log.framework().fatal("Unknown Exception", e);
            return false;
        } catch (final Error e) {
            Log.framework().fatal("Unknown error", e);
            return false;
        } finally {
            Runtime.getRuntime().addShutdownHook(new ShutdownHook(this));
        }

        return true;
    }

    public void run() {
        try {
            mailServer.start();
            feedbackServer.start();
            scgiServer.start();

        } catch (final RuntimeException e) {
            Log.framework().fatal("Unknown RuntimeException", e);
        } catch (final Exception e) {
            Log.framework().fatal("Unknown Exception", e);
        } catch (final Error e) {
            Log.framework().fatal("Unknown error", e);
        }
    }

    private void shutdown() {
        Reporting.reporter.reportServerStop();
        Reporting.reporter.report();
        SessionManager.saveSessions();
        scgiServer.stop();
        MailServer.getInstance().quickStop();
        ModelAccessor.shutdown();
    }

    private static final class ShutdownHook extends Thread {
        private final Framework framework;

        public ShutdownHook(final Framework framework) {
            super();
            this.framework = framework;
        }

        @Override
        public void run() {
            framework.shutdown();
        }
    }

}
