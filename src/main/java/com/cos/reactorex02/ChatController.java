package com.cos.reactorex02;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;

@CrossOrigin
@RestController
public class ChatController {

	// 프로세서 사용 : 중간에 데이터가 들어와도 진행
	
	Sinks.Many<String> sink;

	// multicast() 새로 들어온 데이터만 응답받음 hot 시퀀스 = 스트림
	// replay() 기존 데이터 + 새로운 데이터 응답 cold 시퀀스
	
	public ChatController() {
		this.sink = Sinks.many().multicast().onBackpressureBuffer();
	}

	@PostMapping("/send")
	public EmitResult send(@RequestBody Content content) {

		String user = content.getUser();
		String chat = content.getChat();

		EmitResult result = sink.tryEmitNext(user + ": " + chat);

		return result;
	}

	// data : 실제값 \n\n\
	@GetMapping(value = "/sse")
	public Flux<ServerSentEvent<String>> sse() { // ServcerSendEvent의 ContentType은 text event stream
		return sink.asFlux().map(e -> ServerSentEvent.builder(e).build()).doOnCancel(() -> {
			System.out.println("SSE 종료됨");
			sink.asFlux().blockLast();
		}); // 구독
	}

}