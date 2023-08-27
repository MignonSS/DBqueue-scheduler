package project.walletscheduler.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShutdownListener implements ApplicationListener<ContextClosedEvent> {

    private final WalletSchedulerV3 walletScheduler;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        // 애플리케이션 종료 직전 스레드 풀 종료 처리
        walletScheduler.getExecutorService().shutdown();
    }
}
