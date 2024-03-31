package com.example.sse_practice.sse.controller;


import com.example.sse_practice.sse.config.SseManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/sse")
public class SseController {

    private final SseManager sseManager;

    private static final Long TIME_OUT_LIMIT = 5 * 60 * 1000L;

    public SseController(SseManager sseManager) {
        this.sseManager = sseManager;
    }

    @GetMapping(value="/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> connect() {
        SseEmitter sseEmitter = new SseEmitter(TIME_OUT_LIMIT);
        sseManager.add(sseEmitter);

        /*
            연결 후 만료시간 까지 아무데이터도 보내지 않으면 503 에러를 보내므로
            연결 후 바로 Event 보내기
         */
        try {
            sseEmitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected!"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.ok(sseEmitter);
    }

    //외부에서 임 함수를 호출 했을 때 sseManager에 있는 sseEmitter들에게 send event를 날린다.
    @GetMapping("/count")
    public ResponseEntity<Void> count() {
        sseManager.count();
        return ResponseEntity.ok().build();
    }
}
