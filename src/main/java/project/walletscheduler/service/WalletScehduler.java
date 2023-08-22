package project.walletscheduler.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.walletscheduler.domain.Wallet;
import project.walletscheduler.domain.WalletQueue;
import project.walletscheduler.repository.WalletQueueRepository;
import project.walletscheduler.repository.WalletRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletScehduler {

    private final WalletQueueRepository walletQueueRepository;
    private final WalletUpdaterService walletUpdaterService;

    private static Long lastWalletQueueId = 0L;

    @Scheduled(fixedDelay = 100)
    public void scheduleWalletTask() {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        // wallet_queues 테이블에서 100개의 데이터 가져오기
        List<WalletQueue> walletQueues = walletQueueRepository.getWalletQueues100OfEach(lastWalletQueueId, PageRequest.of(0, 100));
        lastWalletQueueId = walletQueues.get(walletQueues.size() - 1).getId();

        log.info("lastWalletQueueId : {}", lastWalletQueueId); // 100, 200, 300, ...

        // CompletableFuture를 사용한 비동기 작업으로 각 walletId 별로 나누어 하나의 스레드에 같은 walletId의 작업만 구분 할당하기
        List<CompletableFuture<Void>> taskResult = LongStream.rangeClosed(1, 10)
                .mapToObj(userId -> CompletableFuture.runAsync(() -> {
                    // 비동기로 실행될 작업
                    System.out.println("Async task is running");

                    // DB에서 가져온 100개의 walletQueue 중에서 현재 walletId 에 해당하는 walletQueue만 필터링
                    List<WalletQueue> curTasks = walletQueues.stream()
                            .filter(walletQueue -> walletQueue.getWallet().getUserId() == userId)
                            .collect(Collectors.toList());

                    // 필터링된 walletQueues 에 대한 작업 처리
                    for (WalletQueue walletQueue : curTasks) {
                        System.out.println("walletQueueId : " + walletQueue.getId() + ", userId : " + userId + ", thread : " + Thread.currentThread().getName());

                        walletUpdaterService.updateWallet(walletQueue);

//                        updateWallet(walletQueue);
//                        updateWalletAsync(walletQueue);
                    }
                }, executorService))
                .collect(Collectors.toList());

        CompletableFuture<Void> allOf = CompletableFuture.allOf(
                taskResult.toArray(new CompletableFuture[0])
        );

        try {
            allOf.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("error: {}", e.toString());
        }

        executorService.shutdown();
    }

/*
    @Transactional
    public void updateWallet(WalletQueue walletQueue) {
        Wallet wallet = walletRepository.findByIdWithPessimisticLock(walletQueue.getWallet().getId());

        wallet.changeBalance(walletQueue.getBalances());
        walletQueueRepository.delete(walletQueue);

        walletRepository.saveAndFlush(wallet);
    }

    @Async
    @Transactional
    public CompletableFuture<Void> updateWalletAsync(WalletQueue walletQueue) {
        Wallet wallet = walletRepository.findByIdWithPessimisticLock(walletQueue.getWallet().getId());

        wallet.changeBalance(walletQueue.getBalances());
        walletQueueRepository.delete(walletQueue);

        walletRepository.saveAndFlush(wallet);

        return CompletableFuture.completedFuture(null);
    }
*/
}

