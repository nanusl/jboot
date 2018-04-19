/**
 * Copyright (c) 2015-2018, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jboot.component.opentracing;

import brave.Tracing;
import brave.opentracing.BraveTracer;
import io.jboot.Jboot;
import io.opentracing.Tracer;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.urlconnection.URLConnectionSender;

public class ZipkinTracerFactory implements TracerFactory {


    final Tracer tracer;

    public ZipkinTracerFactory() {

        JbootOpentracingConfig config = Jboot.config(JbootOpentracingConfig.class);

        URLConnectionSender sender = URLConnectionSender.newBuilder()
                .endpoint(config.getUrl())
                .connectTimeout(config.getConnectTimeout())
                .compressionEnabled(config.isCompressionEnabled())
                .readTimeout(config.getReadTimeout())
                .build();

        AsyncReporter<Span> reporter = AsyncReporter.builder(sender)
                .build();


        Tracing tracing = Tracing.newBuilder()
                .spanReporter(reporter)
                .localServiceName(config.getServiceName())
                .build();

        tracer = BraveTracer.newBuilder(tracing).build();
    }


    @Override
    public Tracer getTracer() {
        return tracer;
    }
}
