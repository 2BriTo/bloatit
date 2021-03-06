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
package com.bloatit.framework.utils.datetime;

import static com.bloatit.framework.webprocessor.context.Context.trn;

import java.util.Date;

import com.bloatit.framework.utils.i18n.DateLocale.FormatStyle;
import com.bloatit.framework.webprocessor.context.Context;

/**
 * A class used to render time
 */
public class TimeRenderer {

    public enum TimeBase {
        WEEK(1000 * DateUtils.SECOND_PER_WEEK), //
        DAY(1000 * DateUtils.SECOND_PER_DAY), //
        HOUR(1000 * DateUtils.SECOND_PER_HOUR), //
        MINUTE(1000 * DateUtils.SECOND_PER_MINUTE), //
        SECOND(1000), //
        MILLI(1), //
        ;

        private final long coef;

        private TimeBase(final long coef) {
            this.coef = coef;
        }

        public long getCoef() {
            return coef;
        }

    }

    private final long milliseconds;
    private final TimeRenderer.TimeBase base;
    private boolean printPrefixSuffix;

    public TimeRenderer(final long miliseconds) {
        super();
        this.milliseconds = Math.abs(miliseconds);
        this.base = computeBestResolution();
        this.printPrefixSuffix = false;
    }

    private TimeRenderer.TimeBase computeBestResolution() {
        final long positiveMillisecond = Math.abs(milliseconds);
        if (positiveMillisecond < 1000) {
            return TimeBase.MILLI;
        }
        final long second = positiveMillisecond / 1000;
        if (second > DateUtils.SECOND_PER_WEEK) {
            return TimeBase.WEEK;
        }
        if (second > DateUtils.SECOND_PER_DAY) {
            return TimeBase.DAY;
        }
        if (second > DateUtils.SECOND_PER_HOUR) {
            return TimeBase.HOUR;
        }
        if (second > DateUtils.SECOND_PER_MINUTE) {
            return TimeBase.MINUTE;
        }
        return TimeBase.SECOND;
    }

    public String getTimeString() {
        if (printPrefixSuffix) {
            if (milliseconds >= 0) {
                return Context.tr("{0} ago", doGetTimeString());
            }
            return Context.tr("in {0}", doGetTimeString());
        }
        return doGetTimeString();
    }

    private String doGetTimeString() {
        if (getSubBase() != null) {
            return printTime(getTime(), getBase()) + " " + printTime(getSubTime(), getSubBase());
        }
        return printTime(getTime(), getBase());
    }

    private String printTime(final long time, final TimeBase theBase) {
        switch (theBase) {
            case WEEK:
                return trn("{0} week", "{0} weeks", time, time);
            case DAY:
                return trn("{0} day", "{0} days", time, time);
            case HOUR:
                return trn("{0} hour", "{0} hours", time, time);
            case MINUTE:
                return trn("{0} minute", "{0} minutes", time, time);
            case SECOND:
                return trn("{0} second", "{0} seconds", time, time);
            case MILLI:
                return trn("{0} millisecond", "{0} milliseconds", time, time);
            default:
                return trn("{0} second", "{0} seconds", time, time);
        }
    }

    public TimeRenderer.TimeBase getBase() {
        return base;
    }

    public long getTime() {
        return milliseconds / base.getCoef();
    }

    public long getSubTime() {
        if (getSubBase() == null) {
            return 0;
        }
        return (milliseconds - (getTime() * base.getCoef())) / getSubBase().getCoef();
    }

    public TimeRenderer.TimeBase getSubBase() {
        switch (base) {
            case WEEK:
                return TimeBase.DAY;
            case DAY:
                return TimeBase.HOUR;
            case HOUR:
                return TimeBase.MINUTE;
            case MINUTE:
                return TimeBase.SECOND;
            case SECOND:
                return TimeBase.MILLI;
            case MILLI:
                return null;
            default:
                break;
        }
        return null;
    }

    public void setPrefixSuffix(final boolean hasPrefixSuffix) {
        this.printPrefixSuffix = hasPrefixSuffix;

    }

    public String render(final FormatStyle dateStyle) {
        if (milliseconds > (DateUtils.SECOND_PER_WEEK * 1000)) {
            if (printPrefixSuffix) {
                return Context.tr("on ", Context.getLocalizator().getDate(new Date(DateUtils.now().getTime() + milliseconds)).toString(dateStyle));
            }
            return Context.getLocalizator().getDate(new Date(DateUtils.now().getTime() + milliseconds)).toString(dateStyle);
        }
        return getTimeString();
    }

}
