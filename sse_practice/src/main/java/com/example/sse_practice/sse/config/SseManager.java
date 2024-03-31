package com.example.sse_practice.sse.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class SseManager {

    private final List<SseEmitter> sseEmitters = new CopyOnWriteArrayList<>();
    private final AtomicLong count = new AtomicLong();

    public SseEmitter add(SseEmitter emitter) {
        log.info("adding");
        sseEmitters.add(emitter);

        emitter.onCompletion(() -> {
            log.info("onCompletion callback");
            //emitter가 완료되면 emitter list에서 제거해줌
            sseEmitters.remove(emitter);
        });
        emitter.onTimeout(() -> {
            log.info("ontimeout callback");
            //timeout되면 emmiter 완료처리를 해준다.
            emitter.complete();
        });
        emitter.onError(error -> {
            log.error("error : {}", error);
            sseEmitters.remove(emitter);
        });
        return emitter;
    }

    public void count() {
        Long result = count.incrementAndGet();
        sseEmitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("count")
                        .data(result)
                );

            } catch (IOException e) {
                e.printStackTrace();
            }
        });




    }

}
