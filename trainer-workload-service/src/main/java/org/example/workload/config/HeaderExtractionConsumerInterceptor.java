package org.example.workload.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.example.workload.filter.TraceIdFilter.TRACE_ID_KEY;

@Component
@RequiredArgsConstructor
public class HeaderExtractionConsumerInterceptor implements RecordInterceptor<Object, Object> {

    @Override
    public @Nullable ConsumerRecord<Object, Object> intercept(ConsumerRecord<Object, Object> record, Consumer<Object, Object> consumer) {
        Header traceIdHeader = record.headers().lastHeader(TRACE_ID_KEY);
        if (traceIdHeader != null) {
            MDC.put(TRACE_ID_KEY, new String(traceIdHeader.value(), StandardCharsets.UTF_8));
        } else {
            MDC.put(TRACE_ID_KEY, UUID.randomUUID().toString());
        }
        MDC.put("topic", record.topic());
        MDC.put("partition", String.valueOf(record.partition()));
        MDC.put("offset", String.valueOf(record.offset()));
        return record;
    }

    @Override
    public void afterRecord(ConsumerRecord<Object, Object> record, Consumer<Object, Object> consumer){
        MDC.clear();
        SecurityContextHolder.clearContext();
    }
}
