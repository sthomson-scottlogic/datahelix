/*
 * Copyright 2019 Scott Logic Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.scottlogic.deg.generator.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.scottlogic.deg.generator.generation.*;

import java.io.PrintWriter;

public class MonitorProvider implements Provider<ReductiveDataGeneratorMonitor>  {
    private GenerationConfigSource commandLine;
    private NoopDataGeneratorMonitor noopDataGeneratorMonitor;

    @Inject
    MonitorProvider(
        GenerationConfigSource commandLine,
        NoopDataGeneratorMonitor noopDataGeneratorMonitor) {

        this.commandLine = commandLine;
        this.noopDataGeneratorMonitor = noopDataGeneratorMonitor;
    }

    @Override
    public ReductiveDataGeneratorMonitor get() {
        switch (commandLine.getMonitorType()) {
            case VERBOSE:
                return new MessagePrintingDataGeneratorMonitor(
                    new PrintWriter(System.err, true));

            case QUIET:
                return this.noopDataGeneratorMonitor;

            default:
                return new VelocityMonitor(
                    new PrintWriter(System.err, true));
        }
    }
}
